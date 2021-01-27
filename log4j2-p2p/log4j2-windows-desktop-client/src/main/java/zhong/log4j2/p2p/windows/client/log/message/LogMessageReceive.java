package zhong.log4j2.p2p.windows.client.log.message;

import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import zhong.log4j2.p2p.windows.client.LogP2pTrayIcon;
import zhong.log4j2.p2p.windows.client.config.Config;
import zhong.log4j2.p2p.windows.client.config.Instructions;

import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * log信息接收
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 09:59
 */
public class LogMessageReceive implements Runnable {
    /**
     * 单线程的线程池
     */
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    /**
     * 是否已关闭
     */
    private boolean closed;
    /**
     * 定时发送ping让服务端保持连接
     */
    private Timer timer;
    /**
     * 事件触发器
     */
    private Selector selector;
    /**
     * socket连接
     */
    private SocketChannel socketChannel;

    /**
     * 运行接收线程
     */
    public static void startReceive() {
        SERVICE.execute(new LogMessageReceive());
    }

    /**
     * jvm退出挂钩
     */
    private void hookUp() {
        Runtime.getRuntime().addShutdownHook(new Thread("log-p2p-windows-desktop-exit-hook") {
            @Override
            public void run() {
                LogMessageReceive.this.close();
            }
        });
    }

    /**
     * 关机处理
     */
    private void close() {
        if (closed) return;// 已关闭就不处理
        try {
            Set<SelectionKey> selectionKeys = this.selector.keys();
            for (SelectionKey selectionKey : selectionKeys) {
                try {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    if (Objects.nonNull(socketChannel)) socketChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 关闭事件触发器
            this.selector.close();
            SERVICE.shutdownNow();// 关闭线程池
            this.timer.cancel();// 取消定时任务
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.connect();// 连接
            this.obtain();// 不断的获取log推送
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送到系统通知
     */
    private void pushTrayIcon(LogMessageEntity messageEntity) {
        // 推送到桌面图标
        TrayIcon[] trayIcons = SystemTray.getSystemTray().getTrayIcons();
        // 拼接消息
        String time = DateFormatUtils.format(messageEntity.getTime(), "yyyy-MM-dd HH:mm:ss");
        String shortMessage = "时间: " + time + System.lineSeparator()
                + "级别: " + messageEntity.getLevel() + System.lineSeparator() +
                "摘要: " + messageEntity.getSummary();
        for (TrayIcon trayIcon : trayIcons) {
            if (trayIcon instanceof LogP2pTrayIcon) {
                trayIcon.setToolTip(shortMessage);
                TrayIcon.MessageType messageType;
                if ("ERROR".equalsIgnoreCase(messageEntity.getLevel())) {
                    messageType = TrayIcon.MessageType.ERROR;
                } else if ("WARN".equalsIgnoreCase(messageEntity.getLevel())) {
                    messageType = TrayIcon.MessageType.WARNING;
                } else if ("INFO".equalsIgnoreCase(messageEntity.getLevel())) {
                    messageType = TrayIcon.MessageType.INFO;
                } else {
                    messageType = TrayIcon.MessageType.NONE;
                }
                // 显示消息
                trayIcon.displayMessage("项目: " + messageEntity.getProject(), shortMessage, messageType);
            }
        }
    }

    /**
     * 获取日志事件
     */
    private void obtain() {
        try {
            // 监听触发的事件, 无事件就会阻塞
            while (this.selector.select() > 0) {
                Set<SelectionKey> keys = this.selector.selectedKeys();// 获得触发的事件列表
                keys.removeIf(selectionKey -> {
                    SocketChannel socketChannel = null;
                    try {
                        if (selectionKey.isReadable()) {
                            // 解析服务器推送的信息
                            socketChannel = (SocketChannel) selectionKey.channel();
                            LogMessageEntity messageEntity;
                            {
                                // 推送到消息队列
                                LogMessageParser parser = new LogMessageParser(socketChannel);
                                messageEntity = parser.parser();
                                if (Objects.isNull(messageEntity)) return true;
                                LogMessageManager.addLogMessage(messageEntity);
                            }
                            this.pushTrayIcon(messageEntity);// 推送到系统通知
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            socketChannel.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;// 防止重复处理同一个事件, 必须删除
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 建立连接
     */
    private void connect() throws IOException {
        // 挂钩jvm
        this.hookUp();
        {
            // 定时ping
            this.timer = new Timer("log4j2-p2p-ping");
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        LogMessageEntity logMessageEntity = new LogMessageEntity();
                        logMessageEntity.setInstruction(Instructions.ping);
                        logMessageEntity.setTime(System.currentTimeMillis());
                        String jsonStr = JSONObject.toJSONString(logMessageEntity);
                        ByteBuffer byteBuffer = ByteBuffer.allocate(jsonStr.length());
                        byteBuffer.put(jsonStr.getBytes(StandardCharsets.UTF_8));
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                    } catch (IOException e) {
                        close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10000, 10000);
        }
        {
            // 连接服务器
            this.socketChannel = SocketChannel.open(new InetSocketAddress(Config.SERVER_HOST, Config.SERVER_PORT));
            this.socketChannel.configureBlocking(false);
            this.selector = Selector.open();
            if (!this.socketChannel.finishConnect()) throw new IOException("连接失败");
            this.socketChannel.register(this.selector, SelectionKey.OP_READ);
        }
    }
}

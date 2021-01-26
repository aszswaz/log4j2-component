package zhong.log4j2.server.commpent;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * p2p 服务端
 *
 * @author aszswaz
 * @date 2021-01-26 星期二 12:31
 */
@Log4j2
class P2pServer implements Runnable {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    /**
     * 事件队列
     */
    private final Map<SocketChannel, ClientConnect> socketMap = new ConcurrentHashMap<>();
    /**
     * 定时任务清理无效连接
     */
    private Timer timer;
    /**
     * 是否关闭
     */
    private boolean close;

    private P2pServer(int port) {
        // 使用nio
        try {
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            // 设置非阻塞
            this.serverSocketChannel.configureBlocking(false);
            // 绑定端口
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            this.serverSocketChannel.bind(socketAddress);
            // 注册选择器, 设置触发事件为建立连接事件
            this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (Objects.nonNull(this.selector)) this.selector.close();
                if (Objects.nonNull(this.serverSocketChannel)) this.serverSocketChannel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static P2pServer start(int port) {
        P2pServer p2pServer = new P2pServer(port);
        p2pServer.init();
        p2pServer.service.execute(p2pServer);
        p2pServer.close = false;
        return p2pServer;
    }

    /**
     * 初始化
     */
    private void init() {
        // 启动定时器,定时检查连接是否过期
        this.timer = new Timer("log4j2-p2p-server-connect-check");
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                P2pServer.this.socketMap.entrySet().removeIf(entry -> {
                    ClientConnect clientConnect = entry.getValue();
                    if (System.currentTimeMillis() - clientConnect.getLastPing() > 30000) {
                        clientConnect.close();// 销毁连接
                        return true;// 从队列删除连接
                    }
                    return false;
                });
            }
        }, 5000, 5000);
        // 添加挂钩
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "log4j-p2p-server-jvm-exit-thread"));
    }

    public void close() {
        try {
            if (this.close) return;
            log.info("closing");
            this.selector.close();
            this.service.shutdownNow();
            this.timer.cancel();
            this.close = true;
            log.info("closed");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        this.socketMap.forEach((socket, client) -> client.close());
    }

    @Override
    public void run() {
        try {
            log.info("starting log4j2 p2p server...");
            // 判断选择器当中是否存在事件, 如果没有事件则进入阻塞
            while (this.selector.select() > 0) {
                // 获得触发事件的key
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                // 遍历发生的事件, 必须得使用迭代器,并且获取一次后必须立刻删除, 否则会出现事件被重复处理
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    try {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();// 从队列中删除事件, 否则事件不会自动消失, 会出现重复处理
                        // 新建立连接
                        if (selectionKey.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            // 连接加入监控队列
                            ClientConnect clientConnect = new ClientConnect();
                            clientConnect.setSocketChannel(socketChannel);
                            clientConnect.setLastPing(System.currentTimeMillis());
                            // 将连接加入读监听
                            clientConnect.setSelectionKey(socketChannel.register(this.selector, SelectionKey.OP_READ));
                            this.socketMap.put(socketChannel, clientConnect);
                            // 写出欢迎语
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            byteBuffer.put(Config.WELCOME.getBytes(StandardCharsets.UTF_8));
                            byteBuffer.flip();// 数据写出做好准备
                            socketChannel.write(byteBuffer);
                        } else if (selectionKey.isReadable()) {
                            SocketChannel socketChannel = null;
                            try {
                                socketChannel = (SocketChannel) selectionKey.channel();
                                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                int len = socketChannel.read(byteBuffer);
                                if (len == -1) {
                                    // 客户端主动关闭连接
                                    selectionKey.cancel();// 取消事件监听
                                    socketChannel.close();// 销毁连接
                                    this.socketMap.remove(socketChannel);
                                    continue;
                                }
                                String message = new String(byteBuffer.array(), 0, len, StandardCharsets.UTF_8);
                                if (Config.PING.equals(message)) {
                                    // 刷新连接保持时间
                                    this.socketMap.get(socketChannel).setLastPing(System.currentTimeMillis());
                                    // 回应客户端
                                    byteBuffer.flip();// 缓冲区就绪
                                    socketChannel.write(byteBuffer);
                                } else {
                                    // 接收消息不对立刻销毁连接
                                    selectionKey.cancel();
                                    socketChannel.close();
                                    this.socketMap.remove(socketChannel);
                                }
                            } catch (IOException e) {
                                socketChannel.close();
                                selectionKey.cancel();
                            }
                        }
                    } catch (Exception e) {
                        log.info(e.getMessage(), e);
                    }
                }
            }
        } catch (
                IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 群发日志消息
     */
    public void sendGroup(String header, byte[] message) {
        // 向保持着连接的客户端发送信息
        this.socketMap.entrySet().removeIf(entry -> {
            SocketChannel socketChannel = null;
            try {
                socketChannel = entry.getKey();
                byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
                byte[] end = "\r\n".getBytes(StandardCharsets.UTF_8);// 一行结束或消息发送完毕
                ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + message.length + end.length << 2);
                byteBuffer.put(headerBytes);
                byteBuffer.put(message);
                byteBuffer.put(end);
                byteBuffer.put(end);
                byteBuffer.flip();// 缓冲区准备就绪
                socketChannel.write(byteBuffer);
                return false;
            } catch (IOException e) {
                try {
                    socketChannel.close();
                } catch (IOException ioException) {
                    log.error(ioException.getMessage(), ioException);
                }
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        });
    }
}
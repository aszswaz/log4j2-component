package zhong.log4j2.p2p.windows.client;

import zhong.log4j2.p2p.windows.client.config.ToolTipMessages;
import zhong.log4j2.p2p.windows.client.log.message.LogMessageManager;
import zhong.log4j2.p2p.windows.client.mainform.P2pClientMainForm;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 托盘图标
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 09:19
 */
public class LogP2pTrayIcon extends java.awt.TrayIcon {

    private LogP2pTrayIcon(Image image) {
        super(image);
    }

    /**
     * 构建一个托盘图标
     */
    public static LogP2pTrayIcon build() {
        try {
            // 判断系统是否支持图标
            if (SystemTray.isSupported()) {
                LogP2pTrayIcon trayIcon;
                {
                    // 读取图标文件
                    String filePath = "images/icon.png";
                    InputStream imageStream = LogP2pTrayIcon.class.getClassLoader().getResourceAsStream(filePath);
                    if (Objects.isNull(imageStream)) throw new FileNotFoundException(filePath + " not found");
                    BufferedImage bufferedImage = ImageIO.read(imageStream);
                    trayIcon = new LogP2pTrayIcon(bufferedImage);
                }
                trayIcon.setToolTip(ToolTipMessages.NO_NEWS);// 设置提示信息
                final PopupMenu popupMenu = new PopupMenu();// 创建快捷采集
                {
                    MenuItem menuItem = new MenuItem("清理日志队列");
                    // 添加点击事件
                    menuItem.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LogMessageManager.clear();
                            P2pClientMainForm.clear();
                        }
                    });
                    popupMenu.add(menuItem);
                }
                {
                    // 显示主窗口
                    MenuItem menuItem = new MenuItem("显示主窗口");
                    menuItem.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            P2pClientMainForm.createMainForm();
                        }
                    });
                    popupMenu.add(menuItem);
                }
                {
                    // 退出
                    // 显示主窗口
                    MenuItem menuItem = new MenuItem("退出");
                    menuItem.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.exit(0);// 退出系统
                        }
                    });
                    popupMenu.add(menuItem);
                }
                trayIcon.setPopupMenu(popupMenu);
                return trayIcon;
            } else {
                // 系统不支持图标
                String os = System.getProperty("os.name");
                throw new LogP2pTrayIconException("os.name: " + os + ", icon not supported");
            }
        } catch (LogP2pTrayIconException e) {
            throw e;
        } catch (Exception e) {
            throw new LogP2pTrayIconException(e.getMessage(), e);
        }
    }
}

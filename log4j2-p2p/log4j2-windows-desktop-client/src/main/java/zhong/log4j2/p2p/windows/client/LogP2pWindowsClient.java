package zhong.log4j2.p2p.windows.client;

import zhong.log4j2.p2p.windows.client.log.message.LogMessageReceive;

import java.awt.*;
import java.io.IOException;

/**
 * 日志推送接收客户端
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 09:13
 */
public class LogP2pWindowsClient {
    public static void main(String[] args) throws AWTException, IOException {
        LogMessageReceive.startReceive();
         SystemTray.getSystemTray().add(LogP2pTrayIcon.build());
    }
}

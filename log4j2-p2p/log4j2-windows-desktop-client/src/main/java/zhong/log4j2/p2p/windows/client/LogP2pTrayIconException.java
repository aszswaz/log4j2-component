package zhong.log4j2.p2p.windows.client;

/**
 * 图标创建异常
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 09:25
 */
public class LogP2pTrayIconException extends RuntimeException {
    public LogP2pTrayIconException(String message) {
        super(message);
    }

    public LogP2pTrayIconException(String message, Throwable cause) {
        super(message, cause);
    }
}

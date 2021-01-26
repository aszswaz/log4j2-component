package zhong.log4j2.server.commpent;

/**
 * p2p服务端异常
 *
 * @author aszswaz
 * @date 2021-01-26 星期二 11:18
 */
@SuppressWarnings("unused")
public class P2pServerException extends RuntimeException {
    public P2pServerException() {
        super();
    }

    public P2pServerException(String message) {
        super(message);
    }

    public P2pServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public P2pServerException(Throwable cause) {
        super(cause);
    }

    protected P2pServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

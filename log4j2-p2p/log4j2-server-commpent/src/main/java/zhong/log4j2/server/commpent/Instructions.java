package zhong.log4j2.server.commpent;

/**
 * 指令集
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 12:53
 */
public enum Instructions {
    /**
     * 欢迎
     */
    welcome,
    /**
     * 日志推送
     */
    log,
    /**
     * 保持连接
     */
    ping
}

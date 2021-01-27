package zhong.log4j2.p2p.windows.client.log.message;

import lombok.Data;
import zhong.log4j2.p2p.windows.client.config.Instructions;

/**
 * log信息实体类
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 09:54
 */
@Data
public class LogMessageEntity {
    /**
     * 指令
     */
    private Instructions instruction;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 级别
     */
    private String level;
    /**
     * 事件产生类
     */
    private String logger;
    /**
     * 发生时间
     */
    private long time;
    /**
     * 项目
     */
    private String project;
    /**
     * 消息体或消息明细
     */
    private String body;
}

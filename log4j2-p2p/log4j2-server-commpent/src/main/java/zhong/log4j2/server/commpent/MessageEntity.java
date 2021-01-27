package zhong.log4j2.server.commpent;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息实体
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 12:51
 */
@Data
public class MessageEntity implements Serializable {
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

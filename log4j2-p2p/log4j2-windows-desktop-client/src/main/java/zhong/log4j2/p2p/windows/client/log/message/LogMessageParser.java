package zhong.log4j2.p2p.windows.client.log.message;

import com.alibaba.fastjson.JSONObject;
import zhong.log4j2.p2p.windows.client.config.Instructions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 信息读取和解析
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 11:29
 */
public class LogMessageParser {
    private final SocketChannel socketChannel;

    public LogMessageParser(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public LogMessageEntity parser() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        while (true) {
            int len = this.socketChannel.read(byteBuffer);
            if (len == -1) throw new IOException("连接关闭");
            if (len == 0) break;// 读取完毕或无内容
            byteOut.write(byteBuffer.array(), 0, len);
        }
        String jsonStr = byteOut.toString(StandardCharsets.UTF_8.name());
        LogMessageEntity messageEntity = JSONObject.parseObject(jsonStr, LogMessageEntity.class);
        // 过滤ping
        if (messageEntity.getInstruction() == Instructions.ping) {
            return null;
        }
        return messageEntity;
    }
}

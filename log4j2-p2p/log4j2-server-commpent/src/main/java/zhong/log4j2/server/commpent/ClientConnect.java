package zhong.log4j2.server.commpent;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * 客户端连接
 *
 * @author aszswaz
 * @date 2021-01-26 星期二 17:53
 */
@Log4j2
@Data
class ClientConnect {
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private long lastPing;

    public void close() {
        try {
            if (Objects.nonNull(this.socketChannel)) this.socketChannel.close();
            if (Objects.nonNull(this.selectionKey)) this.selectionKey.cancel();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

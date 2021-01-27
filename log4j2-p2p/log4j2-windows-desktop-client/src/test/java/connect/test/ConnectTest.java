package connect.test;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * 连接测试
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 19:09
 */
public class ConnectTest {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("desktop.computer", 13154));
        socketChannel.configureBlocking(false);
        if (socketChannel.finishConnect()) {
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
            selector:
            while (selector.select() > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int len = socketChannel.read(byteBuffer);
                        if (len == 0) break;
                        if (len == -1) {
                            socketChannel.close();
                            break selector;
                        }
                        System.out.println(new String(byteBuffer.array(), 0, len, StandardCharsets.UTF_8));
                    }
                    iterator.remove();
                }
            }
        }
    }
}

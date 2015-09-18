package edu.telemarket;

import edu.telemarket.https.Request;
import edu.telemarket.https.Response;
import edu.telemarket.https.Status;
import edu.telemarket.services.Service;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/18.
 */
public class RequestController implements Runnable {

    private static Logger logger = Logger.getLogger("RequestController");
    private final ByteBuffer buffer;
    private final SocketChannel channel;
    private final Selector selector;
    private static LinkedHashMap<String, Service> views = new LinkedHashMap<>();

    public static void regist(String pattern, Service service) {
        views.put(pattern, service);
    }

    public RequestController(ByteBuffer buffer, SocketChannel client, Selector selector) {
        this.buffer = buffer;
        this.channel = client;
        this.selector = selector;
    }

    @Override
    public void run() {
        Request request = getRequestFromBuffer(buffer);
        Service service = null;
        for (Map.Entry<String, Service> entry : views.entrySet()) {
            if (request.getFilePath().matches(entry.getKey())) {
                service = entry.getValue();
                break;
            }

        }
        SelectionKey key;
        try {
            key = channel.register(selector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, e, () -> "通道已关闭");
            return;
        }
        if (service == null) {
            key.attach(new Response(Status.NOT_FOUND_404));
        } else {
            Response response = service.execute(request);
            if (response == null) {
                key.attach(new Response(Status.NOT_FOUND_404));
            }
            key.attach(response);

        }

    }


    public static Request getRequestFromBuffer(ByteBuffer buffer) {
        buffer.flip();
        int remaining = buffer.remaining();
        byte[] bytes = new byte[remaining];
        buffer.get(bytes);
        int i = 0;
        for (; i < remaining; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                i += 2;
            }
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                break;
            } else {
                i += 2;
            }
        }
        System.out.println(new String(bytes));
        byte[] head = new byte[i];
        byte[] body = new byte[remaining - i];
        buffer.get(head, 0, i);
        buffer.get(body, i, remaining);
        return Request.createFromBytes(head, body);
    }
}

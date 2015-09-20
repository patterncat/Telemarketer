package edu.telemarketer;

import edu.telemarketer.http.Status;
import edu.telemarketer.http.exceptions.IllegalRequestException;
import edu.telemarketer.http.exceptions.ServerInternalException;
import edu.telemarketer.http.responses.NotFoundResponse;
import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.Response;
import edu.telemarketer.http.responses.ServerInternalResponse;
import edu.telemarketer.services.Service;

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
public class Controller implements Runnable {

    private static Logger logger = Logger.getLogger("Controller");
    private final ByteBuffer buffer;
    private final SocketChannel channel;
    private final Selector selector;
    private static Map<String, Service> services = new LinkedHashMap<>(); //TODO 一直可写的话注意同步问题

    public static void register(String pattern, Service service) {
        services.put(pattern, service);
    }

    public Controller(ByteBuffer buffer, SocketChannel client, Selector selector) {
        this.buffer = buffer;
        this.channel = client;
        this.selector = selector;
    }

    @Override
    public void run() {
        Request request;
        try {
            request = Request.parseFromBuffer(buffer);
        } catch (IllegalRequestException e) {
            logger.log(Level.WARNING, e, () -> "请求有错误");
            attachResponse(new Response(Status.BAD_REQUEST_400));
            return;
        }

        Service service = null;
        for (Map.Entry<String, Service> entry : services.entrySet()) {
            if (request.getFilePath().matches(entry.getKey())) {
                service = entry.getValue();
                break;
            }
        }

        Response response;
        if (service == null) {
            response = new NotFoundResponse();
        } else {
            try {
                response = service.execute(request);
                if (response == null) {
                    response = new NotFoundResponse();
                }
            } catch (ServerInternalException e) {
                logger.log(Level.SEVERE, e, () -> "服务器内部错误");
                attachResponse(new ServerInternalResponse());
                return;
            }
        }
        attachResponse(response);
        logger.info(request.getMethod() + " \"" + request.getFilePath() + "\" " + response.getStatus().getCode());

    }

    private void attachResponse(Response response) {
        try {
            SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE);
            key.attach(response);
        } catch (ClosedChannelException e) {
            logger.log(Level.WARNING, e, () -> "通道已关闭");
        }
    }


}

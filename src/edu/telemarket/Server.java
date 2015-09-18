package edu.telemarket;

import edu.telemarket.https.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/17.
 */
public class Server {
    private Logger logger = Logger.getLogger("Server");
    private InetAddress ip;
    private int port;
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private Selector selector;

    public Server(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;

    }

    public static void main(String[] args) {
        if (args.length < 1 || !args[0].equals("start")) {
            System.out.println("Usage: start [address:port]");
            System.exit(1);
        }
        InetAddress ip = null;
        int port;
        if (args.length == 2 && args[1].matches(".+:\\d+")) {
            String[] address = args[1].split(":");
            try {
                ip = InetAddress.getByName(address[0]);
            } catch (UnknownHostException e) {
                System.out.println("请输入正确的ip");
                System.exit(1);
            }
            port = Integer.valueOf(address[1]);
        } else {
            try {
                ip = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }
            port = 8090;
            System.out.println("使用默认ip和端口..." + ip.getHostAddress() + ":" + port);
        }

        Server server = new Server(ip, port);
        server.start();
    }

    private void init() {
        ServerSocketChannel serverChannel;
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(this.ip, this.port));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "初始化错误");
            System.exit(1);
        }


    }


    public void start() {
        init();

        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, () -> "selector错误");
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                try {
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        client.read(buffer);
                        executor.execute(new RequestController(buffer, client, selector));
                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        Response response = (Response) key.attachment();
                        client.write(response.getByteBuffer());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e, () -> "socket channel 出错了");
                }
            }
        }
    }

}

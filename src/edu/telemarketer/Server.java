package edu.telemarketer;

import edu.telemarketer.http.responses.Response;
import edu.telemarketer.services.Controller;
import edu.telemarketer.services.Service;
import edu.telemarketer.services.ServiceClass;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
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
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
            port = 8080;
            System.out.println("未指定地址和端口,使用默认ip和端口..." + ip.getHostAddress() + ":" + port);
        }

        Server server = new Server(ip, port);
        server.start();
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
                        client.register(selector, SelectionKey.OP_WRITE);
                        key.interestOps(SelectionKey.OP_WRITE);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        client.read(buffer);
                        executor.execute(new Controller(buffer, client, selector));
                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        Response response = (Response) key.attachment();
                        if (response == null) {
                            continue;
                        }
                        ByteBuffer byteBuffer = response.getByteBuffer();
                        if (byteBuffer.hasRemaining()) {
                            client.write(byteBuffer);
                        } else {
                            key.cancel(); //TODO 写注释解释这个
                            client.close();
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e, () -> "socket channel 出错了");
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private void init() {
        ServerSocketChannel serverChannel;
        try {
            registerServices();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(this.ip, this.port));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "初始化错误");
            System.exit(1);
        }
        System.out.println("服务器启动 http://" + ip.getHostAddress() + ":" + port + "/");
    }

    private void registerServices() throws IOException {
        URL packageUrl = this.getClass().getResource("/");
        if (packageUrl == null) {
            return;
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String name = this.getClass().getPackage().getName();
        registerFromPackage(name, packageUrl.getFile() + name.replaceAll("\\.", File.separator), classLoader);
    }

    private void registerFromPackage(String packageName, String packagePath, ClassLoader classLoader) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(".class"));
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                registerFromPackage(packageName + "." + file.getName(), file.getAbsolutePath(), classLoader);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {

                    Class<?> aClass = classLoader.loadClass(packageName + "." + className);// class forName 会执行静态域
                    ServiceClass annotation = aClass.getAnnotation(ServiceClass.class);
                    if (annotation != null && Service.class.isAssignableFrom(aClass)) { //TODO 写注释解释这个
                        Controller.register(annotation.urlPattern(), aClass.asSubclass(Service.class).newInstance());
                        System.out.println("成功注册服务: " + annotation.urlPattern() + "  " + className);
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.log(Level.WARNING, e, () -> "注册服务出错");
                }
            }
        }
    }

}

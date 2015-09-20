package edu.telemarketer.http.responses;

import edu.telemarketer.http.Status;
import edu.telemarketer.util.PropertiesHelper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/18.
 */
public class Response {

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static Logger logger = Logger.getLogger("Response");
    private static final String CHARSET = "utf-8";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    static {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Status status;
    private Map<String, String> heads;
    private byte[] content;

    public Response(Status status) {
        this.status = status;
        heads = new HashMap<>();
        content = new byte[0];
        heads.put("Date", simpleDateFormat.format(new Date()));
        heads.put("Server", PropertiesHelper.getProperty("server_name", "Telemarketer"));
        heads.put("Connection", "Close");
    }

    public Response(Status status, String json) {
        this(status);
        heads.put("Content-Type", "application/json; charset=" + CHARSET);
        try {
            this.content = json.getBytes(CHARSET);
        } catch (UnsupportedEncodingException ignore) {
        }
    }

    public Response(Status status, File file) {
        this(status);
        if (!file.isFile() && file.canRead() && file.getName().endsWith("file")) {
            this.status = Status.NOT_FOUND_404;
            return;
        }
        String path = file.getAbsolutePath();
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(path);
        try {
            content = Files.readAllBytes(FileSystems.getDefault().getPath(path));
        } catch (IOException e) {
            this.status = Status.NOT_FOUND_404;
            return;
        }
        if (contentType.startsWith("text")) {
            contentType += "; charset=" + CHARSET;
        }
        heads.put("Content-Type", contentType);
    }

    public void setHead(String key, String value) {
        heads.put(key, value);
    }

    public String getField(String key) {
        return heads.get(key);
    }



    public Status getStatus() {
        return status;
    }
    private ByteBuffer finalData = null;
    public ByteBuffer getByteBuffer() {
        if (finalData == null) {
            heads.put("Content-Length", String.valueOf(content.length));
            StringBuilder sb = new StringBuilder();
            sb.append(HTTP_VERSION).append(" ").append(status.getCode()).append(" ").append(status.getMessage()).append("\r\n");
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            sb.append("\r\n");
            byte[] head;
            try {
                head = sb.toString().getBytes(CHARSET);
            } catch (UnsupportedEncodingException ignore) {
                logger.log(Level.SEVERE, "amazing,不支持编码" + CHARSET);
                throw new IllegalStateException("amazing,不支持编码" + CHARSET);
            }
            finalData = ByteBuffer.allocate(head.length + content.length + 2);
            finalData.put(head);
            finalData.put(content);
            finalData.put((byte) '\r');
            finalData.put((byte) '\n');
            finalData.flip(); // 第一次没注意
        }
        return finalData;
    }
}

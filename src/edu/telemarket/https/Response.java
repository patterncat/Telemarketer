package edu.telemarket.https;

import edu.telemarket.util.PropertiesHelper;
import javafx.scene.input.DataFormat;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Be careful!
 * Created by hason on 15/9/18.
 */
public class Response {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz");
    private Status status;
    private Map<String, String> heads;

    public Response(Status status) {
        this.status = status;
        heads = new HashMap<>();
        heads.put("Date", simpleDateFormat.format(new Date()));
        heads.put("Server", PropertiesHelper.getProperty("server_name","Telemarketer"));
    }


    public ByteBuffer getByteBuffer() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status.getCode()).append(" ").append(status.getMessage()).append("\r\n");
        Charset.forName("UTF-8").encode(sb.toString());

        return null;
    }
}

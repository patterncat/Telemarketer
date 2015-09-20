package edu.telemarketer.http.requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/17.
 */
public class Request {

    private static Logger logger = Logger.getLogger("Request");


    private String path;
    private String method;
    private Map<String, String> head;
    private Map<String, String> requestParameters;
    private byte[] messageBody;


    private Request(Map<String, String> head, byte[] messageBody, String path, String method, Map<String, String> requestParameters) {
        this.messageBody = messageBody;
        this.head = head;
        this.path = path;
        this.method = method;
        this.requestParameters = requestParameters;
    }


    public static Request parseFromBytes(byte[] head, byte[] body) {
        BufferedReader reader;
        LinkedHashMap<String, String> headMap = new LinkedHashMap<>();
        try {
            reader = new BufferedReader(new StringReader(new String(head, "UTF-8")));
        } catch (UnsupportedEncodingException ignore) {
            logger.log(Level.SEVERE, "基本不可能出现的错误 编码方法不支持");
            throw new IllegalStateException("基本不可能出现的错误 编码方法不支持");
        }
        String path;
        String method;
        try {
            String[] lineOne = reader.readLine().split("\\s");
            path = URLDecoder.decode(lineOne[1], "utf-8");
            method = lineOne[0];
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
                    break;
                }
                String[] keyValue = line.split(":");
                headMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "请求解析有错误");
            throw new IllegalStateException(e);
        }
        Map<String, String> requestParameters = new HashMap<>();
        String[] pathPart = path.split("\\?");
        path = pathPart[0];
        if (pathPart.length == 2) {
            parseParameters(pathPart[1], requestParameters);
        }

        if (headMap.containsKey("Content-Type") && headMap.get("Content-Type").contains("application/x-www-form-urlencoded")) {
            try {
                String bodyMsg = new String(body, "utf-8");
                parseParameters(bodyMsg, requestParameters);
            } catch (UnsupportedEncodingException e) {
                logger.log(Level.SEVERE, "基本不可能出现的错误 编码方法不支持");
            }
        }
        return new Request(headMap, body, path, method, requestParameters);
    }

    public static Request parseFromBuffer(ByteBuffer buffer) {
        if (buffer.position() != 0) {
            buffer.flip();
        }
        int remaining = buffer.remaining();
        byte[] bytes = new byte[remaining];
        buffer.get(bytes);
        int position = 0;
        for (int i = 0; i < remaining; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                position = i;
                i += 2;
            }
            if (i + 1 < remaining && bytes[i] == '\r' && bytes[i + 1] == '\n') {
                break;
            }
        }
        buffer.rewind();
        byte[] head = new byte[position];
        buffer.get(head, 0, position);
        byte[] body = null;
        if (remaining - position > 4) {
            buffer.position(position + 4);
            body = new byte[remaining - position - 4];
            buffer.get(body, 0, remaining - position - 4);
        }
        return parseFromBytes(head, body);
    }

    private static void parseParameters(String s, Map<String, String> requestParameters) {
        String[] paras = s.split("&");
        for (String para : paras) {
            String[] split = para.split("=");
            requestParameters.put(split[0], split[1]);
        }
    }

    public boolean containParameter(String key) {
        return requestParameters.containsKey(key);
    }

    public String getRequestParameter(String key) {
        return requestParameters.get(key);
    }

    public String getField(String field) {
        return head.get(field);
    }

    public String getFilePath() {
        return path;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public String getMethod() {
        return method;
    }
}

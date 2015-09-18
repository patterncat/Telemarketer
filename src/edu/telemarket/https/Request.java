package edu.telemarket.https;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
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
    private byte[] messageBody;


    private Request(Map<String, String> head, byte[] messageBody, String path, String method) {
        this.messageBody = messageBody;
        this.head = head;
        this.path = path;
        this.method = method;
    }


    public static Request createFromBytes(byte[] head, byte[] body) {
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
            path = lineOne[1];
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
        return new Request(headMap, Arrays.copyOf(body, body.length), path, method);

    }




    public String getField(String field) {
        return head.get(field);
    }

    public String getFilePath() {
        return head.get("path");
    }

    public String getMethod() {
        return head.get("method");
    }
}

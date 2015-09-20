package edu.telemarketer.http.responses;

import edu.telemarketer.http.Status;
import edu.telemarketer.http.exceptions.ServerInternalException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Be careful!
 * Created by hason on 15/9/20.
 */
public class JsonResponse extends Response {
    public JsonResponse(Status status, JSONObject json) {
        super(status);
        if (json == null) {
            throw new ServerInternalException("Json响应对象为空");
        }
        heads.put("Content-Type", "application/json; charset=" + CHARSET);
        try {
            super.content=json.toString().getBytes(CHARSET);
        } catch (UnsupportedEncodingException ignored) {
        }

    }
}

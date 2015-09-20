package edu.telemarketer.services.servicesimpls;

import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.Response;
import edu.telemarketer.http.Status;
import edu.telemarketer.services.Service;
import edu.telemarketer.services.ServiceClass;
import edu.telemarketer.util.PropertiesHelper;

import java.io.File;

/**
 * Be careful!
 * Created by hason on 15/9/19.
 */

@ServiceClass(urlPattern = "^/$")
public class IndexService implements Service {
    @Override
    public Response execute(Request request) {
        return new Response(Status.SUCCESS_200, PropertiesHelper.getTemplateFile("index.html"));
    }
}

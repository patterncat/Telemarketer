package edu.telemarketer.services.servicesimpls;

import edu.telemarketer.http.Status;
import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.NotFoundResponse;
import edu.telemarketer.http.responses.Response;
import edu.telemarketer.services.Service;
import edu.telemarketer.services.ServiceClass;
import edu.telemarketer.util.PropertiesHelper;

import java.io.File;

/**
 * Be careful!
 * Created by hason on 15/9/17.
 */
@ServiceClass(urlPattern = "^" + StaticFileService.prefix + ".*$")
public class StaticFileService implements Service {

    public static final String prefix = "/s/";
    private static String staticPath;

    static {
        staticPath = PropertiesHelper.getProperty("static_path");
    }

    @Override
    public Response execute(Request request) {
        String filePath = staticPath + File.separator + request.getFilePath().replaceAll(prefix, "");
        File file = new File(filePath);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new NotFoundResponse();
        }
        return new Response(Status.SUCCESS_200, file);
    }
}

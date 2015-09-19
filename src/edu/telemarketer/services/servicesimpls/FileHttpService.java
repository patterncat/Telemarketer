package edu.telemarketer.services.servicesimpls;

import edu.telemarketer.http.responses.Response;
import edu.telemarketer.services.Service;
import edu.telemarketer.services.ServiceClass;
import edu.telemarketer.util.PropertiesHelper;
import edu.telemarketer.http.requests.Request;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/17.
 */
@ServiceClass(urlPattern = "^/s/.*$")
public class FileHttpService implements Service {
    private static final Logger logger = Logger.getLogger("FileHttpService");
    private static URI staticPath;

    static {
        try {
            staticPath = new URI(PropertiesHelper.getProperty("static_path"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "static_path 配置错误");
            System.exit(1);
        }
    }


    @Override
    public Response execute(Request request) {
        String filePath = staticPath.resolve(request.getFilePath()).toString();
        File file = new File(filePath);

        return null;
    }
}

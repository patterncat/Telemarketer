package edu.telemarketer.http.responses;

import edu.telemarketer.http.Status;
import edu.telemarketer.util.PropertiesHelper;

import java.io.File;

/**
 * Be careful!
 * Created by hason on 15/9/19.
 */
public class NotFoundResponse extends Response {

    private static final File PATH_404HTML;

    static {
        PATH_404HTML = new File(PropertiesHelper.getProperty("404html_path"));
    }

    public NotFoundResponse() {
        super(Status.NOT_FOUND_404, PATH_404HTML);
    }
}

package edu.telemarketer.http.responses;

import edu.telemarketer.http.Status;
import edu.telemarketer.http.exceptions.ServerInternalException;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * Be careful!
 * Created by hason on 15/9/20.
 */
public class FileResponse extends Response {

    public FileResponse(Status status, File file) {
        super(status);
        if (file == null) {
            throw new ServerInternalException("Response File 对象为空");
        }
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
}

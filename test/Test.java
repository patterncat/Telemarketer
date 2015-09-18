import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;

/**
 * Be careful!
 * Created by hason on 15/9/18.
 */
public class Test {

    public static void main(String[] args) throws IOException {
        HttpPost httppost = new HttpPost("http://49.140.166.27:8090/");

        FileBody bin = new FileBody(new File("/Users/hason/Documents/2345_image_file_copy_3.jpg"));

        HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("bin", bin).build();

        httppost.setEntity(reqEntity);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        httpclient.execute(httppost);

    }
}

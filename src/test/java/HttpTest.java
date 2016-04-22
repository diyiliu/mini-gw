import com.tiza.util.JacksonUtil;
import com.tiza.util.bean.Bts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * Description: HttpTest
 * Author: DIYILIU
 * Update: 2016-04-22 10:52
 */
public class HttpTest {


    @Test
    public void test() {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://api.cellocation.com/cell/?lac=21238&output=json&mnc=0&ci=10920&mcc=460");

            System.out.println("Executing request " + httpget.getRequestLine());

            ResponseHandler<String> responseHandler = (final HttpResponse response) -> {

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);

            Bts bts = JacksonUtil.toObject(responseBody, Bts.class);

            System.out.println(bts.getLat());
            System.out.println(bts.getLon());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

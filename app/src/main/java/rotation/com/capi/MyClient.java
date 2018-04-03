package rotation.com.capi;

import android.content.Context;
import android.content.res.Resources;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;

public class MyClient{

    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;
    public MyListener myListener;

    HttpResponse httpResponse;
    private String url;

    private int responseCode;
    private String message;

    private String response;

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    Context context;

    int flag;

    public MyClient(String url, Context ctx)
    {
        this.context = ctx;
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();

    }

    public void AddParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }

    public void AddHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }

    public void Execute(int method) throws Exception
    {

        switch(method) {

            case 0://get
            {

                //add parameters
                String combinedParams = "";
                if(!params.isEmpty()){
                    combinedParams += "?";
                    for(NameValuePair p : params)
                    {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
                        if(combinedParams.length() > 1)
                        {
                            combinedParams  +=  "&" + paramString;
                        }
                        else
                        {
                            combinedParams += paramString;
                        }
                    }
                }

                HttpGet request = new HttpGet(url + combinedParams);



                executeRequest(request, url);
                break;
            }

            case 1:  //post
            {
                HttpPost request = new HttpPost(url);


                if(!params.isEmpty()){
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }

                executeRequest(request, url);
                break;
            }
        }
    }

    private void executeRequest(final HttpUriRequest request, String url)
    {
        final HttpClient client = new DefaultHttpClient();


        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        httpResponse = trustEveryone().execute(request);

                        responseCode = httpResponse.getStatusLine().getStatusCode();
                        message = httpResponse.getStatusLine().getReasonPhrase();

                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {

                            InputStream instream = entity.getContent();
                            response = convertStreamToString(instream);
                            myListener.onResult(response);
                            // Closing the input stream will trigger connection release
                            instream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (Exception e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();

        }

    }


    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private DefaultHttpClient trustEveryone() throws Exception {
        // validation and accept all type of self signed certificates
        SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(context,null);
        sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        // Enable HTTP parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        // Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", sslFactory, 443));

        // Create a clientnew connection manager using the newly created registry and then create a clientnew HTTP client
        // using this connection manager
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient  client = new DefaultHttpClient(ccm, params);
        return client;

    }


}
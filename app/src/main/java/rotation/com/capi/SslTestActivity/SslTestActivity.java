package rotation.com.capi.SslTestActivity;

import java.io.*;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.*;
import javax.security.cert.Certificate;

import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.*;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;

import rotation.com.capi.R;

public class SslTestActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here

                    try {
                        // setup truststore to provide trust for the server certificate

                        // load truststore certificate
                        InputStream clientTruststoreIs = getResources().openRawResource(R.raw.servernew);
                        KeyStore trustStore = null;
                        trustStore = KeyStore.getInstance("BKS");
                        trustStore.load(clientTruststoreIs, "servernew".toCharArray());

                        System.out.println("Loaded server certificates: " + trustStore.size());

                        // initialize trust manager factory with the read truststore
                        TrustManagerFactory trustManagerFactory = null;
                        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init(trustStore);

                        // setup client certificate

                        // load client certificate
                        InputStream keyStoreStream = getResources().openRawResource(R.raw.clientpfx);
                        KeyStore keyStore = null;
                        keyStore = KeyStore.getInstance("PKCS12");
                        keyStore.load(keyStoreStream, "test".toCharArray());

                        System.out.println("Loaded client certificates: " + keyStore.size());

                        // initialize key manager factory with the read client certificate
                        KeyManagerFactory keyManagerFactory = null;
                        keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                        keyManagerFactory.init(keyStore, "MyPassword".toCharArray());


                        // initialize SSLSocketFactory to use the certificates
                        SSLSocketFactory socketFactory = null;
                        socketFactory = new SSLSocketFactory(SSLSocketFactory.TLS, keyStore, "servernew",
                                trustStore, null, null);

                        // Set basic data
                        HttpParams params = new BasicHttpParams();
                        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                        HttpProtocolParams.setContentCharset(params, "UTF-8");
                        HttpProtocolParams.setUseExpectContinue(params, true);
                        HttpProtocolParams.setUserAgent(params, "Android app/1.0.0");

                        // Make pool
                        ConnPerRoute connPerRoute = new ConnPerRouteBean(12);
                        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
                        ConnManagerParams.setMaxTotalConnections(params, 20);

                        // Set timeout
                        HttpConnectionParams.setStaleCheckingEnabled(params, false);
                        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
                        HttpConnectionParams.setSoTimeout(params, 20 * 1000);
                        HttpConnectionParams.setSocketBufferSize(params, 8192);

                        // Some client params
                        HttpClientParams.setRedirecting(params, false);

                        // Register http/s shemas!
                        SchemeRegistry schReg = new SchemeRegistry();
                        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                        schReg.register(new Scheme("https", socketFactory, 443));
                        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
                        DefaultHttpClient sClient = new DefaultHttpClient(conMgr, params);

                        HttpGet httpGet = new HttpGet("https://a3dyou.com:9000/texture/state/uuid?uuid=12");
                        HttpResponse response = sClient.execute(httpGet);
                        HttpEntity httpEntity = response.getEntity();

                        InputStream is = httpEntity.getContent();
                        BufferedReader read = new BufferedReader(new InputStreamReader(is));
                        String query = null;
                        while ((query = read.readLine()) != null)
                            System.out.println(query);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();




    }


/*
    private static KeyStore loadTrustStore(String[] certificateFilenames) {
        AssetManager assetsManager = GirdersApp.getInstance().getAssets();

        int length = certificateFilenames.length;
        List<Certificate> certificates = clientnew ArrayList<Certificate>(length);
        for (String certificateFilename : certificateFilenames) {
            InputStream is;
            try {
                is = assetsManager.open(certificateFilename, AssetManager.ACCESS_BUFFER);
                Certificate certificate = KeyStoreManager.loadX509Certificate(is);
                certificates.add(certificate);
            } catch (Exception e) {
                throw clientnew RuntimeException(e);
            }
        }

        Certificate[] certificatesArray = certificates.toArray(clientnew Certificate[certificates.size()]);
        return clientnew generateKeystore(certificatesArray);
    }

    */
/**
     * Generates keystore congaing the specified certificates.
     *
     * @param certificates certificates to add in keystore
     * @return keystore with the specified certificates
     * @throws KeyStoreException if keystore can not be generated.
     *//*

    public KeyStore generateKeystore(Certificate[] certificates) throws RuntimeException {
        // construct empty keystore
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);

        // initialize keystore
        keyStore.load(null, null);

        // load certificates into keystore
        int length = certificates.length;
        for (int i = 0; i < length; i++) {
            Certificate certificate = certificates[i];
            keyStore.setEntry(String.valueOf(i), clientnew KeyStore.TrustedCertificateEntry(certificate),
                    null);
        }
        return keyStore;
    }

*/


}
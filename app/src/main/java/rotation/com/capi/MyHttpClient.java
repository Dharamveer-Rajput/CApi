package rotation.com.capi;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.Certificate;

public class MyHttpClient extends DefaultHttpClient {

    final Context context;

    public MyHttpClient(Context context) {
        this.context = context;
    }

    @Override
    protected ClientConnectionManager createClientConnectionManager() {

        SchemeRegistry registry = new SchemeRegistry();

        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", newSslSocketFactory(), 443));


       // final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params,registry);

        return new SingleClientConnManager(getParams(), registry);
    }

/*    protected org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            // the bks file we generated above
            final InputStream in = context.getResources().openRawResource( R.raw.servernew);
            try {
                // don't forget to put the password used above in strings.xml/mystore_password
                ks.load(in, context.getString( R.string.mystore_password ).toCharArray());
            } finally {
                in.close();
            }

            return new AdditionalKeyStoresSSLSocketFactory(ks);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }*/


    private SSLSocketFactory newSslSocketFactory() {

        try {
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getResources().openRawResource(R.raw.servernew);
            try {
                trusted.load(in, "servernew".toCharArray());
            } finally {
                in.close();
            }
            return new SSLSocketFactory(trusted);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    //Add this code before the HttpsURLConnection and it will be done. I got it.
    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}
                },
                    new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }


    private class AdditionalKeyStoresSSLSocketFactory extends SSLSocketFactory {



        protected SSLContext sslContext = SSLContext.getInstance("TLS");

        public AdditionalKeyStoresSSLSocketFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(null, null, null, null, null, null);
            sslContext.init(null, new TrustManager[]{new AdditionalKeyStoresTrustManager(keyStore)}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }



        /**
         * Based on http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager
         */
        public  class AdditionalKeyStoresTrustManager implements X509TrustManager {

            protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();


            protected AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
                final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

                try {
                    // The default Trustmanager with default keystore
                    final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    original.init((KeyStore) null);
                    factories.add(original);

                    for( KeyStore keyStore : additionalkeyStores ) {
                        final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        additionalCerts.init(keyStore);
                        factories.add(additionalCerts);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }



                /*
                 * Iterate over the returned trustmanagers, and hold on
                 * to any that are X509TrustManagers
                 */
                for (TrustManagerFactory tmf : factories)
                    for( TrustManager tm : tmf.getTrustManagers() )
                        if (tm instanceof X509TrustManager)
                            x509TrustManagers.add( (X509TrustManager)tm );


                if( x509TrustManagers.size()==0 )
                    throw new RuntimeException("Couldn't find any X509TrustManagers");

            }

            /*
             * Delegate to the default trust manager.
             */
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
                defaultX509TrustManager.checkClientTrusted(chain, authType);
            }

            /*
             * Loop over the trustmanagers until we find one that accepts our server
             */
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                for( X509TrustManager tm : x509TrustManagers ) {
                    try {
                        tm.checkServerTrusted(chain,authType);
                        return;
                    } catch( CertificateException e ) {
                        // ignore
                    }
                }
                throw new CertificateException();
            }

            public X509Certificate[] getAcceptedIssuers() {
                final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
                for( X509TrustManager tm : x509TrustManagers )
                    list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
                return list.toArray(new X509Certificate[list.size()]);
            }
        }

    }

}

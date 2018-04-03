package rotation.com.capi;

import android.content.Context;
import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import org.apache.http.conn.ssl.SSLSocketFactory;


/**
 * Simple SSLFactory implementation which is designed to ignore all the SSL certificate.
 * Note: Please only use this for development testing (for self signed in certificate). Adding this to the
 * public application is a serious blunder.
 * @author Syed Ghulam Akbar
 */
public class SimpleSSLSocketFactory extends SSLSocketFactory
{

    public SimpleSSLSocketFactory (Context context , KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(null);


        newSslSocketFactory(context);

     /*   try
        {

           KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getResources().openRawResource(R.raw.servernew);
            try {
                trusted.load(in, "servernew".toCharArray());
            } finally {
                in.close();
            }

            return;


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }


    private SSLSocketFactory newSslSocketFactory(Context context) {
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




}
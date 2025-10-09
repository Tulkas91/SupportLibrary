package it.mm.supportlibrary.volley;

import android.content.Context;

import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.ResponseDelivery;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;

public class RequestQueueFactory {

    /**
     * Restituisce la coda di richieste in base al nome specificato.
     */
    public static RequestQueue getQueue(Context context, String name) throws Exception {
        RequestQueue result = null;

        if (it.mm.supportlibrary.volley.RequestOptions.DEFAULT_QUEUE.equals(name)) {
            result = getDefault(context);
        }
        if (it.mm.supportlibrary.volley.RequestOptions.BACKGROUND_QUEUE.equals(name)) {
            result = newBackgroundQueue(context);
        }

        return result;
    }

    /**
     * Restituisce la coda di richieste predefinita usando OkHttp.
     */
    public static RequestQueue getDefault(Context context) throws Exception {
        return Volley.newRequestQueue(context.getApplicationContext(), createOkHttpStack());
    }

    /**
     * Restituisce una coda di richieste per le immagini usando OkHttp.
     */
    public static RequestQueue getImageDefault(Context context) throws Exception {
        return newImageQueue(context.getApplicationContext(), it.mm.supportlibrary.volley.RequestOptions.DEFAULT_POOL_SIZE);
    }

    /**
     * Restituisce una coda di richieste di background.
     */
    public static RequestQueue newBackgroundQueue(Context context) throws Exception {
        return newBackgroundQueue(context, it.mm.supportlibrary.volley.RequestOptions.DEFAULT_POOL_SIZE);
    }

    /**
     * Restituisce una nuova coda di richieste di background con uno stack OkHttp.
     */
    public static RequestQueue newBackgroundQueue(Context context, int threadPoolSize) throws Exception {
        File cacheDir = new File(context.getCacheDir(), it.mm.supportlibrary.volley.RequestOptions.REQUEST_CACHE_PATH);

        // Crea un'istanza di OkHttp e configurala
        Network network = new BasicNetwork(createOkHttpStack());

        // Esegui le richieste in background con un pool di thread
        ResponseDelivery delivery = new ExecutorDelivery(Executors.newFixedThreadPool(threadPoolSize));

        // Usa NoCache se non hai bisogno di cache persistente
        RequestQueue queue = new RequestQueue(new NoCache(), network, threadPoolSize, delivery);
        queue.start();

        return queue;
    }

    /**
     * Restituisce una nuova coda di richieste per le immagini.
     */
    public static RequestQueue newImageQueue(Context context, int threadPoolSize) throws Exception {
        File rootCache = context.getExternalCacheDir();
        if (rootCache == null) {
            rootCache = context.getCacheDir();
        }

        File cacheDir = new File(rootCache, it.mm.supportlibrary.volley.RequestOptions.IMAGE_CACHE_PATH);
        cacheDir.mkdirs();

        Network network = new BasicNetwork(createOkHttpStack());
        DiskBasedCache diskBasedCache = new DiskBasedCache(cacheDir, RequestOptions.DEFAULT_DISK_USAGE_BYTES);

        RequestQueue queue = new RequestQueue(diskBasedCache, network, threadPoolSize);
        queue.start();

        return queue;
    }

    /**
     * Crea e configura uno stack di rete OkHttp per gestire le richieste di rete.
     */
    public static OkHttpStack createOkHttpStack() throws Exception {
        // Configura OkHttpClient
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // Configura il `SSLSocketFactory` personalizzato
        SSLSocketFactory sslSocketFactory = UnsafeSSLHelper.getUnsafeSslSocketFactory();
        clientBuilder.sslSocketFactory(sslSocketFactory, new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        });

        // Aggiungi logging interceptor per vedere le richieste (opzionale)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        // Configura protocolli compatibili
        clientBuilder.protocols(java.util.Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2, Protocol.HTTP_3));

        OkHttpClient okHttpClient = clientBuilder.build();

        return new OkHttpStack(okHttpClient);
    }

    public static class UnsafeSSLHelper {

        public static SSLSocketFactory getUnsafeSslSocketFactory() throws Exception {
            // Crea un TrustManager che accetta tutti i certificati
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Inizializza un contesto SSL con il TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return sslContext.getSocketFactory();
        }
    }
}

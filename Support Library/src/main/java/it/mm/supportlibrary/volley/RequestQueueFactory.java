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
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;

public class RequestQueueFactory {

    /**
     * Restituisce la coda di richieste in base al nome specificato.
     */
    public static RequestQueue getQueue(Context context, String name) {
        RequestQueue result = null;

        if (RequestOptions.DEFAULT_QUEUE.equals(name)) {
            result = getDefault(context);
        }
        if (RequestOptions.BACKGROUND_QUEUE.equals(name)) {
            result = newBackgroundQueue(context);
        }

        return result;
    }

    /**
     * Restituisce la coda di richieste predefinita usando OkHttp.
     */
    public static RequestQueue getDefault(Context context) {
        return Volley.newRequestQueue(context.getApplicationContext(), createOkHttpStack());
    }

    /**
     * Restituisce una coda di richieste per le immagini usando OkHttp.
     */
    public static RequestQueue getImageDefault(Context context) {
        return newImageQueue(context.getApplicationContext(), RequestOptions.DEFAULT_POOL_SIZE);
    }

    /**
     * Restituisce una coda di richieste di background.
     */
    public static RequestQueue newBackgroundQueue(Context context) {
        return newBackgroundQueue(context, RequestOptions.DEFAULT_POOL_SIZE);
    }

    /**
     * Restituisce una nuova coda di richieste di background con uno stack OkHttp.
     */
    public static RequestQueue newBackgroundQueue(Context context, int threadPoolSize) {
        File cacheDir = new File(context.getCacheDir(), RequestOptions.REQUEST_CACHE_PATH);

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
    public static RequestQueue newImageQueue(Context context, int threadPoolSize) {
        File rootCache = context.getExternalCacheDir();
        if (rootCache == null) {
            rootCache = context.getCacheDir();
        }

        File cacheDir = new File(rootCache, RequestOptions.IMAGE_CACHE_PATH);
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
    public static OkHttpStack createOkHttpStack() {
        // Configura OkHttpClient
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // Aggiungi logging interceptor per vedere le richieste (opzionale)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        // Configura protocolli compatibili
        clientBuilder.protocols(java.util.Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2));

        OkHttpClient okHttpClient = clientBuilder.build();

        return new OkHttpStack(okHttpClient);
    }
}

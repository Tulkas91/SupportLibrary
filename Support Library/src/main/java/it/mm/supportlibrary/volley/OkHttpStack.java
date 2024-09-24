package it.mm.supportlibrary.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.Header;
import com.android.volley.toolbox.HttpResponse;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OkHttpStack extends BaseHttpStack {

    private final OkHttpClient client;

    public OkHttpStack(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        // Crea la richiesta OkHttp
        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
        okHttpRequestBuilder.url(request.getUrl());

        // Aggiungi gli header personalizzati
        Map<String, String> headers = new HashMap<>();
        headers.putAll(request.getHeaders()); // Headers della richiesta Volley
        headers.putAll(additionalHeaders); // Headers aggiuntivi

        for (Map.Entry<String, String> header : headers.entrySet()) {
            okHttpRequestBuilder.addHeader(header.getKey(), header.getValue());
        }

        // Imposta il metodo HTTP (GET, POST, PUT, DELETE, ecc.)
        setMethod(request, okHttpRequestBuilder);

        // Esegui la richiesta con OkHttp
        Response okHttpResponse = client.newCall(okHttpRequestBuilder.build()).execute();

        // Gestisci il corpo della risposta in modo sicuro
        InputStream responseStream = null;
        if (okHttpResponse.body() != null && okHttpResponse.body().contentLength() > 0) {
            responseStream = okHttpResponse.body().byteStream();
        }

        // Estrai il codice di risposta e gli headers
        int responseCode = okHttpResponse.code();
        List<Header> responseHeaders = convertHeaders(okHttpResponse.headers().toMultimap());

        // Creazione della HttpResponse per Volley
        HttpResponse httpResponse = new HttpResponse(responseCode, responseHeaders, (int) okHttpResponse.body().contentLength(),  responseStream);

        // Assicurati di chiudere il corpo della risposta dopo l'uso per evitare perdite di risorse
        if (okHttpResponse.body() != null) {
            okHttpResponse.body().close();
        }

        return httpResponse;
    }

    /**
     * Imposta il metodo HTTP (GET, POST, PUT, ecc.) e il body della richiesta per POST e PUT.
     */
    private void setMethod(Request<?> request, okhttp3.Request.Builder builder) throws AuthFailureError, IOException {
        switch (request.getMethod()) {
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            default:
                throw new IllegalStateException("Metodo HTTP non supportato.");
        }
    }

    /**
     * Crea un RequestBody per POST e PUT.
     */
    private RequestBody createRequestBody(Request<?> request) throws AuthFailureError {
        final byte[] body = request.getBody();
        if (body != null) {
            return RequestBody.create(body, MediaType.parse(request.getBodyContentType()));
        }
        return RequestBody.create(new byte[0], MediaType.parse(request.getBodyContentType()));
    }

    /**
     * Converte gli headers OkHttp in un formato compatibile con Volley.
     */
    private List<Header> convertHeaders(Map<String, java.util.List<String>> headers) {
        List<Header> result = new ArrayList<>();
        for (Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.add(new Header(entry.getKey(), entry.getValue().get(0)));
            }
        }
        return result;
    }
}

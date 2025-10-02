package it.mm.supportlibrary.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpStack extends BaseHttpStack {

    private final OkHttpClient baseClient;

    public OkHttpStack(OkHttpClient client) {
        this.baseClient = client;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {

        // 1) Timeout per-request da Volley (fallback 10s se 0 o negativo)
        int timeoutMs = request.getTimeoutMs();
        if (timeoutMs <= 0) timeoutMs = 10_000;

        // 2) Clona il client con i timeout della request
        OkHttpClient clientForThisCall = baseClient.newBuilder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .callTimeout(timeoutMs, TimeUnit.MILLISECONDS) // opzionale ma consigliato
                .build();

        // 3) Costruisci la richiesta OkHttp
        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder()
                .url(request.getUrl());

        // Headers: request + additional (gestisci null)
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHeaders = request.getHeaders();
        if (reqHeaders != null) headers.putAll(reqHeaders);
        if (additionalHeaders != null) headers.putAll(additionalHeaders);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            okHttpRequestBuilder.addHeader(header.getKey(), header.getValue());
        }

        // Metodo + body
        setMethod(request, okHttpRequestBuilder);

        // 4) Esegui la call con il client “clonato”
        Response okHttpResponse = clientForThisCall.newCall(okHttpRequestBuilder.build()).execute();

        // 5) Converte in HttpResponse di Volley
        int responseCode = okHttpResponse.code();
        List<Header> responseHeaders = convertHeaders(okHttpResponse.headers().toMultimap());

        byte[] responseBody;
        try (InputStream responseStream = okHttpResponse.body().byteStream()) {
            responseBody = convertInputStreamToByteArray(responseStream);
        } finally {
            okHttpResponse.close();
        }

        return new HttpResponse(responseCode, responseHeaders, responseBody);
    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Imposta il metodo HTTP (GET, POST, PUT, DELETE, PATCH, HEAD) e il body dove necessario.
     */
    private void setMethod(Request<?> request, okhttp3.Request.Builder builder) throws AuthFailureError {
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
            case Request.Method.PATCH: // gestisci PATCH
                builder.patch(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            default:
                // fallback sicuro
                builder.get();
                break;
        }
    }

    /**
     * Crea un RequestBody per POST/PUT/PATCH.
     */
    private RequestBody createRequestBody(Request<?> request) throws AuthFailureError {
        final byte[] body = request.getBody();
        String ct = request.getBodyContentType();
        if (ct == null || ct.isEmpty()) ct = "application/octet-stream";
        MediaType mediaType = MediaType.parse(ct);
        return RequestBody.create(body != null ? body : new byte[0], mediaType);
    }

    /**
     * Converte gli headers OkHttp in un formato compatibile con Volley.
     */
    private List<Header> convertHeaders(Map<String, List<String>> headers) {
        List<Header> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.add(new Header(entry.getKey(), entry.getValue().get(0)));
            }
        }
        return result;
    }
}

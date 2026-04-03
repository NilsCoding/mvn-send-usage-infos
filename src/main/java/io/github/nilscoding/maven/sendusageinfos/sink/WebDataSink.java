package io.github.nilscoding.maven.sendusageinfos.sink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.nilscoding.maven.sendusageinfos.SettingsWrapper;
import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import io.github.nilscoding.maven.sendusageinfos.utils.SSLContextUtils;
import io.github.nilscoding.maven.sendusageinfos.utils.TrustAllCertsTrustManager;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.maven.plugin.logging.Log;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * HTTP/HTTPS sink.
 *
 * @author NilsCoding
 */
public final class WebDataSink implements IDataSink {

    @Override
    public boolean willWriteTo(String location) {
        if (location == null) {
            return false;
        }
        return (location.startsWith("http://") || location.startsWith("https://"));
    }

    @Override
    public void sendData(UsageData usageData, SinkParameters parameters, SettingsWrapper settings, Log log) {
        if ((usageData == null) || (parameters == null) || (log == null)) {
            return;
        }
        boolean prettyPrinting = parameters.isPrettyPrinting();
        String targetUrl = parameters.getUrl();
        if ((targetUrl == null) || (targetUrl.isEmpty())) {
            return;
        }
        String urlAuthHeader = parameters.getAuthHeader();
        String urlMethod = parameters.getHttpMethod();
        if ((urlMethod == null) || (urlMethod.isEmpty())) {
            urlMethod = "POST";
        }
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            // pretty-printing for web requests is disabled by default
            if (prettyPrinting) {
                gsonBuilder.setPrettyPrinting();
            }
            Gson gson = gsonBuilder.create();
            String bodyContent = gson.toJson(usageData);

            // initialize SSL: allow all certificates and hostnames, useful for intranet usage
            X509TrustManager customTrustManager = new TrustAllCertsTrustManager();
            SSLContext sslContext = SSLContextUtils.createContext("TLS", customTrustManager);
            SSLSocketFactory customSslSocketFactory = null;
            if (sslContext != null) {
                customSslSocketFactory = sslContext.getSocketFactory();
            }
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .followRedirects(true);
            if (customSslSocketFactory != null) {
                clientBuilder.followSslRedirects(true)
                        .sslSocketFactory(customSslSocketFactory, customTrustManager)
                        .hostnameVerifier((hostname, sslSession) -> true);
            }
            OkHttpClient client = clientBuilder.build();
            Request.Builder reqBuilder = new Request.Builder()
                    .url(targetUrl);
            if ((urlAuthHeader != null) && (urlAuthHeader.isEmpty() == false)) {
                reqBuilder.addHeader("Authorization", urlAuthHeader);
            }
            RequestBody reqBody = RequestBody.create(bodyContent, MediaType.get("application/json"));
            reqBuilder.method(urlMethod, reqBody);
            Request req = reqBuilder.build();
            log.info("sending usage info data to '" + targetUrl + "' ...");
            Call call = client.newCall(req);
            try (Response resp = call.execute()) {
                int respCode = resp.code();
                log.info("data sent (http " + respCode + ")");
            }
        } catch (Exception ex) {
            log.error("exception sending data via web: " + ex);
        }
    }
}

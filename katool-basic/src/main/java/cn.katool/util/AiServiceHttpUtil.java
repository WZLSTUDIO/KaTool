package cn.katool.util;

import cn.hutool.core.net.SSLUtil;
import lombok.*;
import okhttp3.*;

import javax.net.ssl.*;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiServiceHttpUtil {
    volatile protected OkHttpClient client = null;

    public OkHttpClient createHttpClient(Proxy proxy) {
        if(client == null){
            synchronized (this){
                if(client == null) {
                    ConnectionPool connectionPool = new ConnectionPool(10, 5, TimeUnit.MINUTES);
                    client = new OkHttpClient().newBuilder()
                            .connectionPool(connectionPool)
                            .retryOnConnectionFailure(true)
                            .hostnameVerifier(getIgnoreSslHostnameVerifier())
                            .proxy(proxy)
                            .sslSocketFactory(getIgnoreInitedSslContext().getSocketFactory(), IGNORE_SSL_TRUST_MANAGER_X509)
                            .readTimeout(5, TimeUnit.MINUTES)
                            .writeTimeout(5, TimeUnit.MINUTES)
                            .connectTimeout(5, TimeUnit.MINUTES)
                            .build();
                }
             }
        }
        return client;
    }
    public OkHttpClient createHttpClient() {
        return createHttpClient(null);
    }
    /**
     * X509TrustManager instance which ignored SSL certification
     */
    private static final X509TrustManager IGNORE_SSL_TRUST_MANAGER_X509 = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    };

    /**
     * Get initialized SSLContext instance which ignored SSL certification
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @SneakyThrows
    private static SSLContext getIgnoreInitedSslContext() {
        var sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { IGNORE_SSL_TRUST_MANAGER_X509 }, new SecureRandom());
        return sslContext;
    }

    /**
     * Get HostnameVerifier which ignored SSL certification
     *
     * @return
     */
    private static HostnameVerifier getIgnoreSslHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };
    }
}
package com.codeaim.urlcheck.probe.configuration;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpConfiguration
{
    @Autowired
    ProbeConfiguration probeConfiguration;

    @Bean
    OkHttpClient okHttpClient() throws KeyManagementException, NoSuchAlgorithmException
    {
        X509TrustManager x509TrustManager = new X509TrustManager()
        {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers()
            {
                return new X509Certificate[]{};
            }
        };

        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(
                null,
                new TrustManager[]{x509TrustManager},
                new java.security.SecureRandom());

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager)
                .hostnameVerifier((hostname, session) -> true)
                .connectTimeout(probeConfiguration.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(probeConfiguration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(probeConfiguration.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ExecutorService getExecutorService()
    {
        return Executors.newFixedThreadPool(probeConfiguration.getExecutorThreadPoolSize());
    }
}

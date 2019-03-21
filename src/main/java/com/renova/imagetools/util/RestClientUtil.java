package com.renova.imagetools.util;

import com.google.common.base.Throwables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.codehaus.jackson.map.ObjectMapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;

public final class RestClientUtil {

    private static final LazyInitializer<Client> CLIENT_INIT = LazyInitializer.newLazyInitializer(RestClientUtil::createSSLClient);

    private RestClientUtil() {
        super();
    }

    public static <T> T getRestResponse(String requestUrl, Class<T> clazz) {
        WebResource resource = CLIENT_INIT.getInstance().resource(requestUrl);
        ClientResponse response = resource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new IllegalStateException("Failed : HTTP error code : " + response.getStatus());
        }
        String jsonResponse = response.getEntity(String.class);
        ObjectMapper mapper = new ObjectMapper();
        T responseObject;
        try {
            responseObject = mapper.readValue(jsonResponse, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to map json response to object", e);
        }
        return responseObject;
    }

    public static boolean putRestRequest(String requestUrl, String productCode, int imageCount) {
        WebResource resource = CLIENT_INIT.getInstance().resource(requestUrl);
        String body = "{\"productCode\":\"" + productCode + "\",\"cdnImageCount\":" + imageCount + "}";
        ClientResponse response = resource.type("application/json")
                .accept("application/json").put(ClientResponse.class, body);
        if (response.getStatus() != 200) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private static Client createSSLClient() {
        final ClientConfig config = new DefaultClientConfig();
        config.getProperties()
                .put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        new HTTPSProperties(
                                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER,
                                SSLUtil.getInsecureSSLContext()));
        return Client.create(config);
    }

    private static class SSLUtil {
        protected static SSLContext getInsecureSSLContext() {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                final java.security.cert.X509Certificate[] arg0, final String arg1)
                                throws CertificateException {
                            // do nothing and blindly accept the certificate
                        }

                        public void checkServerTrusted(
                                final java.security.cert.X509Certificate[] arg0, final String arg1)
                                throws CertificateException {
                            // do nothing and blindly accept the server
                        }

                    }
            };

            SSLContext sslcontext = null;
            try {
                sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, trustAllCerts,
                        new java.security.SecureRandom());
            } catch (Exception e) {
                Throwables.propagate(e);
            }

            return sslcontext;
        }
    }
}

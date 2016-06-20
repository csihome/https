package net.famunity.https;

import java.io.File;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 *
 1、生成服务器证书库
 keytool -genkey -v -alias server -keyalg RSA -keystore server.keystore -dname "CN=server.synchronoss.com,OU=SNCR,O=IPA,L=Dublin,ST=Dublin,c=IE" -storepass changeit -keypass changeit -validity 3650

 2、生成客户端证书库
 keytool -genkeypair -v -alias client -keyalg RSA -storetype PKCS12 -keystore client.p12 -dname "CN=client.synchronoss.com,OU=SNCR,O=IPA,L=Dublin,ST=Dublin,c=IE" -storepass changeit -keypass changeit -validity 3650

 3、从客户端证书库中导出客户端证书
 keytool -export -v -alias client -keystore client.p12 -storetype PKCS12 -storepass changeit -rfc -file client.cer

 4、从服务器证书库中导出服务器证书
 keytool -export -v -alias server -keystore server.keystore -storepass changeit -rfc -file server.cer

 5、生成客户端信任证书库(由服务端证书生成的证书库)
 keytool -import -v -alias server -file server.cer -keystore client.truststore -storepass changeit
 keytool -import -v -alias httpbin -file httpbin.pem -keystore client.truststore -storepass changeit

 6、将客户端证书导入到服务器证书库(使得服务器信任客户端证书)
 keytool -import -v -alias client -file client.cer -keystore server.keystore -storepass changeit

 7、查看证书库中的全部证书
 keytool -list -keystore server.keystore -storepass changeit
 keytool -list -keystore client.truststore -storepass changeit


 a. get website ssl key
 openssl s_client -showcerts -connect httpbin.org:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >httpbin.pem
 */
public class ClientCustomSSL {

    public final static void main(String[] args) throws Exception {
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(new File("/Users/csihome/Workspace/Cui/Workspace/https/src/test/resources/ssl/client.truststore"), "changeit".toCharArray(), new TrustSelfSignedStrategy()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

//        CloseableHttpClient httpclient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

        try {

            HttpGet httpget = new HttpGet("https://httpbin.org/");

            System.out.println("Executing request " + httpget.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

}
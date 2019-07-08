package main;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

public class server {
    public static void main(String args[]) {
        try{
            InetSocketAddress address = new InetSocketAddress(1680);
            HttpsServer server = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            char[] password = "password".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream keyStream = main.server.class.getClassLoader().getResourceAsStream("testkey.jks");
            keyStore.load(keyStream, password);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext context = SSLContext.getDefault();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        SSLParameters defaultSslParams = context.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSslParams);
                    } catch (NoSuchAlgorithmException nsae) {
                        nsae.printStackTrace();
                    }
                }
            });
            //Create server event handler context
            server = createHandlerContexts(server);
            server.setExecutor(null);
            server.start();
            System.out.println("Server running and listening on " + address);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static HttpsServer createHandlerContext(HttpsServer server){
        server.createContext("/authenticate_user", new AuthenticateUserHandler());
        return server;
    }
}

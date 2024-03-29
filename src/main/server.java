package main;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import main.handlers.*;
import main.data.DatabaseInteraction;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class server {
    public static void main(String args[]){
        try{
            //Create socket address
            InetSocketAddress address = new InetSocketAddress(6969);

            //Initialize https server
            HttpsServer server = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            //Initialize the keystore
            char[] password = "password".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream inputStream = DatabaseInteraction.class.getClassLoader().getResourceAsStream("main/data/testkey.jks");
            keyStore.load(inputStream, password);

            //Create key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password);

            //Create the trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            //Create the HTTPS context and parameters
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext){
                public void configure(HttpsParameters params){
                    try{
                        //Init SSL context
                        SSLContext context = SSLContext.getDefault();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        //Fetch default params
                        SSLParameters defaultSSLParams = context.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParams);
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
            //Create server event handler context
            server = createHandlerContexts(server);
            //Start the server instance and hope for the best
            server.setExecutor(null);
            server.start();
            //Debug
            System.out.println("Server Running and Listening On " + address);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static HttpsServer createHandlerContexts(HttpsServer server){
        server.createContext("/user/authenticate", new AuthenticateUserHandler());
        server.createContext("/user/create", new UserHandler());
        return server;
    }
}

import java.util.concurrent.CountDownLatch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;



public class Server {

    public static HttpServer server = null;
    public static String response = null;
    public static String token = null;
    public static CountDownLatch tokenAcquiredLatch = null;

    public static void runServer(String port, String response_value, CountDownLatch tokensAcquiredLatchReference) throws Exception {
        // starts a server, where we will be redirected during OAuth2
        response = response_value;
        tokenAcquiredLatch = tokensAcquiredLatchReference;
        server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);
        server.createContext("/auth", new MyHandler());
        server.setExecutor(null);
        server.start();
    }

    public static void stopServer(){
        server.stop(1);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            // a handler for handling the request

            // get the query and parse an access token
            URI requestedUri = he.getRequestURI();
            String query = requestedUri.getRawQuery();

            try{
                token = parseToken(query);
            }
            catch(Exception ex){
                throw new IOException(ex.getMessage());
            }

            String last_response = "Thank you! You can close this window and go back to the application.";
            he.sendResponseHeaders(200, last_response.length());
            OutputStream os = he.getResponseBody();
            os.write(last_response.toString().getBytes());
            os.close();

            // countDown to turn off the server
            tokenAcquiredLatch.countDown();
        }

        public static String parseToken(String query) throws Exception{
            // a simple parser for obtaining an access token
            // just split the query by & and find, which argument has key "code="
            //then get the access token
            String[] splits = query.split("&");
            for(var s:splits){
                if(s.indexOf("code=") == 0){
                    String[] new_splits = s.split("=");
                    return new_splits[1];
                }
            }
            throw new Exception("The token is not provided in the query.");
        }
    }
}
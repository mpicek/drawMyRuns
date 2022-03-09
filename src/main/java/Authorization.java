import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.json.*;

public class Authorization extends Thread{

    public String URL = null;
    public CountDownLatch tokensAcquiredLatch;
    public String client_id = null;
    public String client_secret = null;
    public String port = null;
    public String scope = null;
    public String access_token = null;

    public Authorization(String client_id, String client_secret, String port, String scope) {
        /*
        Authorizes the user in Strava API.
         */
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.port = port;
        this.scope = scope;

        URL = "https://www.strava.com/oauth/authorize?client_id="
                + client_id
                + "&response_type=code&redirect_uri=http://localhost:"
                + port
                + "/auth&approval_prompt=force&scope="
                + scope;
        tokensAcquiredLatch = new CountDownLatch(1);
    }

    @Override
    public void run(){

        String server_response = "Thank you! You can close this window and go back to the application.";

        // creates a server where we will be redirected during OAuth2 authorization
        try{
            Server.runServer(port, server_response, tokensAcquiredLatch);
        }
        catch(Exception ex){
            System.out.println("Problem occured during running the server: " + ex.getMessage());
        }

        System.out.println("Please, open the following URL in the browser and follow the steps in the browser to authorize:");
        System.out.println(URL);

        // we have to wait until the user is authorized
        try{
            tokensAcquiredLatch.await();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }

        // we stop the server
        Server.stopServer();

        // we do the second part of the authorization
        access_token = POSTAuth();
    }

    public String POSTAuth(){

        // we create a Map with url parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", client_id);
        parameters.put("client_secret", client_secret);
        parameters.put("code", Server.token);
        parameters.put("grant_type", "authorization_code");

        // we create a form from the Map of parameters
        String form = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        // we create a new Http Client for the POST request
        HttpClient client = HttpClient.newHttpClient();

        // we build the POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.strava.com/api/v3/oauth/token"))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        String access_token = null;

        // we send the POST request
        try{
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(response.body().toString());
            access_token = obj.getString("access_token");

        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }

        return access_token;
    }

}

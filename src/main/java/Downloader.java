import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class Downloader {
    public static ArrayList<String> getActivityIds(String access_token, String[] types){
        // downloads ids of all activities of the selected types

        ArrayList<String> ids = new ArrayList<>();

        boolean someActivities = true;
        int page = 1;

        // while there are still some activities, get their ids, increase a page and get the next ids
        while(someActivities){
            HttpClient client = HttpClient.newHttpClient();

            // create a request
            Builder result = HttpRequest.newBuilder().uri(URI.create("https://www.strava.com/api/v3/athlete/activities?page=" + Integer.toString(page) + "&per_page=50"));
            result.timeout(Duration.ofMinutes(2L));
            result.header("accept", "application/json");
            result.header("authorization", "Bearer " + access_token);
            HttpRequest request = result.build();

            try{
                // send the request and get the ids
                HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // parse the response with an external library for parsing JSON format
                String newResponse = "{\"arr\": " + response.body().toString() + "}";
                JSONObject o = new JSONObject(newResponse);
                JSONArray arr = o.getJSONArray("arr");

                // iterate over the activities and get the ids
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject new_o = arr.getJSONObject(i);
                    var id = new_o.getBigInteger("id");
                    var type = new_o.getString("type");
                    if(Arrays.asList(types).contains(type)){
                        ids.add(id.toString());
                    }
                }

                // if there is no other activity set the flag someActivity to false to stop the cycle
                if(arr.length() == 0){
                    someActivities = false;
                }

            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
                someActivities = false;
            }
            page++;
        }
        return ids;
    }

    public static String getTrk(String id, String access_token) throws Exception{
        // create a Trk part for one trek for a .gpx file

        HttpClient client = HttpClient.newHttpClient();

        // create a request
        Builder result = HttpRequest.newBuilder().uri(URI.create("https://www.strava.com/api/v3/activities/" + id + "/streams?keys=latlng&key_by_type=true"));
        result.timeout(Duration.ofMinutes(2L));
        result.header("accept", "application/json");
        result.header("authorization", "Bearer " + access_token);
        HttpRequest request = result.build();

        String trk = null;

        try{
            // send the request and
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseString = response.body().toString();
            System.out.println("Processing activity " + id + ".");

            // get an array of points of the trek from the request response
            ArrayList<Pair<Double>> latlngArrayList = getLatLngArrayList(responseString);
            trk = GPXCreator.getTrk(latlngArrayList);

        }
        catch(Exception ex){
            throw new Exception("\tProblem occured - activity will not be included.");
        }
        System.out.println("\tDone.");
        return trk;
    }

    public static ArrayList<Pair<Double>> getLatLngArrayList(String response) throws Exception{
        // returns an arraylist of points of the trek from a JSON stream of data

        JSONObject o = new JSONObject(response);
        JSONObject latlng = null;
        JSONArray data = null;

        // try to parse the data
        try{
            latlng = o.getJSONObject("latlng");
            data = latlng.getJSONArray("data");
        }
        catch (Exception ex){
            throw new Exception("No data in the activity available.");
        }

        ArrayList<Pair<Double>> latlngList = new ArrayList<>();

        // iterate over data points, process them into the right format and return them
        for(int i = 0; i < data.length(); i++) {
            JSONArray one_data = data.getJSONArray(i);
            double lat = one_data.getDouble(0);
            double lng = one_data.getDouble(1);
            latlngList.add(new Pair<Double>(lat, lng));
        }

        return latlngList;
    }
}
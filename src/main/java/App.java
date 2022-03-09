import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class App {

    public static final String client_id = "";
    public static final String port = "37106";
    public static final String client_secret = "";
    public static void main(String[] args) throws Exception{

        if(args.length < 2){
            System.out.println("You need to provide output .html filename and what types of activities you want to display (eg. Run, Ride, ..)");
            System.out.println("For example, use these arguments: \"website.html Ride\" or \"my_runs.html Run\" or \"my_sports Run Ride Walk\"");
            System.exit(1);
        }

        // load the arguments
        String outputHtml = args[0];
        ArrayList<String> types = new ArrayList<>();
        for(int i = 1; i < args.length; i++){
            types.add(args[i]);
        }

        // authorize in the API
        Authorization auth = new Authorization(client_id, client_secret, port, "activity:read_all");

        // authorization is a thread
        auth.start();

        // we need to wait until it is done
        while (auth.isAlive()) {
            try {
                auth.join();
            } catch (InterruptedException ex) {
                System.out.println("Authorization interupted.");
                System.exit(1);
            }
        }

        String access_token = auth.access_token;

        // download all activity ids (of a selected type/types) of a user
        ArrayList<String> ids = Downloader.getActivityIds(access_token, types.toArray(new String[0]));

        // generate .gpx file
        StringBuilder gpx = new StringBuilder();

        // add a basic header
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<gpx version=\"1.0\">");


        for(var id:ids){
            try{
                // create a gpx trk (=part of a gpx file) for each activity
                String trk = Downloader.getTrk(id, access_token);
                gpx.append(trk);
            }
            catch (Exception ex){
                System.out.println("Problem occured: " + ex.getMessage());
            }
        }
        gpx.append("</gpx>");

        // delete the last output.gpx file
        Path path = Paths.get("output.gpx");
        try {
            Files.deleteIfExists(path);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // create an output.gpx file and pass there
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("output.gpx"));){
            writer.write(gpx.toString());
            System.out.println("Created output.gpx file.");
        }
        catch (Exception ex){
            System.out.println("Problem with creating .gpx file: " + ex.getMessage());
            System.exit(1);
        }

        // delete on old outputHtml file
        Path webPath = Paths.get(outputHtml);
        try {
            Files.deleteIfExists(webPath);
        }
        catch (Exception ex) {
            System.out.println("Problem with creating " + outputHtml + " file: " + ex.getMessage());
            System.exit(1);
        }

        // create a static website with treks visualized
        String htmlContent = WebsiteCreator.createWebsite(gpx.toString());

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputHtml));){
            writer.write(htmlContent);
        }

        System.out.println("Now you can open " + outputHtml + " in your browser to see your treks :)");
    }
}


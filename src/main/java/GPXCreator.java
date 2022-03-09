import java.util.ArrayList;

public class GPXCreator {

    public static String getTrk(ArrayList<Pair<Double>> data){
        // returns Trk part of a .gpx file in the right format

        StringBuilder trk = new StringBuilder();

        // start the trek
        trk.append("\t<trk>\n" + "\t\t<trkseg>\n");

        // insert every data point
        for(var x: data){
            trk.append("\t\t\t<trkpt lat=\"");
            trk.append(x.fst + "");
            trk.append("\" lon=\"");
            trk.append(x.snd + "");
            trk.append("\"></trkpt>\n");
        }

        // end the trek
        trk.append("\t\t</trkseg>\n" + "\t</trk>\n");
        return trk.toString();
    }
}

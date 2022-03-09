public class WebsiteCreator {

    public static String htmlPart1 = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
            "    <title>DrawMyRuns</title>\n" +
            "    <script type=\"text/javascript\" src=\"https://api.mapy.cz/loader.js\"></script>\n" +
            "    <script type=\"text/javascript\">Loader.load();</script>\n" +
            "</head>\n" +
            "<body id=\"advanced-markers\">\n" +
            "<div id=\"m\" style=\"height:800px\" ></div>\n" +
            "<script>\n" +
            "    var center = SMap.Coords.fromWGS84(14.400307, 50.071853);\n" +
            "var m = new SMap(JAK.gel(\"m\"), center, 5);\n" +
            "m.addDefaultLayer(SMap.DEF_TURIST).enable();\n" +
            "m.addDefaultControls();\n" +
            "\n" +
            "window.onload = function() { /* Funkce volaná po stisku tlačítka */\n" +
            "    var value = `";

    public static String htmlPart2 = "`;\n" +
            "    if (!value) { return alert(\"No data available.\"); }\n" +
            "    var xmlDoc = JAK.XML.createDocument(value);\n" +
            "\n" +
            "    var gpx = new SMap.Layer.GPX(xmlDoc, null, {maxPoints:50000000});\n" +
            "    m.addLayer(gpx);\n" +
            "    gpx.enable();\n" +
            "    gpx.fit();\n" +
            "}\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>";

    public static String createWebsite(String gpx){
        // creates a simple static website with .gpx file already inserted
        return htmlPart1 + gpx + htmlPart2;
    }
}

package ir.ac.iust.dml.kg.raw.distantsupervison.reUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by hemmatan on 6/10/2017.
 */
public class JSONHandler {

    public static JSONArray getJsonArrayFromURL(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // read from the URL
        Scanner scan = null;
        try {
            scan = new Scanner(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = new String();
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();

        // build a JSON array
        JSONArray arr = new JSONArray(str);

        return arr;
    }

    @Test
    public void sample() throws Exception {
        // build a URL
        String s = "http://dmls.iust.ac.ir:8100/rest/v1/raw/export";
        URL url = new URL(s);

        // read from the URL
        Scanner scan = new Scanner(url.openStream());
        String str = new String();
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();

        // build a JSON object
        JSONArray arr = new JSONArray(str);

        JSONObject temp = arr.getJSONObject(0);
        System.out.println(temp.get("predicate"));


        // get the first result
        //JSONObject res = obj.getJSONArray("results").getJSONObject(0);
        // System.out.println(res.getString("formatted_address"));
        // JSONObject loc =
        //        res.getJSONObject("geometry").getJSONObject("location");
        // System.out.println("lat: " + loc.getDouble("lat") +
        //        ", lng: " + loc.getDouble("lng"));
    }
}
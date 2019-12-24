package fr.xtof.tasklab;

import android.util.Base64;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Zimbra {
    public static boolean getNewCal(String user,String pwd) {
        String userpass = user + ":" + pwd;
        String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(),Base64.DEFAULT);
        try {
            // on prend toujours 7 jours en arriere pour n'avoir que les futurs events
            final String url = "https://zimbra.inria.fr/home/cerisara/calendar?fmt=ics&auth=ba&start=7day";
            URL uurl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) uurl.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestProperty("Authorization", basicAuth);
            int status = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            System.out.println("ZIMBRADET "+content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}


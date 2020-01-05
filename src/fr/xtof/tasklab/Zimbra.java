package fr.xtof.tasklab;

import android.util.Base64;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class Zimbra {
    public static void showCal() {
        ArrayList<String> events = new ArrayList<String>();
        try {
            ArrayList<String> eventstmp = new ArrayList<String>();
            BufferedReader in = new BufferedReader(new FileReader("/mnt/sdcard/zimbra.ics"));
            String inputLine;
            boolean inevt=false;
            String summ="", debyear="", debmonth="", debday="", debhour="", finyear="", finmonth="", finday="", finhour="";
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("BEGIN:VEVENT")) inevt = true;
                if (inputLine.startsWith("END:VEVENT") && inevt) {
                    inevt = false;
                    String s = "";
                    if (debday.length()>0) {
                        s+=debyear+debmonth+debday; // only for sorting purpose
                        s+=debday+"/"+debmonth+"/"+debyear;
                        if (debhour.length()>0) {
                            s+="("+debhour.substring(0,2)+":"+debhour.substring(2,4)+")";
                        }
                    }
                    if (finday.length()>0) {
                        s+="-"+finday+"/"+finmonth+"/"+finyear;
                        if (finhour.length()>0) {
                            s+="("+finhour.substring(0,2)+":"+finhour.substring(2,4)+")";
                        }
                        s+=" ";
                    }
                    s+=summ;
                    eventstmp.add(s);
                    summ=""; debyear=""; debmonth=""; debday=""; debhour=""; finyear=""; finmonth=""; finday=""; finhour="";
                }
                if (inputLine.startsWith("SUMMARY:") && inevt) summ=inputLine.substring(8);
                if (inputLine.startsWith("DTSTART") && inevt) {
                    int i = inputLine.indexOf(':');
                    if (i>=0) {
                        String datestr = inputLine.substring(i+1);
                        debyear = datestr.substring(0,4);
                        debmonth = datestr.substring(4,6);
                        debday = datestr.substring(6,8);
                        if (datestr.length()>=13)
                            debhour = datestr.substring(9,13);
                            // sinon, c'est pour la journee
                    }
                }
                if (inputLine.startsWith("DTEND") && inevt) {
                    int i = inputLine.indexOf(':');
                    if (i>=0) {
                        String datestr = inputLine.substring(i+1);
                        finyear = datestr.substring(0,4);
                        finmonth = datestr.substring(4,6);
                        finday = datestr.substring(6,8);
                        if (datestr.length()>=13)
                            finhour = datestr.substring(9,13);
                            // sinon, c'est pour la journee
                    }
                }
            }
            Collections.sort(eventstmp); 
            for (String s: eventstmp) {
                events.add(s.substring(8));
            }
            in.close();
        } catch (Exception e) {
            System.out.println("ZIMBRADET KO showcal");
            e.printStackTrace();
        }
        TaskLabAct.main.showCal(events);
    }
    public static boolean getNewCal(String user,String pwd) {
        final String userpass = user + ":" + pwd;
        final String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(),Base64.DEFAULT);
        Thread zimbrath = new Thread(new Runnable() {
            public void run() {
                try {
                    // Relative dates should not very well managed from Zimbra ??
                    final String url = "https://zimbra.inria.fr/home/cerisara/calendar?fmt=ics&auth=ba&start=-1day&end=+15days";
                    URL uurl = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) uurl.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.setRequestProperty("Authorization", basicAuth);
                    int status = con.getResponseCode();
                    System.out.println("ZIMBRADET status "+status);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    PrintWriter f = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/mnt/sdcard/zimbra.ics")));
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                        f.println(inputLine);
                    }
                    f.close();
                    in.close();
                    con.disconnect();
                    System.out.println("ZIMBRADET content "+content);
                    System.out.println("ZIMBRADET OK saved in /mnt/sdcard/zimbra.ics");
                } catch (Exception e) {
                    System.out.println("ZIMBRADET KO saved");
                    e.printStackTrace();
                }
            }
        });
        zimbrath.start();
        return false;
    }
}


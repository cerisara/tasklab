package fr.xtof.tasklab;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TaskClient {

    public interface ListShower {
        public void showList(List<String> vals);
        public void msg(String s);
    }

    public static final String serverurl = "http://tasklab.cerisara.fr";

    private static String serverpwd = "";

    // commands that can be sent to the server:
    public static final int POSITION = 0;
    public static final int MENU = 1;
    public static final int SAVE = 2;

    public ArrayList<String> vals = new ArrayList<String>();
    private ListShower display = new TextListShower();

    public TaskClient(String pwd, ListShower lshow) {
        serverpwd = pwd;
        vals.clear();
        vals.add("Press MENU");
        if (lshow!=null) display = lshow;
        display.showList(vals);
    }

    class TextListShower implements ListShower {
        @Override
        public void showList(List<String> vals) {
            for (int i=0;i<vals.size();i++) {
                System.out.println(Integer.toString(i)+": "+vals.get(i));
            }
            System.out.println("a:MENU b:SETUP c:SAVE");
        }
        public void msg(String s) {
            System.out.println(s);
        }
    }
 
    public void menu() {
        sendToServer(MENU,"");
    }

    public void setup() {
        // TODO
    }

    public void save(String s) {
        sendToServer(SAVE,s);
        display.msg("SAVE OK");
    }

    public void select(String pos) {
        sendToServer(POSITION, pos);
    }

    private void httppost(final String url, final String parms) {
        System.out.println("debug "+parms);
        try {
            byte[] postData       = parms.getBytes("UTF-8");
            int    postDataLength = postData.length;
            URL    uurl            = new URL( url );
            HttpURLConnection conn= (HttpURLConnection) uurl.openConnection();
            conn.setDoOutput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                wr.write( postData );
            } catch (Exception e) {
                e.printStackTrace();
            }
            int status = conn.getResponseCode();
            System.out.println("detson connection status "+status);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            String content = "";
            while ((inputLine = in.readLine()) != null) {
                content += inputLine+"\n";
            }
            in.close();
            conn.disconnect();
            handleServerInput(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleServerInput(String ss) {
        String[] lines = ss.split("\n");
        vals.clear();
        String s="";
        for (int i=0;i<lines.length;i++) {
            String l = lines[i].trim();
            if (l.equals("_n") && s.length()>0) {
                vals.add(s.trim());
                s="";
            } else s += l+"\n";
        }
        if (s.length()>0) vals.add(s.trim());
        display.showList(vals);
    }

    private void sendToServer(int cmd, String s) {
        String parms = "txt=";
        String scmd = "select";
        switch (cmd) {
            case SAVE:
                scmd = "save";
            case POSITION:
                parms += s;
                break;
            case MENU:
                scmd = "menu";
        }
        httppost(serverurl+"/"+scmd+"?auth="+serverpwd,parms);
    }

    public static void main(String args[]) throws Exception {
        BufferedReader f = new BufferedReader(new FileReader("password.txt"));
        String s=f.readLine();
        TaskClient cl = new TaskClient(s.trim(),null);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for (;;) {
            s = reader.readLine();
            if (s.equals("Q")) break;
            if (s.equals("a")) cl.menu();
            else if (s.equals("b")) cl.setup();
            else {
                cl.select(s);
            }
        }
    }
}

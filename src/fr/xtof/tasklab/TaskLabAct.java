package fr.xtof.tasklab;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;
import java.util.Arrays;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import java.lang.Exception;
import java.net.URLEncoder;
import java.net.URL;
import cz.msebera.android.httpclient.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import com.loopj.android.http.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.HttpContext;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.HttpGet;
import java.net.URI;

public class TaskLabAct extends FragmentActivity {
    private static String gitlabpwd = "";
    private static String gitlabusr = "";
    private static String gitlabtok = null;
    // private static GitlabAPI gitlabapi = null;

    // assume that the user has a Gitlab repository with this name and file:
    private static final String reponame = "TODO";
    private static final String repofile = "todo.txt";

    private static String zimbrausr = "";
    private static String zimbrapwd = "";
    ListView listView;
    ArrayList<String> vals = new ArrayList();
    public static Context ctxt;
    public static TaskLabAct main;
    private String fromshare = null;

    private RSS fr3items = new RSS();
    private RSS zdnetitems = new RSS();
    private RSS curitems = fr3items;

    // action to perform when clicking on an item in the list
    private int list2action = 0;

    // position of the list in RSS items just before showing the details of one item
    private int lastListPos = 0, top=0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ctxt=this;
        main=this;
        setContentView(R.layout.main);

        String k=PrefUtils.getFromPrefs(ctxt, "ONEAPPAUTH","");
        if (k.equals("")) askCreds(null);
        else {
            gitlabpwd=k;
        }

        // show the last list displayed:
        getCurTasks();

        Intent in = this.getIntent();
        fromshare = null;
        if (in.getAction().equals(Intent.ACTION_SEND)) getNewStringFromShareMenu(in);
    }

    /* Store all current tasks in store.
     * When pressing "GET" erases all current tasks with the ones from the server (TODO: ask for confirm)
     * When pressing "PUT" uploads current tasks onto the server
     */
    private void getCurTasks() {
        vals.clear();
        String k=PrefUtils.getFromPrefs(ctxt,"TASKLABDAT","");
        String[] kk = k.split(" &_@ ");
        vals.addAll(Arrays.asList(kk));
        vals.add("<New Task>");
        showList();
    }
    // called from Zimbra
    public void showCal(ArrayList events) {
        vals.clear();
        vals.addAll(events);
        showList();
    }
    private void setCurTasks() {
        String k="";
        if (vals.size()>0) k=vals.get(0);
        for (int i=1;i<vals.size()-1;i++) k=k+" &_@ "+vals.get(i);
        PrefUtils.saveToPrefs(ctxt,"TASKLABDAT",k);
    }
    private void addNewTask() {
        vals.add("<New Task>");
        showList();
    }

    private void msg(final String s) {
        main.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(main, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getNewStringFromShareMenu(Intent in) {
        fromshare = null;
        Object o = in.getExtras().get("android.intent.extra.TEXT");
        if (o!=null) {
            String s=(String)o;
            fromshare = ""+s;
            vals.set(vals.size()-1,s);
            addNewTask();
            setCurTasks();
            main.msg("Share OK "+s.length());
        } else {
            main.msg("WARNING: nothing to share");
        }
    }

    @Override
    public void onNewIntent(Intent in) {
        fromshare = null;
        System.out.println("ONNEWINTENT "+in.toString());
        if (in.getAction().equals(Intent.ACTION_SEND)) getNewStringFromShareMenu(in);
    }

    private void showDetails(final int i) {
        lastListPos = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
        vals.clear();
        vals.add(curitems.getTitle(i));
        vals.add(curitems.getSummary(i));
        setCurTasks();
        setButtons(null,"back",null);
        but2id=1;
        showList();
    }

    private void editTask(final int taskid) {
        class LoginDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View custv = inflater.inflate(R.layout.dialog_edit, null);
                // recopy the old task text to edit
                TextView txt = (TextView)custv.findViewById(R.id.taskdef);
                txt.setText(vals.get(taskid));

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(custv)
                    // Add action buttons
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView txt = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.taskdef);
                            // TODO: initialise la fenetre avec le texte precedent de la task ? (sauf pour newtask)
                            String s = txt.getText().toString();
                            // il nous faut une seule ligne par task
                            s=s.replace('\n',' ');
                            vals.set(taskid,s);
                            if (taskid>=vals.size()-1) vals.add("<New Task>");
                            setCurTasks();
                            showList();
                        }
                    })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginDialogFragment.this.getDialog().cancel();
                    }
                });

                return builder.create();
            }
        }
        LoginDialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(),"edit task");
    }

    private void menuTODOlist() {
        list2action = 2;
         new AlertDialog.Builder(this)
            .setTitle("Download TODO ")
            .setMessage("Download TODO ?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    httpget("http://xolki.duckdns.org/todo?auth="+gitlabpwd);
                }})
        .setNegativeButton(android.R.string.no, null).show();
    }
    private void menuRSS(final String endpoint) {
        list2action = 1;
        new AlertDialog.Builder(this)
            .setTitle("Download RSS "+endpoint)
            .setMessage("Download RSS "+endpoint+" ?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    httpget("http://xolki.duckdns.org/"+endpoint+"?auth="+gitlabpwd);
                }})
        .setNegativeButton(android.R.string.no, null).show();
    }
    private void choixmenu(int i) {
        switch(i) {
            case 0:
                menuRSS("rssf3");
                break;
            case 1:
                menuRSS("rsszdnet");
                break;
            case 2:
                menuRSS("rsshn");
                break;
            case 3:
                menuTODOlist();
                break;
        }
    }
    public void menu(View v) {
        list2action = 0;
        setButtons("Menu","SETUP","FR3");
        but2id=0;
        vals.clear();
        vals.add("RSS France 3");
        vals.add("RSS ZDNet");
        vals.add("RSS HackerNews");
        vals.add("TODO list");
        vals.add("Meteo");
        vals.add("Calendar PRO");
        vals.add("Calendar PERSO");
        vals.add("Emails");
        showList();
    }

    public void askCreds(View view) {
        class CustomListener implements View.OnClickListener {
            private final Dialog dialog;

            public CustomListener(Dialog dialog) {
                this.dialog = dialog;
            }

            @Override
            public void onClick(View v) {
                TextView glabpwd = (TextView) dialog.findViewById(R.id.usernamepwd);
                glabpwd.setText(gitlabpwd);
                glabpwd.invalidate();

                // If you want to close the dialog, uncomment the line below
                //dialog.dismiss();
            }
        }
        class LoginDialogFragment extends DialogFragment {
            public void setTok() {
            }

            @Override
            public void onResume() {
                super.onResume();
                TextView glabpwd = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernamepwd);
                glabpwd.setText(gitlabpwd);
                glabpwd.invalidate();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(inflater.inflate(R.layout.dialog_signin, null))
                    // Add action buttons
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView glabpwd = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernamepwd);
                            gitlabpwd = glabpwd.getText().toString();
                            PrefUtils.saveToPrefs(ctxt,"ONEAPPAUTH",gitlabpwd);
                        }
                    })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginDialogFragment.this.getDialog().cancel();
                    }
                });

                return builder.create();
            }
        }
        LoginDialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(),"signin");
        // Button theButton = dialog.getDialog().getButton(DialogInterface.BUTTON_NEUTRAL);
        // theButton.setOnClickListener(new CustomListener(dialog));
    }

    private void setButtons(final String b1, final String b2, final String b3) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    Button b = (Button)findViewById(R.id.but1);
                    if (b1==null) {
                        b.setEnabled(false);
                        b.setText("");
                    } else {
                        b.setEnabled(true);
                        b.setText(b1);
                    }
                }
                {
                    Button b = (Button)findViewById(R.id.but2);
                    if (b2==null) {
                        b.setEnabled(false);
                        b.setText("");
                    } else {
                        b.setEnabled(true);
                        b.setText(b2);
                    }
                }
                {
                    Button b = (Button)findViewById(R.id.but3);
                    if (b3==null) {
                        b.setEnabled(false);
                        b.setText("");
                    } else {
                        b.setEnabled(true);
                        b.setText(b3);
                    }
                }
            }
        });
    }
    private void showList() {
        // clean up vals
        for (int i=vals.size()-1;i>=0;i--)
            if (vals.get(i).length()==0) vals.remove(i);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get ListView object from xml
                listView = (ListView) findViewById(R.id.list);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctxt, android.R.layout.simple_list_item_1, vals);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int itemPosition     = position;
                        // String  itemValue    = (String) listView.getItemAtPosition(position);
                        // si on veut editer en cliquant dessus:
                        switch(list2action) {
                            case 0: // menu principal: choix RSS, emails...
                                choixmenu(itemPosition);
                                break;
                            case 1: // liste de RSS items: on veut le detail d'un item
                                showDetails(itemPosition);
                                break;
                            case 2: // on edit une tache dans la TODO list
                                editTask(itemPosition);
                                break;
                        }
                    }
                });
            }
        });
    }

    // shortcut button accessible on the first page
    public void fr3(View view) {
        menuRSS("rssf3");
    }
    private int but2id = 0;
    public void reset(View view) {
        switch(but2id) {
            case 0:
                askCreds(null);
                break;
            case 1:
                pageRSSList();
                break;
        }
    }
   
    private void pageRSSList() {
        setButtons("Menu","SETUP","FR3");
        but2id=0;
        vals.clear();
        for (int i=0;i<curitems.getNitems();i++)
            vals.add(curitems.getTitle(i));
        showList();
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.setSelectionFromTop(lastListPos,top);
            }
        });
    }

    private void httpget(final String url) {
        Thread th = new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("detson init connection "+url);
                    URL uurl = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) uurl.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    // con.setRequestProperty("Authorization", basicAuth);
                    int status = con.getResponseCode();
                    System.out.println("detson connection status "+status);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    String content = "";
                    while ((inputLine = in.readLine()) != null) {
                        content += inputLine+"\n";
                    }
                    in.close();
                    con.disconnect();
                    String str = content;
                    switch(list2action) {
                        case 1:
                            parseRSSList(str);
                            break;
                        case 2:
                            parseTODOList(str);
                            break;
                    }
                    // System.out.println("detson content "+content);
                } catch (Exception e) {
                    System.out.println("detson connect KO saved");
                    e.printStackTrace();
                }
            }
        });
        th.start();
    }

    private class DetSyncHttpClient extends SyncHttpClient {
        protected RequestHandle sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context) {
            System.out.println("WZA "+uriRequest.getClass().getName());
            if (uriRequest.getClass().getName().endsWith("HttpGet")) {
                HttpGet gg = (HttpGet)uriRequest;
                String s = gg.toString();
                int i=s.lastIndexOf(' ');
                s=s.substring(4,i);
                try {
                    String ss = s.replace("/TODO","%2FTODO");
                    URI uri = new URI(ss);
                    gg.setURI(uri);
                } catch (Exception e) {
                    System.out.println("AZAZAZ HORROR");
                    e.printStackTrace();
                }
                System.out.println("AZAZAZ "+gg.toString());
                return super.sendRequest(client,httpContext,gg,contentType, responseHandler, context);
            } else {
                return super.sendRequest(client,httpContext,uriRequest,contentType, responseHandler, context);
            }
        }
    }

    private class DetProgressTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog dialog = new ProgressDialog(ctxt);
        int getOuPut=0; // GET par defaut
        String url;

        public DetProgressTask(int getput, String url) {
            this.getOuPut=getput;
            this.url=url;
        }

        private void connect(final int typ, String url) {
            System.out.println("connect "+url);
            SyncHttpClient client = new DetSyncHttpClient();
            AsyncHttpResponseHandler rephdl = new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    // called before request is started
                    System.out.println("ON START");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    String str="OOO";
                    try {
                        str = new String(response, "UTF-8"); // for UTF-8 encoding
                        System.out.println("RESPONSEEEEEE "+str);
                        parseRSSList(str);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("ON SUCCESS "+str);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    String str="OOO";
                    try {
                        str = new String(errorResponse, "UTF-8"); // for UTF-8 encoding
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                    System.out.println("ON FAILURE "+str);
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                    System.out.println("ON RETRY");
                }
            };
        }

        /** progress dialog to show user that the backup is processing. */

        /** application context. */

        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            try {
                connect(getOuPut,url);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (success) {
            } else {
                main.msg("Error postexec");
            }
        }
    }

    private void parseTODOList(String s) {
        String[] st = s.split("\n");
        vals.clear();
        for (int i=0;i<st.length;i++)
            vals.add(st[i]);
        setCurTasks();
        main.msg("got TODO OK");
        showList();
    }
    private void parseRSSList(String s) {
        curitems.parse(s);
        vals.clear();
        for (int i=0;i<curitems.getNitems();i++)
            vals.add(curitems.getTitle(i));
        setCurTasks();
        main.msg("Pull OK");
        showList();
    }

    private class GenericProgressTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog dialog = new ProgressDialog(ctxt);
        Runnable f;

        public GenericProgressTask(Runnable fct) {
            f=fct;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(final String... args) {
            try {
                f.run();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) dialog.dismiss();
            if (success) {
            } else {
                main.msg("Error postexec");
            }
        }
    }
}

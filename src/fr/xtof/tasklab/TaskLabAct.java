package fr.xtof.tasklab;

import java.io.DataOutputStream;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;
import android.widget.EditText;
import java.util.Arrays;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import java.lang.Exception;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import android.text.Editable;
import android.text.Selection;

import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.HttpContext;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.HttpGet;
import java.net.URI;

public class TaskLabAct extends FragmentActivity {
    public static final String serverurl = "http://tasklab.cerisara.fr";

    private static String serverpwd = "";

    // commands that can be sent to the server:
    public static final int POSITION = 0;
    public static final int MENU = 1;
    public static final int SAVE = 2;

    public static Context ctxt;
    public static TaskLabAct main;

    ListView listView;
    ArrayList<String> vals = new ArrayList<String>();

    // position of the list in RSS items just before showing the details of one item
    private int lastListPos = 0, top=0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctxt=this;
        main=this;
        setContentView(R.layout.main);

        String k=PrefUtils.getFromPrefs(ctxt, "ONEAPPAUTH","");
        if (k.equals("")) askCreds(null);
        else {
            serverpwd=k;
        }

        vals.add("Press MENU");
        showList();

        Intent in = this.getIntent();
        if (in.getAction().equals(Intent.ACTION_SEND)) getNewStringFromShareMenu(in);
    }

    private void showList() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView = (ListView) findViewById(R.id.list);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctxt, android.R.layout.simple_list_item_1, vals);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        sendToServer(POSITION,Integer.toString(position));
                    }
                });
            }
        });
    }
 
    private void msg(final String s) {
        main.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(main, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getNewStringFromShareMenu(Intent in) {
        Object o = in.getExtras().get("android.intent.extra.TEXT");
        if (o!=null) {
            String s=(String)o;
            vals.clear();
            vals.add(s);
            showList();
            sendToServer(SAVE,s);
            main.msg("Share OK "+s.length());
        } else {
            main.msg("WARNING: nothing to share");
        }
    }

    @Override
    public void onNewIntent(Intent in) {
        System.out.println("ONNEWINTENT "+in.toString());
        if (in.getAction().equals(Intent.ACTION_SEND)) getNewStringFromShareMenu(in);
    }

    private void editIdea() {
        class LoginDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View custv = inflater.inflate(R.layout.dialog_edit, null);
                // recopy the old task text to edit
                TextView txt = (TextView)custv.findViewById(R.id.taskdef);
                txt.setText("");

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(custv)
                    // Add action buttons
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView txt = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.taskdef);
                            String s = txt.getText().toString();
                            String parms = "txt="+s;
                            httppost(serverurl+"/pushidea?auth="+serverpwd,parms);
                            list2action = 0;
                        }
                    })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginDialogFragment.this.getDialog().cancel();
                        list2action = 0;
                    }
                });
                return builder.create();
            }
        }
        LoginDialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(),"edit idea");
    }

    private void editTask(final int taskid) {
        class LoginDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View custv = inflater.inflate(R.layout.dialog_edit, null);
                EditText txt = (EditText)custv.findViewById(R.id.taskdef);
                String stxt = vals.get(taskid);
                txt.setText(stxt);
                // Editable etext = txt.getText();
                // Selection.setSelection(etxt,txt.length());
                txt.setSelection(stxt.length());

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
                            if (list2action==5) setCurTasks();
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

    // 1er bouton
    public void menu(View v) {
        vals.clear();
        sendToServer(MENU,"");
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
                glabpwd.setText(serverpwd);
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
                glabpwd.setText(serverpwd);
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
                            serverpwd = glabpwd.getText().toString();
                            PrefUtils.saveToPrefs(ctxt,"ONEAPPAUTH",serverpwd);
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

    // 2eme bouton
    public void reset(View view) {
        askCreds(null);
    }
    // 3eme bouton
    public void save(View view) {
        sendToServer(SAVE,"");
    }
   
    private void httppost(final String url, final String parms) {
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
            handleServerInput(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleServerInput(String s) {
        String[] lines = s.split("\n");
        vals.clear();
        String s="";
        for (int i=0;i<lines.length;i++) {
            String l = lines[i].trim();
            if (l.equals("_n") && s.length()>0) {
                vals.add(s);
                s="";
            } else l += s+"\n";
        }
        if (s.length()>0) vals.add(s);
        showList();
    }

    private void sendToServer(int cmd, String s) {
        String parms = null;
        String scmd = "select";
        switch (cmd) {
            case SAVE:
                scmd = "save";
            case POSITION:
                parms = s;
                break;
            case MENU:
                scmd = "menu";
        }
        httppost(serverurl+"/"+scmd+"?auth="+serverpwd,parms);
    }
}

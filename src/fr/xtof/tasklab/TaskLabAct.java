package fr.xtof.tasklab;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.widget.TextView;
import java.util.Arrays;
import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.view.View;
import android.content.Context;
import java.lang.Exception;
import java.net.URLEncoder;
import cz.msebera.android.httpclient.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import com.loopj.android.http.*;

public class TaskLabAct extends FragmentActivity {
    // TODO: check in the phone app storage if the config is here (secret keys + gtilab url); if not, let the user enter them in textfields
    private static String gitlabkey = "";// put your gitlab access token here";
    ListView listView;
    String[] vals = {"<New Task>"};
    static Context ctxt;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ctxt=this;
        setContentView(R.layout.main);
        showList();

        String k=PrefUtils.getFromPrefs(ctxt, "TASKLABKEY","");
        if (k.equals("")) askCreds();
        else {
            gitlabkey=k;
        }
    }
    private void askCreds() {
        class LoginDialogFragment extends DialogFragment {
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
                                TextView username = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.username);
                                gitlabkey = username.getText().toString();
                                PrefUtils.saveToPrefs(ctxt,"TASKLABKEY",gitlabkey);
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
		dialog.show(getSupportFragmentManager(),"dgs signin");

    }
    private void showList() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("run SHOWLIST");
                // Get ListView object from xml
                listView = (ListView) findViewById(R.id.list);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctxt, android.R.layout.simple_list_item_1, vals);
                listView.setAdapter(adapter);
                System.out.println("run SHOWLIOOOOOOOOOOOOOOST");
                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int itemPosition     = position;
                        String  itemValue    = (String) listView.getItemAtPosition(position);
                    }
                });
            }
        });
    }

    /** Called when the user touches the button */
    public void geturl(View view) {
        DetProgressTask dett = new DetProgressTask(0,"");
        dett.execute();
    }

    public void putfile(View view) {
        String s="";
        for (int i=0;i<vals.length-1;i++) { // not the last <new task>
            s+=vals[i]+'\n';
        }
        s=s.substring(0,s.length()-1);
        try {
            s=URLEncoder.encode(s,"UTF-8");
            DetProgressTask dett = new DetProgressTask(1,s);
            dett.execute();
        } catch (Exception e) {
            e.printStackTrace();
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
            SyncHttpClient client = new SyncHttpClient();
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
                        // encode
                        //
                        // decode
                        // TODO parse the JSON
                        // TODO handle priorities
                        // TODO manage conflicts
                        int i=str.indexOf("content\":\"");
                        if (i>=0) {
                            i+=10;
                            int j=str.indexOf("\"",i);
                            byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
                            str = new String(tmp2, "UTF-8");
                            if (typ==0) {
                                if (str.charAt(str.length()-1)!='\n') str+='\n';
                                str+="<New Task>";
                                vals=str.split("\n");
                                showList();
                            }
                        }
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

            if (typ==1) client.put("http://152.81.128.46:8888/api/v3/projects/88/repository/files?private_token="+gitlabkey+"&file_path=todo.txt&branch_name=master&content="+url+"&commit_message=update%20file", rephdl);
            else client.get("http://152.81.128.46:8888/api/v3/projects/88/repository/files?private_token="+gitlabkey+"&file_path=todo.txt&ref=master", rephdl);
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
                Toast.makeText(ctxt, "Successfully connected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctxt, "Error", Toast.LENGTH_LONG).show();
            }
        }
    }

}

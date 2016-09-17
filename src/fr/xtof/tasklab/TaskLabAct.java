package fr.xtof.tasklab;

import java.util.Arrays;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import java.lang.Exception;
import java.net.URLEncoder;
import cz.msebera.android.httpclient.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import com.loopj.android.http.*;

public class TaskLabAct extends Activity
{
    private final static String gitlabkey = "";// put your gitlab access token here";
    ListView listView;
    String[] vals = {"<New Task>"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        showList();
    }
    private void showList() {
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, vals);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition     = position;
                String  itemValue    = (String) listView.getItemAtPosition(position);
            }
        });
    }

    // TODO add feedback to the user while its trying to connect

    /** Called when the user touches the button */
    public void geturl(View view) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://152.81.128.46:8888/api/v3/projects/88/repository/files?private_token="+gitlabkey+"&file_path=todo.txt&ref=master", new AsyncHttpResponseHandler() {

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
                    // encode
                    //
                    // decode
                    // TODO parse the JSON
                    int i=str.indexOf("content\":\"");
                    if (i>=0) {
                        i+=10;
                        int j=str.indexOf("\"",i);
                        byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
                        str = new String(tmp2, "UTF-8");
                        if (str.charAt(str.length()-1)!='\n') str+='\n';
                        str+="<New Task>";
                        vals=str.split("\n");
                        showList();
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
        });
    }

    public void putfile(View view) {
        AsyncHttpClient client = new AsyncHttpClient();
        String s="";
        for (int i=0;i<vals.length-1;i++) { // not the last <new task>
            s+=vals[i]+'\n';
        }
        try {
            s=URLEncoder.encode(s.substring(0,s.length()-1),"UTF-8");

            client.put("http://152.81.128.46:8888/api/v3/projects/88/repository/files?private_token="+gitlabkey+"&file_path=todo.txt&branch_name=master&content="+s+"&commit_message=update%20file", new AsyncHttpResponseHandler() {

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
                        // encode
                        //
                        // decode
                        // TODO parse the JSON
                        int i=str.indexOf("content\":\"");
                        if (i>=0) {
                            i+=10;
                            int j=str.indexOf("\"",i);
                            byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
                            str = new String(tmp2, "UTF-8");
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
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

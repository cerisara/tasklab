package fr.xtof.tasklab;

import java.util.Arrays;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import java.lang.Exception;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

public class TaskLabAct extends Activity
{
    private final static String gitlabkey = "";// put your gitlab access token here";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

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
        // TODO encode the string to put in the file in HTML
        client.put("http://152.81.128.46:8888/api/v3/projects/88/repository/files?private_token="+gitlabkey+"&file_path=todo.txt&branch_name=master&content=some%20other%20content%20TOTO&commit_message=update%20file", new AsyncHttpResponseHandler() {

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
    }

}

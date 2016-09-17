package fr.xtof.tasklab;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

public class TaskLabAct extends Activity
{
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
        client.get("http://152.81.128.46:8888", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                System.out.println("ON START");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                System.out.println("ON SUCCESS");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                System.out.println("ON FAILURE");
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                System.out.println("ON RETRY");
            }
        });
    }
}

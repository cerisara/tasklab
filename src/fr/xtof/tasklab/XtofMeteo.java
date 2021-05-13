package fr.xtof.tasklab;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.view.WindowManager;
import android.widget.Button;
import android.content.Context;
import java.lang.Exception;
import android.widget.Toast;
import java.net.URI;
import java.io.File;
import java.util.Random;
import java.io.DataInputStream;
import java.util.ArrayList;

import org.joda.time.*;
import org.joda.time.format.*;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

import com.softmoore.android.graphlib.Function;
import com.softmoore.android.graphlib.Graph;
import com.softmoore.android.graphlib.GraphView;
import com.softmoore.android.graphlib.Label;
import com.softmoore.android.graphlib.Point;

public class XtofMeteo extends Activity {

	public static Context ctxt;
    public static XtofMeteo main = null;
    public File fdir = null;

    private Graph graph;
    private ArrayList<Point> pts = new ArrayList<Point>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ctxt=this;
        main=this;
        fdir = getExternalFilesDir(null);

        // JodaTimeAndroid.init(this);

        setContentView(R.layout.main);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initgraph();
    }

    private String getStrJson(String str, int i) {
        try {
            int j=str.indexOf("\"",i);
            byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
            String s = new String(tmp2, "UTF-8");
            return s;
        } catch (Exception e) {
            System.out.println("UTF-8 ENCODING exception");
            e.printStackTrace();
            return str;
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

		private void connect(final int task, String url) {
			SyncHttpClient client = new SyncHttpClient();
            client.setUserAgent("XtofMeteo/0.1");
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
						System.out.println("ON SUCCESS "+str);
						// encode
						//
						// decode
						// TODO parse the JSON
                        // TODO handle priorities
                        // TODO manage conflicts
                        try {
                            String s;
                            DateTimeFormatter parser    = ISODateTimeFormat.dateTimeParser();
                            int i=str.indexOf("\"time\"");
                            i+=8; s=getStrJson(str,i);
                            DateTime d = parser.parseDateTime(s);
                            System.out.println("dethour zoneid");
                            System.out.println(d.getHourOfDay());
                        } catch (Exception e) {
                            main.msg("Error parsing JSON");
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
					}
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

			if (task==0) {
                try {
                    String s = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=48.69&lon=6.18";
                            // URLEncoder.encode(gitlabusr+"/TODO","UTF-8")+
                    System.out.println("CALLDETGET "+s);
                    client.get(s, rephdl);
                } catch (Exception e) {
                    System.out.println("CALLDETGET error encoding ");
                    e.printStackTrace();
                }
            }
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


    private void getMeteo() {
        DetProgressTask dett = new DetProgressTask(0,"");
        dett.execute();
    }

    private void initgraph() {

        Point[] points =
        {
            new Point(1, 178),  new Point(2, 179),  new Point(3, 179),
            new Point(4, 181),  new Point(5, 180),  new Point(6, 182),
            new Point(7, 182),  new Point(8, 184),  new Point(9, 183),
            new Point(10, 185), new Point(11, 185), new Point(12, 186)
        };
        Label[] xLabels =
        {
            new Label(1, "J"),  new Label(2, "F"),  new Label(3, "M"),
            new Label(4, "A"),  new Label(5, "M"),  new Label(6, "J"),
            new Label(7, "J"),  new Label(8, "A"),  new Label(9, "S"),
            new Label(10, "O"), new Label(11, "N"), new Label(12, "D")
        };

        graph = new Graph.Builder()
            .setWorldCoordinates(-2, 15, -2, 20)
            .setXLabels(xLabels)
            .build();
        GraphView graphView = (GraphView)findViewById(R.id.graph_view);
        graphView.setGraph(graph);
        setTitle("Meteo Nancy");
    }

    public static void msg(final String s) {
        main.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(main, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void get(View v) {
        getMeteo();
    }

    public void changeView(View v) {
        pts.clear();
        final Button but = (Button) findViewById(R.id.butSwitch);
    }
}

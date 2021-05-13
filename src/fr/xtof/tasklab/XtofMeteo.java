/**
 * Licence: CC BY 4.0
 *
 * This app makes use of data and API from https://developer.yr.no/
 *
 * */

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

import android.graphics.Color;
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
    private ArrayList<String> times = new ArrayList<String>();
    private ArrayList<Float> pluies = new ArrayList<Float>();

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
        redrawGraph();
    }

    private String getHour(String s) {
        System.out.println("getHour with string "+s);
        // nasty hack, because other options are not so nice:
        // time package not available in old android
        // joda not easy to integrate without maven
        if (s.length()<14) return null;
        String x = s.substring(11,13);
        Integer h = Integer.parseInt(x);
        h += 2; // France TZ
        return h.toString();
    }
    private String getStrJson(String str, int i, char sep) {
        try {
            int j=str.indexOf(sep,i);
            if (j<0) return null;
            String s = str.substring(i,j);
            // byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
            // String s = new String(tmp2, "UTF-8");
            return s;
        } catch (Exception e) {
            System.out.println("UTF-8 ENCODING exception");
            e.printStackTrace();
            return null;
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
            client.setUserAgent("XtofMeteo/0.1 cerisara@gmail.com");
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
                            times.clear();
                            pluies.clear();
                            String s;
                            int a=0;
                            for (;;) {
                                int i=str.indexOf("\"time\"",a);
                                if (i<0||i+8>=str.length()) break;
                                i+=8; s=getStrJson(str,i,'"');
                                if (s==null) break;
                                String hr = getHour(s);

                                int j=str.indexOf("\"precipitation_amount\"",i);
                                if (j<0||j+23>=str.length()) break;
                                j+=23; s=getStrJson(str,j,'}');
                                if (s==null) break;
                                float pluie = Float.parseFloat(s);

                                System.out.println("partial json "+hr+" "+pluie);
                                times.add(hr); pluies.add(pluie);
                                a=j;
                            }
                            redrawGraph();
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

    private void redrawGraph() {
        main.runOnUiThread(new Runnable() {
            public void run() {
                Point[] points;
                Label[] xLabels;
                float pluiemax = 0;
                System.out.println("redraw graph "+times.size());
                if (times.size()==0) {
                    points = new Point[2];
                    xLabels = new Label[2];
                    points[0] = new Point(1,0);
                    points[1] = new Point(2,0);
                    xLabels[0] = new Label(1,"?");
                    xLabels[1] = new Label(2,"?");
                } else {
                    int npts = times.size();
                    if (npts>10) npts=10;
                    points = new Point[npts];
                    xLabels = new Label[npts];
                    for (int i=0;i<npts;i++) {
                        points[i] = new Point(i+1,pluies.get(i));
                        if (pluies.get(i)>pluiemax) pluiemax = pluies.get(i);
                        xLabels[i] = new Label(i+1,times.get(i));
                    }
                }
                System.out.println("points "+points);
                graph = new Graph.Builder()
                    .setWorldCoordinates(-2, points.length+1, -2, pluiemax+1.)
                    .setXLabels(xLabels)
                    .addLineGraph(points, Color.RED)
                    .build();
                GraphView graphView = (GraphView)findViewById(R.id.graph_view);
                graphView.setGraph(graph);
                setTitle("Meteo Nancy");
                System.out.println("graph redrawn");
            }
        });
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

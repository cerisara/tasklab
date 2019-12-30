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
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.TokenType;
import org.gitlab.api.models.GitlabProject;

public class TaskLabAct extends FragmentActivity {
	private static String gitlabpwd = "";
	private static String gitlabusr = "";
	private static String gitlaburl = "";
	private static String gitlabtok = null;
    private static GitlabAPI gitlabapi = null;

    // assume that the user has a Gitlab repository with this name and file:
    private static final String reponame = "TODO";
    private static final String repofile = "todo.txt";

	private static String zimbrausr = "";
	private static String zimbrapwd = "";
	ListView listView;
	ArrayList<String> vals = new ArrayList();
	public static Context ctxt;
	public static TaskLabAct main;
    private boolean zimbramode = false;
    private String fromshare = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ctxt=this;
		main=this;
		setContentView(R.layout.main);

        String k=PrefUtils.getFromPrefs(ctxt, "TASKLABPWD","");
        if (k.equals("")) askCreds(null);
        else {
            gitlabpwd=k;
            gitlaburl=PrefUtils.getFromPrefs(ctxt, "TASKLABURL","");
            gitlabusr=PrefUtils.getFromPrefs(ctxt, "TASKLABUSR","");
        }
        zimbrausr=PrefUtils.getFromPrefs(TaskLabAct.ctxt, "ZIMBRAUSR","");
        zimbrapwd=PrefUtils.getFromPrefs(TaskLabAct.ctxt, "ZIMBRAPWD","");

		getCurTasks();

		Intent in = this.getIntent();
		System.out.println("ONCREATE INTENT "+in.toString());
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


	private interface TextConsumer {
		public void newText(String s);
	}

	private void detdialog(String msg, TextConsumer fct) {

	}

	public void askCreds(View view) {
		class CustomListener implements View.OnClickListener {
			private final Dialog dialog;

			public CustomListener(Dialog dialog) {
				this.dialog = dialog;
			}

			@Override
			public void onClick(View v) {
				TextView glaburl = (TextView) dialog.findViewById(R.id.usernameurl);
				TextView glabusr = (TextView) dialog.findViewById(R.id.usernameusr);
				TextView glabpwd = (TextView) dialog.findViewById(R.id.usernamepwd);
				glaburl.setText(gitlaburl);
				glabusr.setText(gitlabusr);
				glabpwd.setText(gitlabpwd);
				glaburl.invalidate();
				glabusr.invalidate();
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
				TextView glaburl = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernameurl);
				TextView glabusr = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernameusr);
				TextView glabpwd = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernamepwd);
				glaburl.setText(gitlaburl);
				glabusr.setText(gitlabusr);
				glabpwd.setText(gitlabpwd);
				glaburl.invalidate();
				glabusr.invalidate();
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
							TextView glaburl = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernameurl);
							TextView glabusr = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernameusr);
							TextView glabpwd = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernamepwd);
							gitlabpwd = glabpwd.getText().toString();
							gitlaburl = glaburl.getText().toString();
							gitlabusr = glabusr.getText().toString();
							PrefUtils.saveToPrefs(ctxt,"TASKLABPWD",gitlabpwd);
							PrefUtils.saveToPrefs(ctxt,"TASKLABURL",gitlaburl);
							PrefUtils.saveToPrefs(ctxt,"TASKLABUSR",gitlabusr);
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

	public void askCredsZimbra() {
		class CustomListener implements View.OnClickListener {
			private final Dialog dialog;

			public CustomListener(Dialog dialog) {
				this.dialog = dialog;
			}

			@Override
			public void onClick(View v) {
				TextView glaburl = (TextView) dialog.findViewById(R.id.zimbrausr);
				TextView glabtok = (TextView) dialog.findViewById(R.id.zimbrapwd);
				glaburl.setText(zimbrausr);
				glabtok.setText(zimbrapwd);
				glaburl.invalidate();
				glabtok.invalidate();

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
				TextView glaburl = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.zimbrausr);
				TextView glabtok = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.zimbrapwd);
				glaburl.setText(zimbrausr);
				glabtok.setText(zimbrapwd);
				glaburl.invalidate();
				glabtok.invalidate();
			}

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Get the layout inflater
				LayoutInflater inflater = getActivity().getLayoutInflater();

				// Inflate and set the layout for the dialog
				// Pass null as the parent view because its going in the dialog layout
				builder.setView(inflater.inflate(R.layout.dialog_zimbra, null))
					// Add action buttons
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							TextView glaburl = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.zimbrausr);
							TextView glabtok = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.zimbrapwd);
							zimbrausr = glaburl.getText().toString();
							zimbrapwd = glabtok.getText().toString();
							PrefUtils.saveToPrefs(ctxt,"ZIMBRAUSR",zimbrausr);
							PrefUtils.saveToPrefs(ctxt,"ZIMBRAPWD",zimbrapwd);
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
		dialog.show(getSupportFragmentManager(),"zimbra");
		// Button theButton = dialog.getDialog().getButton(DialogInterface.BUTTON_NEUTRAL);
		// theButton.setOnClickListener(new CustomListener(dialog));
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
						editTask(itemPosition);
					}
				});
			}
		});
	}

	/** Called when the user touches the GET button */
	public void geturl(View view) {
        if (zimbramode) {
            if (!(zimbrausr.equals("") && zimbrapwd.equals(""))) {
                Zimbra.getNewCal(zimbrausr,zimbrapwd);
            }
        } else {
            if (gitlabtok==null) {
                new AlertDialog.Builder(this)
                    .setTitle("Gitlab OAuth")
                    .setMessage("Going to get an OAuth token from Gitlab; you'll have to repress GET afterwards. OK ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            gitlabOAuth();
                        }})
                .setNegativeButton(android.R.string.no, null).show();
            } else {
                new AlertDialog.Builder(this)
                    .setTitle("Download tasks")
                    .setMessage("Do you really want to erase your tasks with tasks from the server? (the text from share will be kept)")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            gitlabPull();
                        }})
                .setNegativeButton(android.R.string.no, null).show();
            }
        }
	}
	public void reset(View view) {
        if (zimbramode) {
            askCredsZimbra();
            return;
        }
		askCreds(null);
	}
    public void zimbra(View view) {
        if (zimbramode) {
            getCurTasks();
        } else {
            if (zimbrausr.equals("")) askCredsZimbra();
            Zimbra.showCal();
        }
        zimbramode = !zimbramode;
    }
	public void putfile(View view) {
        if (zimbramode) {
            main.msg("push2zimbra not implemented");
            return;
        }
		String s="";
		for (int i=0;i<vals.size()-1;i++) { // not the last <new task>
			s+=vals.get(i)+'\n';
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
						int i=str.indexOf("content\"");
						if (i>=0 && typ==0) {
							i+=10;
							int j=str.indexOf("\"",i);
							byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
							str = new String(tmp2, "UTF-8");
							if (str.length()==0||str.charAt(str.length()-1)!='\n') str+='\n';
                            if (fromshare!=null) str+=fromshare+'\n';
							str+="<New Task>";
							vals=new ArrayList(Arrays.asList(str.split("\n")));
							setCurTasks();
							main.msg("Pull OK");
							showList();
						} else if (typ==1) {
							main.msg("Push OK");
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
					System.out.println("ON FAILURE2 "+gitlaburl);
				}

				@Override
				public void onRetry(int retryNo) {
					// called when request is retried
					System.out.println("ON RETRY");
				}
			};

			if (typ==1) client.put(gitlaburl+"?private_token="+gitlabtok+"&ref=master&branch=master&content="+url+"&commit_message=update%20file", rephdl);
			// else client.get(gitlaburl+"?private_token="+gitlabkey+"&ref=master&branch=master", rephdl);
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


    public void gitlabOAuth() {
        GenericProgressTask oauthtask = new GenericProgressTask(new Runnable() {
            public void run() {
                // final String userpass = gitlabusr + ":" + gitlabpwd;
                // final String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(),Base64.DEFAULT);
            try {
                    final String url = gitlaburl+"/oauth/token";
                    String urlParameters  = "grant_type=password"+
                        "&username="+URLEncoder.encode(gitlabusr,"UTF-8")+
                        "&password="+URLEncoder.encode(gitlabpwd,"UTF-8");
                    byte[] postData       = urlParameters.getBytes();
                    int    postDataLength = postData.length;
                    URL uurl = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) uurl.openConnection();
                    con.setRequestMethod("POST");
                    con.setReadTimeout(10000);
                    con.setConnectTimeout(15000);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
                    con.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                    con.setUseCaches( false );
                    con.getOutputStream().write( postData );
                    int status = con.getResponseCode();
                    System.out.println("GITLAB STATUS "+status);
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();
                    System.out.println("GITLAB OK "+content);
                    String s = content.toString();
                    int i=s.indexOf("access_token");
                    if (i>=0) {
                        int j=s.indexOf(":",i);
                        if (j>i) {
                            j+=2;
                            int k=s.indexOf("\"",j);
                            if (k>j) {
                                gitlabtok = s.substring(j,k);
                                msg("OAuth token retrieved");
                            } else msg("Problem with gitlab token ?");
                        } else msg("Problem with gitlab token ?");
                    } else msg("Problem with gitlab token ?");
                } catch (Exception e) {
                    System.out.println("GITLAB KO ");
                    e.printStackTrace();
                }

            }
        });
        oauthtask.execute();
    }
    public void gitlabPull() {
        GenericProgressTask task = new GenericProgressTask(new Runnable() {
            public void run() {
            try {
                    gitlabapi = GitlabAPI.connect(gitlaburl, gitlabtok, TokenType.ACCESS_TOKEN);
                    GitlabProject p = gitlabapi.getProject(gitlabusr,"TODO");
                    if (p==null||!p.getName().equals("TODO")) msg("Cannot find repo TODO");
                    else {
                        byte[] tod = gitlabapi.getRawFileContent(p,"master","todo.txt");
                        String str = new String(tod);
                        if (str.length()==0||str.charAt(str.length()-1)!='\n') str+='\n';
                        if (fromshare!=null) str+=fromshare+'\n';
                        str+="<New Task>";
                        vals=new ArrayList(Arrays.asList(str.split("\n")));
                        setCurTasks();
                        main.msg("Pull OK");
                        showList();
                    }
                } catch (Exception e) {
                    msg("Problem with gitlab pull");
                    System.out.println("GITLAB KO ");
                    e.printStackTrace();
                }
            }
        });
        task.execute();
    }
        
 
}

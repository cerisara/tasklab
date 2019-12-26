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
import cz.msebera.android.httpclient.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import com.loopj.android.http.*;
import java.util.ArrayList;

public class TaskLabAct extends FragmentActivity {
	private static String gitlabkey = "";
	private static String gitlaburl = "";
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

        String k=PrefUtils.getFromPrefs(ctxt, "TASKLABKEY","");
        if (k.equals("")) askCreds(null);
        else {
            gitlabkey=k;
            gitlaburl=PrefUtils.getFromPrefs(ctxt, "TASKLABURL","");
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
				TextView glabtok = (TextView) dialog.findViewById(R.id.usernametok);
				glaburl.setText(gitlaburl);
				glabtok.setText(gitlabkey);
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
				TextView glaburl = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernameurl);
				TextView glabtok = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernametok);
				glaburl.setText(gitlaburl);
				glabtok.setText(gitlabkey);
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
				builder.setView(inflater.inflate(R.layout.dialog_signin, null))
					// Add action buttons
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							TextView glaburl = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernameurl);
							TextView glabtok = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.usernametok);
							gitlabkey = glabtok.getText().toString();
							gitlaburl = glaburl.getText().toString();
							PrefUtils.saveToPrefs(ctxt,"TASKLABKEY",gitlabkey);
							PrefUtils.saveToPrefs(ctxt,"TASKLABURL",gitlaburl);
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
							gitlabkey = glabtok.getText().toString();
							gitlaburl = glaburl.getText().toString();
							PrefUtils.saveToPrefs(ctxt,"ZIMBRAUSR",gitlabkey);
							PrefUtils.saveToPrefs(ctxt,"ZIMBRAPWD",gitlaburl);
                            zimbrausr = gitlabkey;
                            zimbrapwd = gitlaburl;
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
            new AlertDialog.Builder(this)
                .setTitle("Download tasks")
                .setMessage("Do you really want to erase your tasks with tasks from the server? (the text from share will be kept)")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DetProgressTask dett = new DetProgressTask(0,"");
                        dett.execute();
                    }})
            .setNegativeButton(android.R.string.no, null).show();
        }
	}
	public void reset(View view) {
        if (zimbramode) {
            askCredsZimbra();
            return;
        }
		//PrefUtils.saveToPrefs(ctxt,"TASKLABKEY","");
		//gitlabkey="";
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
					System.out.println("ON FAILURE3 "+gitlabkey);
				}

				@Override
				public void onRetry(int retryNo) {
					// called when request is retried
					System.out.println("ON RETRY");
				}
			};

			if (typ==1) client.put(gitlaburl+"?private_token="+gitlabkey+"&ref=master&branch=master&content="+url+"&commit_message=update%20file", rephdl);
			else client.get(gitlaburl+"?private_token="+gitlabkey+"&ref=master&branch=master", rephdl);
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

}

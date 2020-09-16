package fr.xtof.tasklab;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;
import android.widget.EditText;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.text.Editable;
import android.text.Selection;

public class TaskLabAct extends FragmentActivity {
    public static final String serverurl = "http://tasklab.cerisara.fr";

    private static String serverpwd = "";

    public static Context ctxt;
    public static TaskLabAct main;

    ListView listView;
    TaskClient model;

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

        model = new TaskClient(serverpwd, new TaskClient.ListShower() {
            public void showList() {
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView = (ListView) findViewById(R.id.list);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctxt, android.R.layout.simple_list_item_1, model.vals);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                model.select(Integer.toString(position));
                            }
                        });
                    }
                });
            }

            public void msg(String s) {
                main.msg(s);
            }
        });

        Intent in = this.getIntent();
        if (in.getAction().equals(Intent.ACTION_SEND)) getNewStringFromShareMenu(in);
    }

    public void msg(final String s) {
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
            model.save(s);
            msg("Share OK "+s.length());
        } else {
            msg("WARNING: nothing to share");
        }
    }

    @Override
    public void onNewIntent(Intent in) {
        System.out.println("ONNEWINTENT "+in.toString());
        if (in.getAction().equals(Intent.ACTION_SEND)) getNewStringFromShareMenu(in);
    }

    private void editText() {
        class LoginDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View custv = inflater.inflate(R.layout.dialog_edit, null);
                TextView txt = (TextView)custv.findViewById(R.id.taskdef);
                txt.setText("");

                builder.setView(custv).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView txt = (TextView) LoginDialogFragment.this.getDialog().findViewById(R.id.taskdef);
                            String s = txt.getText().toString();
                            // TODO
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
        dialog.show(getSupportFragmentManager(),"edit text");
    }

    // 1er bouton
    public void menu(View v) {
        model.menu();
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
        model.save("");
    }
}

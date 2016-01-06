package com.example.rishav.destrangify;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.parse.ParseException;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONObject;

public class my_profile extends AppCompatActivity {

    private People currentuser;
    private Switch visibility_switch;

    private TextView name_view;
    private TextView email_view;
    private TextView address_view;
    private TextView movies_view;
    private TextView songs_view;

    private EditText name_edit;
    private EditText email_edit;
    private EditText address_edit;
    private EditText movies_edit;
    private EditText songs_edit;

    private Button name_edit_btn;
    private Button email_edit_btn;
    private Button address_edit_btn;
    private Button movies_edit_btn;
    private Button songs_edit_btn;

    private ViewSwitcher songs_switch;
    private ViewSwitcher name_switch;
    private ViewSwitcher email_switch;
    private ViewSwitcher address_switch;
    private ViewSwitcher movies_switch;

    private Switch visiblity_switch;

    private String movies;
    private String songs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        currentuser = (People)People.getCurrentUser();
        if (currentuser==null)
            finish();

        visibility_switch=(Switch)findViewById(R.id.visibility_switch);

        name_view=(TextView)findViewById(R.id.text_name);
        email_view=(TextView)findViewById(R.id.text_email);
        address_view=(TextView)findViewById(R.id.text_address);
        movies_view=(TextView)findViewById(R.id.text_movies);
        songs_view=(TextView)findViewById(R.id.text_songs);

        name_edit=(EditText)findViewById(R.id.edit_name);
        email_edit=(EditText)findViewById(R.id.edit_email);
        address_edit=(EditText)findViewById(R.id.edit_address);
        movies_edit=(EditText)findViewById(R.id.edit_movies);
        songs_edit=(EditText)findViewById(R.id.edit_songs);

        name_edit_btn=(Button)findViewById(R.id.edit_name_btn);
        email_edit_btn=(Button)findViewById(R.id.edit_email_btn);
        address_edit_btn=(Button)findViewById(R.id.edit_address_btn);
        movies_edit_btn=(Button)findViewById(R.id.edit_movies_btn);
        songs_edit_btn=(Button)findViewById(R.id.edit_songs_btn);

        songs_switch=(ViewSwitcher)findViewById(R.id.songs_switch);
        name_switch = (ViewSwitcher) findViewById(R.id.name_switch);
        email_switch=(ViewSwitcher)findViewById(R.id.email_switch);
        address_switch=(ViewSwitcher)findViewById(R.id.address_switch);
        movies_switch=(ViewSwitcher)findViewById(R.id.movies_switch);


        visibility_switch.setChecked(currentuser.isLocVis());

        if(currentuser.getName()!=null) {
            name_view.setText(currentuser.getName());
        }
        else {
            name_view.setText("Enter Name");
        }
        email_view.setText(currentuser.getEmail());
        if(currentuser.getAddress()!=null) {
            address_view.setText(currentuser.getAddress());
        }
        else {
            address_view.setText("Enter Address");
        }
        showList(currentuser.getMovies(), movies_view);
        showList(currentuser.getSongs(), songs_view);


        visibility_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentuser.setLocVis(isChecked);
                currentuser.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        createAndShowDialog("Saved visibility","Alert");
                    }
                });
            }
        });

        name_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name_edit.setText(currentuser.getName());
                name_switch.showNext();
            }
        });
        email_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_edit.setText(currentuser.getEmail());
                //email_switch.showNext();
            }
        });
        address_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address_edit.setText(currentuser.getAddress());
                address_switch.showNext();
            }
        });
        movies_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movies_edit.setText("Enter Movie");
                movies_switch.showNext();
            }
        });
        songs_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songs_edit.setText("Enter Song");
                songs_switch.showNext();
            }
        });

        name_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentuser.setName(name_edit.getText().toString());
                currentuser.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        createAndShowDialog("Saved Name","Alert");
                    }
                });
                name_view.setText(currentuser.getName());
                name_switch.showNext();
            }
        });
        email_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentuser.setEmail(email_edit.getText().toString());
                currentuser.saveEventually();
                email_view.setText(currentuser.getEmail());
                email_switch.showNext();
            }
        });
        address_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentuser.setAddress(address_edit.getText().toString());
                currentuser.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        createAndShowDialog("Saved Address","Alert");
                    }
                });
                address_view.setText(currentuser.getAddress());
                address_switch.showNext();
            }
        });
        movies_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentuser.addMovie(movies_edit.getText().toString());
                currentuser.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        createAndShowDialog("Saved Movies","Alert");
                    }
                });
                showList(currentuser.getMovies(),movies_view);
                movies_switch.showNext();
            }
        });
        songs_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentuser.addSong(songs_edit.getText().toString());
                currentuser.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        createAndShowDialog("Saved Songs","Alert");
                    }
                });
                showList(currentuser.getSongs(),songs_view);
                songs_switch.showNext();
            }
        });




    }

    public void showList(JSONArray ar,TextView tv) {
        String item;
        String str="";
        if (ar!=null) {
            for (int i = 0; i < ar.length(); i++) {
                try {
                    item = ar.getString(i);
                    str = str + item + "\n";
                } catch (Exception e) {
                    createAndShowDialog(e, "Error");
                }
            }
            if(str!="")
                tv.setText(str);
            else
                tv.setText("None Added Yet");
        }
        else
            tv.setText("None Added Yet");
    }

    public void add_interest(View view){
        LinearLayout ll = (LinearLayout)findViewById(R.id.my_interest_list);
        TextView tv_title = new TextView(this);
        tv_title.setText("New interest");
        tv_title.setTextAppearance(this, R.style.p_subtitle);
        ll.addView(tv_title);
        TextView tv_text = new TextView(this);
        tv_text.setText("Description here");
        tv_text.setTextAppearance(this, R.style.p_text_small);
        ll.addView(tv_text);
    }

    public void save_changes(View view){
        Intent intent_dashboard = new Intent(this, Dashboard.class);
        startActivity(intent_dashboard);
    }

    private void createAndShowDialog(Exception exception, String title) {
        createAndShowDialog(exception.toString(), title);
    }


    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

}
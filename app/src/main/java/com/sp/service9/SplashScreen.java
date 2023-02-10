package com.sp.service9;

/**
 * Created by StevenPC on 3/3/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.sp.moveee.R;

public class SplashScreen extends AppCompatActivity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    String savedname;
    String name = "";

    Button next, gameform;
    EditText namebox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        sharedPreferences = getSharedPreferences("counter", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        next = (Button) findViewById(R.id.next);
        gameform = (Button) findViewById(R.id.gameform);

        namebox = (EditText) findViewById(R.id.nameenter);

        Window window = getWindow();

        name = sharedPreferences.getString("KEY", "");

        namebox.setText(name);

        if (namebox.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a name...", Toast.LENGTH_SHORT).show();
        }

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (namebox.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a name...", Toast.LENGTH_SHORT).show();
                } else {

                    savedname = namebox.getText().toString();
                    editor.putString("KEY", savedname);
                    editor.commit();

                    Intent i = new Intent(SplashScreen.this, Pedometer.class);
                    startActivity(i);
                }


            }

        });

        gameform.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (namebox.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a name...", Toast.LENGTH_SHORT).show();
                } else {

                    savedname = namebox.getText().toString();
                    editor.putString("KEY", savedname);
                    editor.commit();

                    Intent i = new Intent(SplashScreen.this, GameForm.class);
                    startActivity(i);
                }

            }

        });





    }
}
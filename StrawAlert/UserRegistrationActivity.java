package de.tum.mw.ftm.praktikum.androidapp_strohhalm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Dominik on 07.01.2017.
 */

public class UserRegistrationActivity extends AppCompatActivity {

    private EditText vorname;
    private EditText nachname;
    private EditText passwort;
    private Button btnAbbrechen;
    private Button btnRegistrieren;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        btnAbbrechen = (Button) findViewById(R.id.cancel_btn);
        btnAbbrechen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserRegistrationActivity.this, LoginActivity.class);
                UserRegistrationActivity.this.startActivity(intent);
            }
        });

        btnRegistrieren = (Button) findViewById(R.id.reg_btn);
        btnRegistrieren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UserRegistrationActivity.this, MainActivity.class);
                UserRegistrationActivity.this.startActivity(intent);
            }
        });
    }
}

package de.tum.mw.ftm.praktikum.androidapp_strohhalm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.util.HashMap;



import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.AppConfig;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.User;


/**
 * Created by Dominik on 04.01.2017.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button btnLogin;
    private Button btnNewUser;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        SharedPreferences prefs = getApplicationContext().getSharedPreferences(User.PREFGROUP_USER, MODE_PRIVATE);
        if (prefs.getBoolean(User.PREF_IS_LOGGED_IN, false)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }


        username = (EditText) findViewById(R.id.log_user);
        password = (EditText) findViewById(R.id.log_pw);

        btnLogin = (Button) findViewById(R.id.log_btn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = username.getText().toString().trim();
                String pw = password.getText().toString().trim();
                login(user, pw);

            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public void register(String id, String password){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params  = new RequestParams();
        HashMap<String,String> upload = new HashMap<String, String>();
        upload.put("u_id", id);
        upload.put("u_pw", password);
        Gson gson = new GsonBuilder().create();
        params.put("register", gson.toJson(upload));
                client.post(AppConfig.URL_Register, params, new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println(arr.length());
                            String helper = "error";
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                helper = obj.get("u_id").toString();
                            }
                            Toast.makeText(getApplicationContext(), "User registered! " + helper, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        if(statusCode == 404){
                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void login(String id, String password){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params  = new RequestParams();
        HashMap<String,String> login = new HashMap<String, String>();
        login.put("u_id", id);
        login.put("u_pw", password);
        Gson gson = new GsonBuilder().create();
        params.put("login", gson.toJson(login));
        client.post(AppConfig.URL_Login, params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                System.out.println(response);
                try {
                    String response2 = response.substring(response.indexOf("?>")+2);
                    JSONArray arr = new JSONArray(response2);
                    System.out.println(arr.length());
                    String helper = "error";
                    for(int i=0; i<arr.length();i++){
                        JSONObject obj = (JSONObject)arr.get(i);
                        helper = obj.get("status").toString();
                        if(helper.equals("true")){
                            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(User.PREFGROUP_USER,MODE_PRIVATE).edit();
                            editor.putString(User.PREF_USERID, obj.get("u_id").toString()).apply();
                            editor.putBoolean(User.PREF_IS_LOGGED_IN, true).apply();
                            //Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            LoginActivity.this.startActivity(intent);
                        }else {
                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_data), Toast.LENGTH_LONG).show();

                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.JSON_error_login), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), getString(R.string.adress_error), Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
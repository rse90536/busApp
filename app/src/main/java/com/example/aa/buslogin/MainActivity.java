package com.example.aa.buslogin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.HandlerThread;
import android.renderscript.Element;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static android.R.attr.value;
import static java.nio.charset.StandardCharsets.*;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    Button btnlogin;
    EditText editacc, editpw;
    boolean data = false;
    //調用webservice 近回的資料,驗證成功true,失敗false
    Thread thread;

    String account = "";
    //使用者名
    String password = "";
    String setting = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // getUserInfo();


        btnlogin = (Button) findViewById(R.id.button);
        editacc = (EditText) findViewById(R.id.editText);
        editpw = (EditText) findViewById(R.id.editText2);

        btnlogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                account = editacc.getText().toString();
                password = editpw.getText().toString();
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("account", account)
                        .add("password", password)
                        .build();
                final Request request = new Request.Builder()
                        .url("http://busdriver.azurewebsites.net/api/Loginapi/login")
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final int StatusCode = response.code();
                        final String ResMsg = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // String ticket_num = null;

                                //  String wa="wrong account";
                                // String wp="wrong password";
                                // ticket_num = response.body().string();

                                if (StatusCode == 200) {
                                    String Ticket = new Gson().fromJson(ResMsg, String.class);
                                    SharedPreferences sharedPreferences = getSharedPreferences("Ticket", Context.MODE_PRIVATE);
                                    sharedPreferences.edit().putString("Ticket", "DriverNumber=" + Ticket).apply();/*存入ticket */
                                    //setting = sharedPreferences.getString("Ticket",Ticket);

                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, SignalRActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("account", account);
                                    intent.putExtras(bundle);

                                    startActivityForResult(intent, 87);
                                } else {
                                    // Intent intent = new Intent();
                                    // intent.setClass(MainActivity.this, SignalRActivity.class);
                                    //  startActivity(intent);
                                    Toast.makeText(MainActivity.this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                                }
                            }

                        });


                    }
                });


            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 87 && resultCode == RESULT_OK) {
            finish();
        }
    }

    public void getUserInfo() {
        String Ticket = getSharedPreferences("Ticket", Context.MODE_PRIVATE)
                .getString("Ticket", "");
        if (!Ticket.equals("")) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://busdriver.azurewebsites.net/api/Loginapi/getuserinfo")
                    .addHeader("Cookie", Ticket)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final int StatusCode = response.code();
                    final String ResMsg = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // String ticket_num = null;

                            //  String wa="wrong account";
                            // String wp="wrong password";
                            // ticket_num = response.body().string();

                            if (StatusCode == 200) {

                                Intent intent = new Intent();

                                intent.setClass(MainActivity.this, SignalRActivity.class);
                                startActivityForResult(intent, 87);
                            } else {
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                Toast.makeText(MainActivity.this, "請重新登入", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });


                }
            });
        }
    }


}
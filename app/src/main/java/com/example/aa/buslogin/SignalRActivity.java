package com.example.aa.buslogin;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ViewModels.NewStopView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.data;
import static android.R.attr.sharedUserId;

public class SignalRActivity extends AppCompatActivity {
    TextView tv, tv3, tt, t1;
    private boolean bound = false;
    private boolean bound2 = false;
    private boolean bound3 = false;
    private SignalRService signalrService;
    private DSignalRService dsignalrService;
    private GoUpService gsignalrService;

    public MyAdapter mAdapter;
    public RecyclerView mRecyclerView;
    String account, busbn;
    //    ArrayList<String> myDataset = new ArrayList<>();
    ArrayList<String> gostopname = new ArrayList<>();
    ArrayList<String> bkstopname = new ArrayList<>();

//    ArrayList<String> gomystop = new ArrayList<String>();
//    ArrayList<String> bkmystop = new ArrayList<String>();

    public NewStopView newStopView = new NewStopView();

    String GoBack, seqNo, Stopname;

    /*計時*/
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_r);
        Bundle bundle = getIntent().getExtras();
        account = bundle.getString("account");
        t1 = (TextView) findViewById(R.id.textView);
        tv = (TextView) findViewById(R.id.textView3);
        mAdapter = new MyAdapter();
        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(SignalRActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //取車號
        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("account", account)
                .build();
        final Request request = new Request.Builder()
                .url("http://busdriver.azurewebsites.net/api/Loginapi/getDriverbn")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatCode = response.code();
                final String bunMsg = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatCode == 200) {
                            busbn = new Gson().fromJson(bunMsg, String.class);
//                            Toast.makeText(SignalRActivity.this, busbn, Toast.LENGTH_LONG).show();
                            //取得公車所有站牌
                            new RunWork().start();
                        } else {
                            Toast.makeText(SignalRActivity.this, "888", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.logout);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SignalRActivity.this)
                        .setMessage("確定要登出嗎?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                clearUserInfo();

                                finish();

                            }
                        })
                        .setNeutralButton("取消", null)
                        .show();

            }
        });
        tt = (TextView) findViewById(R.id.info_text);
        BindSignalr();
        BindDSignalr();
        BindGSignalr();

        /*計時*/
        timer = new Timer();
        setTimerTask();

    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.clean:
//                // About option clicked.777777777777777
//                signalrService.newmystop.clear();
//                gomystop.clear();
//                bkmystop.clear();
//                gostopname.clear();
//                bkstopname.clear();
//                t1.setText("");
//                tv.setText("");
//                new RunWork().start();
//
//                mAdapter.notifyDataSetChanged();
//
//                Toast.makeText(SignalRActivity.this, "已清空", Toast.LENGTH_LONG).show();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.info_text);
            }
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(newStopView.NewStopList.get(position).StopName+" "+newStopView.NewStopList.get(position).count+"人");
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(SignalRActivity.this, "Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();
//                }
//            });
//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    Toast.makeText(SignalRActivity.this, "Item " + position + " is long clicked.", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return newStopView.NewStopList.size();
        }
    }

    public void onclick(View v) {
        clearUserInfo();
        finish();
    }

    private void clearUserInfo() {
        getSharedPreferences("Ticket", MODE_PRIVATE).edit().putString("Ticket", "").apply();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, SignalRService.class);
        Intent dintent = new Intent(this, DSignalRService.class);
        Intent gintent = new Intent(this, GoUpService.class);
        stopService(intent);
        stopService(dintent);
        stopService(gintent);
        super.onDestroy();
        /*計時*/
        timer.cancel();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setMessage("確定要離開嗎?")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .setNeutralButton("取消", null)
                .show();
    }

    private void BindSignalr() {
        Intent intent = new Intent(this, SignalRService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void BindDSignalr() {
        Intent intent = new Intent(this, DSignalRService.class);
        startService(intent);
        bindService(intent, serviceConnection2, Context.BIND_AUTO_CREATE);
    }

    private void BindGSignalr() {
        Intent intent = new Intent(this, GoUpService.class);
        startService(intent);
        bindService(intent, serviceConnection3, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;

            signalrService = binder.getService();
            bound = true;
            signalrService.setSignalRActivity(SignalRActivity.this); // register
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private ServiceConnection serviceConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DSignalRService.LocalBinder binder = (DSignalRService.LocalBinder) service;
            dsignalrService = binder.getService();
            bound2 = true;
            dsignalrService.setDSignalRActivity(SignalRActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg01) {
            bound2 = false;
        }
    };

    private ServiceConnection serviceConnection3 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GoUpService.LocalBinder binder = (GoUpService.LocalBinder) service;
            gsignalrService = binder.getService();
            bound3 = true;
            gsignalrService.setGSignalRActivity(SignalRActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg02) {
            bound3 = false;
        }
    };

    //取得公車資訊 okhttp抓xml
    private class RunWork extends Thread {
        //宣告變數
        String result_xml = "";
        String path = "http://163.17.135.161/bus/route/" + busbn + "/time";
        Runnable task = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                parseXml(result_xml);
            }
        };
        //OkHttpClient官網範例使用
        OkHttpClient client = new OkHttpClient();

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        public void run() {
            try {
                //1.抓資料
                result_xml = run(path);
                //2.改變畫面內容只能用主執行緒(Android機制)
                runOnUiThread(task);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parseXml(String result_xml) {
        try {
            //取得XML根節點
            InputStream is = new ByteArrayInputStream(result_xml.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            org.w3c.dom.Element root = document.getDocumentElement();
            //取得 TagName 裡的內容
//            String UpdateTime=root.getElementsByTagName("UpdateTime").item(0).getTextContent();
            //取得 Node List
            NodeList EstimateTime = root.getElementsByTagName("EstimateTime");
            Bundle bundle = new Bundle();
            //取得 Node
            for (int i = 0; i < EstimateTime.getLength(); i++) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) EstimateTime.item(i);
                String StopName = element.getAttribute("StopName");
                GoBack = element.getAttribute("GoBack");
                if (GoBack.equals("1")) {
                    gostopname.add(StopName);
                } else if (GoBack.equals("2")) {
                    bkstopname.add(StopName);
                }
//                Toast.makeText(MainActivity.this,StopName,Toast.LENGTH_LONG).show();
            }
//            int gosize = gostopname.size();
//            for (int i = 0; i < gosize - 1; i++) {
//                gomystop.add(i, "aaa");
//            }
//            int bksize = bkstopname.size();
//            for (int i = 0; i < bksize - 1; i++) {
//                bkmystop.add(i, "aaa");
//            }
//            if(GoBack.equals("2")) {
//                int size = gostopname.size();
//                for (int i = 0; i < size - 1; i++) {
//                    mystop.add(i, "aaa");
//                }
//                GoBack .equals("1");
//            } if(GoBack.equals("1")){
//                int size = bkstopname.size();
//                for (int i = 0; i < size - 1; i++) {
//                    mystop.add(i, "aaa");
//                }
//            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getacc(String sharedacc) {
        String useraccount = getSharedPreferences("Ticket", Context.MODE_PRIVATE)
                .getString("DriverNumber=", "");
        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("Ticket", useraccount)
                .build();
        final Request request = new Request.Builder()
                .url("http://busdriver.azurewebsites.net/api/Loginapi/getDriverbn")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatCode = response.code();
                final String bunMsg = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });

            }
        });
    }


    //取得公車資訊 okhttp抓xml
    private class BusRunWork extends Thread {
        //宣告變數
        String result_xml = "";
        String path = "http://163.17.135.161/bus/route/" + busbn + "/time";
        Runnable task = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                parse2Xml(result_xml);
            }
        };
        //OkHttpClient官網範例使用
        OkHttpClient client = new OkHttpClient();

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        public void run() {
            try {
                //1.抓資料
                result_xml = run(path);
                //2.改變畫面內容只能用主執行緒(Android機制)
                runOnUiThread(task);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parse2Xml(String result_xml) {
        try {
            //取得XML根節點
            InputStream is = new ByteArrayInputStream(result_xml.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            org.w3c.dom.Element root = document.getDocumentElement();
            //取得 TagName 裡的內容
//            String UpdateTime=root.getElementsByTagName("UpdateTime").item(0).getTextContent();
            //取得 Node List
            NodeList EstimateTime = root.getElementsByTagName("EstimateTime");
            //取得 Node
            for (int i = 0; i < EstimateTime.getLength(); i++) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) EstimateTime.item(i);
                String carId = element.getAttribute("carId");
                if (carId.equals(account)) {
                    seqNo = element.getAttribute("seqNo");
                    GoBack = element.getAttribute("GoBack");
                    break;
                }
            }
            if (seqNo != null) {
                for (int i = 0; i < EstimateTime.getLength(); i++) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) EstimateTime.item(i);
                    String sn = element.getAttribute("seqNo");
                    String gb = element.getAttribute("GoBack");
                    if (sn.equals(String.valueOf((Integer.parseInt(seqNo) - 1))) && gb.equals(GoBack)) {
                        Stopname = element.getAttribute("StopName");
                    }
                }

                for (int i = 0; i < newStopView.NewStopList.size(); i++) {
                    //如果發現站名在畫面上直接刪除跳出
                    if (Stopname.equals(newStopView.NewStopList.get(i).StopName)) {
                        newStopView.NewStopList.remove(i);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            seqNo = null;
//            for (int i = 0; i < signalrService.newmystop.size(); i++) {
//                String name = signalrService.newmystop.get(i);
//                if (name.equals(Stopname)) {
//                    signalrService.newmystop.remove(i);
//                    i--;
//                }
//            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*計時*/
    private void setTimerTask() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                doActionHandler.sendMessage(message);
            }
        }, 20000, 20000/* 表示1000毫秒之後，每隔1000毫秒執行一次 */);
    }

    /**
     * do some action
     */
    private Handler doActionHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgId = msg.what;
            switch (msgId) {
                case 1:
                    new BusRunWork().start();
                    break;
                default:
                    break;
            }
        }
    };
}

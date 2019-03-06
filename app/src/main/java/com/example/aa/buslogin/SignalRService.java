package com.example.aa.buslogin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


import MyMethod.MyCookieCredentials;
import ViewModels.NewStopView;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;


public class SignalRService extends Service {
    private HubConnection connection = null;
    private HubProxy mHub = null;
    private final IBinder mBinder = new LocalBinder();/*綁定ACTIVITY並做溝通*/
    private SignalRActivity signalRActivity;
    private Vibrator vibrator;

    //    private MyAdapter mAdapter;
    int idx;
    ArrayList<String> newmystop = new ArrayList<>();


    public SignalRService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Log.d("test : ", "onStartCommand");
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (connection != null) {
            connection.stop();
            connection = null;
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        Log.d("test : ", "onBind");
        if (connection == null) {
            UseSignalR();

            Log.d("test : ", "onBindUseSignalR");
        }
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }


    private void UseSignalR() {
        Log.d("test : ", "UseSignalR");
        final String HUB_URL = "http://busdriver.azurewebsites.net/signalr";
        final String HUB_NAME = "serverhub";
        SignalRFuture<Void> mSignalRFuture;
        if (connection != null) {
            connection.stop();
            connection = null;
            mHub = null;
        }
        connection = new HubConnection(HUB_URL);
        mHub = connection.createHubProxy(HUB_NAME);
        connection.setCredentials(new MyCookieCredentials(this));
        mSignalRFuture = connection.start(new ServerSentEventsTransport(connection.getLogger()));


        mHub.on("sendMessage", new SubscriptionHandler2<String, String>() {

            @Override
            public void run(final String stopname, final String goback) {

                vibrator.vibrate(1000);
                signalRActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int count=1;
                        for(int i = 0; i < signalRActivity.newStopView.NewStopList.size(); i++){
                            if (signalRActivity.newStopView.NewStopList.get(i).StopName.contains(stopname)){
                                count=signalRActivity.newStopView.NewStopList.get(i).count+1;
                                signalRActivity.newStopView.NewStopList.set(i,new NewStopView.NewStop(signalRActivity.newStopView.NewStopList.get(i).seqNo,signalRActivity.newStopView.NewStopList.get(i).StopName,count));
                                signalRActivity.mAdapter.notifyDataSetChanged();
                            }
                        }
                        //抓站名在第幾站
                        if (goback.equals("1"))
                            idx = signalRActivity.gostopname.indexOf(stopname);
                        else
                            idx = signalRActivity.bkstopname.indexOf(stopname);

                        //如果現在畫面上是空的直接加入
                        if (signalRActivity.newStopView.NewStopList.size() == 0) {

                            signalRActivity.newStopView.NewStopList.add(new NewStopView.NewStop(idx, stopname,count));
                            signalRActivity.mAdapter.notifyDataSetChanged();
                            signalRActivity.tv.setText(stopname + ":" + "有人要上車");
                        } else {
                            //否則進去List比較index
                            for (int i = 0; i < signalRActivity.newStopView.NewStopList.size(); i++) {
                                //如果發現重複直接跳出迴圈
                                if (idx == signalRActivity.newStopView.NewStopList.get(i).seqNo) {
                                    break;
                                }

                                //找到當前index小於的站後插入
                                //例如目前index有 2(0) 5(1) 9(2)  這時候來了7 ，迴圈跑到7<9就會插入到(2)，將9擠到(3)
                                if (idx < signalRActivity.newStopView.NewStopList.get(i).seqNo) {

                                    signalRActivity.newStopView.NewStopList.add(i, new NewStopView.NewStop(idx, stopname,count));
                                    signalRActivity.mAdapter.notifyDataSetChanged();
                                    signalRActivity.mRecyclerView.scrollToPosition(i);
                                    signalRActivity.tv.setText(stopname + ":" + "有人要上車");
                                    break;
                                } else if (i == signalRActivity.newStopView.NewStopList.size() - 1) {
                                    signalRActivity.newStopView.NewStopList.add(i + 1, new NewStopView.NewStop(idx, stopname,count));
                                    signalRActivity.mAdapter.notifyDataSetChanged();
                                    signalRActivity.mRecyclerView.scrollToPosition(i + 1);
                                    signalRActivity.tv.setText(stopname + ":" + "有人要上車");
                                    break;
                                }
                            }
                        }

                        //signalRActivity.tv.setText(stopname);

//                        boolean empty = mystop.isEmpty();


//                        if (goback.equals("1")) {
//
//                            idx = signalRActivity.gostopname.indexOf(stopname);
//
//                            if (signalRActivity.newStopView.NewStopList.size() == 0){
//                                signalRActivity.newStopView.NewStopList.add(new NewStopView.NewStop(idx, stopname));
//                                signalRActivity.tv.setText(stopname + ":" + "有人要上車");
//                            }
//                            else {
//                                for (int i = 0; i < signalRActivity.newStopView.NewStopList.size(); i++) {
//                                    if (idx == signalRActivity.newStopView.NewStopList.get(i).seqNo) {
//                                        break;
//                                    }
//
//                                    if (idx < signalRActivity.newStopView.NewStopList.get(i).seqNo) {
//                                        signalRActivity.newStopView.NewStopList.add(i, new NewStopView.NewStop(idx, stopname));
//                                        signalRActivity.tv.setText(stopname + ":" + "有人要上車");
//                                    }
//                                }
//                            }
////                            signalRActivity.myDataset = signalRActivity.gomystop;
//
//
////                            newmystop.clear();
////                            for (int j = 0; j < signalRActivity.gomystop.size(); j++) {
////                                String s = signalRActivity.gomystop.get(j);//25
////                                newmystop.add(j, s);
////                            }
//////                        int size=newmystop.size();
////                            for (int i = 0; i < newmystop.size(); i++) {
////                                String aaa = newmystop.get(i);
////                                if (aaa.equals("aaa")) {
////                                    newmystop.remove(i);
////                                    i--;
////                                }
////                            }
////
////                            mAdapter = new MyAdapter(newmystop);
//////                        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
////                            final LinearLayoutManager layoutManager = new LinearLayoutManager(SignalRService.this);
////                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
////                            signalRActivity.mRecyclerView.setLayoutManager(layoutManager);
////                            signalRActivity.mRecyclerView.setAdapter(mAdapter);
//
//
//                        } else if (goback.equals("2")) {
//                            boolean result = signalRActivity.bkmystop.contains(stopname);
//                            if (!result) {
//                                idx = signalRActivity.bkstopname.indexOf(stopname);
//                                signalRActivity.bkmystop.add(idx, stopname);
//                                signalRActivity.tv.setText(stopname + ":" + "有人要上車");
//                            }
////                            signalRActivity.myDataset = signalRActivity.bkmystop;
//
//                            newmystop.clear();
//                            for (int j = 0; j < signalRActivity.bkmystop.size(); j++) {
//                                String s = signalRActivity.bkmystop.get(j);
//                                newmystop.add(j, s);
//                            }
////                        int size=newmystop.size();
//                            for (int i = 0; i < newmystop.size(); i++) {
//                                String aaa = newmystop.get(i);
//                                if (aaa.equals("aaa")) {
//                                    newmystop.remove(i);
//                                    i--;
//                                }
//                            }
//
//                            mAdapter = new MyAdapter(newmystop);
////                        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
//                            final LinearLayoutManager layoutManager = new LinearLayoutManager(SignalRService.this);
//                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                            signalRActivity.mRecyclerView.setLayoutManager(layoutManager);
//                            signalRActivity.mRecyclerView.setAdapter(mAdapter);
//
//                        }


//                        newmystop=signalRActivity.mystop;


                    }
                });


                Intent intent = new Intent(SignalRService.this, SignalRActivity.class);
                intent.putExtra("account", signalRActivity.account);
                // intent.putExtra("FromNotifi", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(/*點擊通知跳轉畫面*/
                        SignalRService.this,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI
//                   String Content = groupMessageView.GroupMessageList.get(0).Content;
//                    if (Content.length() > 12)
//                        Content = Content.substring(0, 12) + "...";
                int c = 0;
                c += 1;
                Notification notification = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.stoppp)
                        .setContentTitle("有人要上車")
                        .setContentText(stopname + " : " + "有人要上車")
                        .setContentIntent(pendingIntent)
                        .setSound(soundUri)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(c, notification);

            }
        }, String.class, String.class);


        //開啟連線
        try {
            mSignalRFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void setSignalRActivity(SignalRActivity activity) {
        signalRActivity = activity;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.info_text);
            }
        }

        public MyAdapter(List<String> data) {
            mData = data;
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
            holder.mTextView.setText(mData.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(MainActivity.this, "Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    Toast.makeText(MainActivity.this, "Item " + position + " is long clicked.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }
}

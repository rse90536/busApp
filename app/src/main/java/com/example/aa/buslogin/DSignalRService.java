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
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import MyMethod.MyCookieCredentials;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;
import okhttp3.OkHttpClient;



public class DSignalRService extends Service {
    private HubConnection connection = null;
    private HubProxy mHub = null;
    private final IBinder mBinder = new DSignalRService.LocalBinder();/*綁定ACTIVITY並做溝通*/
    private SignalRActivity signalRActivity;
    private MainActivity mainActivity;
private BlankFragment blankFragment;
    private OkHttpClient client;
    private Vibrator vibrator;

    public DSignalRService() {
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
        public DSignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return DSignalRService.this;
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


        mHub.on("sendMessage", new SubscriptionHandler1<String>() {

            @Override
            public void run(final String stopname) {

                vibrator.vibrate(1000);
                signalRActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        signalRActivity.t1.setText(stopname+":"+"有人要下車");

//                        boolean empty = mystop.isEmpty();
                Log.d("TAG",stopname);


                    }
                });


                Intent intent = new Intent(DSignalRService.this, SignalRActivity.class);
                // intent.putExtra("FromNotifi", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(/*點擊通知跳轉畫面*/
                        DSignalRService.this,
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
                        .setContentTitle("有人要下車")
                        .setContentText(stopname + " : " + "有人要下車")
                        .setContentIntent(pendingIntent)
                        .setSound(soundUri)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(c, notification);

            }
        }, String.class);


        //開啟連線
        try {
            mSignalRFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void setDSignalRActivity(SignalRActivity activity) {
        signalRActivity = activity;
    }

}

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
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler3;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;


public class GoUpService extends Service {
    private HubConnection connection = null;
    private HubProxy mHub = null;
    private final IBinder mBinder = new GoUpService.LocalBinder();/*綁定ACTIVITY並做溝通*/
    private SignalRActivity signalRActivity;
    private Vibrator vibrator;


    public GoUpService() {
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
        }
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public GoUpService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return GoUpService.this;
        }
    }

    private void UseSignalR() {
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


        mHub.on("sendMessage", new SubscriptionHandler3<String, String, String>() {

            @Override
            public void run(final String stopname, final String goback,final String up) {

                vibrator.vibrate(1000);
                signalRActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int count = 1;
                        for (int i = 0; i < signalRActivity.newStopView.NewStopList.size(); i++) {
                            if (signalRActivity.newStopView.NewStopList.get(i).StopName.contains(stopname)) {
                                count = signalRActivity.newStopView.NewStopList.get(i).count - 1;
                                signalRActivity.newStopView.NewStopList.set(i, new NewStopView.NewStop(signalRActivity.newStopView.NewStopList.get(i).seqNo, signalRActivity.newStopView.NewStopList.get(i).StopName, count));
                                signalRActivity.mAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });
            }
        }, String.class, String.class, String.class);


        //開啟連線
        try {
            mSignalRFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void setGSignalRActivity(SignalRActivity activity) {
        signalRActivity = activity;
    }


}


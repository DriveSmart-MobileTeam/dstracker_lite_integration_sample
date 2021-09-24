package com.ds.test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.drivesmartsdk.singleton.DSManager;


public class LiteServiceJava extends Service {
    public static final String CHANNEL_ID = "DSTracker channel";

    private DSManager dsManager;

    public LiteServiceJava(){ }

    @Override
    public void onCreate() {
        super.onCreate();

        dsManager = DSManager.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        String pmd = intent.getStringExtra("PMD");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivityJava.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("DSTracker Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_stat_routetracking)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        dsManager.startDecoupledService(pmd);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        startForceUpload();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "DSTracker Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startForceUpload(){
        dsManager.stopDecoupledService();
        dsManager.startManualUpload(this);
    }
}

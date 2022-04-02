package com.zlotluk.mapindicat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Map;

import logic.SQLiteDbEvent;
import logic.SQLiteDbFlag;
import model.Eventt;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private SQLiteDbEvent sqLe;
    private SQLiteDbFlag sqL;
    private static boolean mf = false;
    private static String lat, lng, m = "m", opis;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sqLe = new SQLiteDbEvent(this);
        sqL = new SQLiteDbFlag(this);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        int idd = Integer.parseInt(remoteMessage.getNotification().getTag());

        Map<String, String> extraData = remoteMessage.getData();

        String lat = extraData.get("lat");
        String lng = extraData.get("lng");

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "TAC")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.drawable.ic_launcher_background);

        if (sqLe.getAllId().contains(idd)) {
            sqLe.update(new Eventt(idd, body, lat, lng));
        } else {
            sqLe.create(new Eventt(idd, body, lat, lng));
        }

        PendingIntent pendingIntent;
        Intent intent;

        if (sqL.getAllFlag().get(0).getMap() == 1) {
            String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lng + " (" + body + ")";
            setM(uri);
        } else {
            setLat(lat);
            setLng(lng);
            setMf(true);
            setM("c");
        }

        setOpis(body);
        intent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        int id = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("TAC", "demo", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(id, notificationBuilder.build());
    }

    public static String getLat() {
        return lat;
    }

    public static void setLat(String lat) {
        MyFirebaseMessagingService.lat = lat;
    }

    public static String getLng() {
        return lng;
    }

    public static void setLng(String lng) {
        MyFirebaseMessagingService.lng = lng;
    }

    public static String getM() {
        return m;
    }

    public static void setM(String m) {
        MyFirebaseMessagingService.m = m;
    }

    public static String getOpis() {
        return opis;
    }

    public static boolean isMf() {
        return mf;
    }

    public static void setMf(boolean mf) {
        MyFirebaseMessagingService.mf = mf;
    }

    public static void setOpis(String opis) {
        MyFirebaseMessagingService.opis = opis;
    }

    class Coor {
        private String opis, lat, lng;

        Coor() {
        }

        Coor(String opis, String lat, String lng) {
            this.opis = opis;
            this.lat = lat;
            this.lng = lng;
        }

        public String getOpis() {
            return opis;
        }

        public void setOpis(String opis) {
            this.opis = opis;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }
}

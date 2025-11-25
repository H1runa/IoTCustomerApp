package com.hiruna.iotcustomerapp.fcm_service;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hiruna.iotcustomerapp.util.NotificationUtil;

import java.util.HashMap;

public class FCMService extends FirebaseMessagingService {

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//    @Override
//    public void onNewToken(@NonNull String token) {
//        super.onNewToken(token);
//
//        //saving to firebase
//        HashMap<String, String> data = new HashMap<>();
//        data.put("token", token);
//        firestore.collection("FCMToken").document("fcmtoken").set(data).addOnSuccessListener((nothing)->{
//            System.out.println("Saved FCM Token to Firestore");
//        }).addOnFailureListener((e)->{
//            System.err.println("ERROR : Failed to save FCM Token to Firestore");
//        });
//    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        System.out.println("MESSAGE RECEIVED");
//        System.out.println(message.getNotification().getTitle());
        NotificationCompat.Builder builder = NotificationUtil.getNotifBuilder(message.getNotification().getTitle(), message.getNotification().getBody(), this, "highPrioChannel");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            System.err.println("ERROR : No Notification perms");
            return;
        }
        NotificationManagerCompat.from(this).notify(101, builder.build());
    }
}

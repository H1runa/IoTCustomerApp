package com.hiruna.iotmobileapp;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotmobileapp.dialog.WaterDialog;
import com.hiruna.iotmobileapp.util.NotificationUtil;

public class MainActivity extends AppCompatActivity {

    private TextView containerLevel;
    private TextView drankAmount;
    private TextView amountLeft;
    private AtomicReference<Long> waterLimitVar = new AtomicReference<>(0L);
    private AtomicReference<Long> totalDrankAmountVar = new AtomicReference<>(0L);
    private static AlertDialog tds_warn_dialog;
    private static AlertDialog water_low_dialog;
    private static Snackbar tds_warn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //parameters button action
        MaterialButton paramsBtn = (MaterialButton) findViewById(R.id.paramsBtn);
        paramsBtn.setOnClickListener((view)->{
            startActivity(new Intent(this, ParametersActivity.class)); //starting system parameters activity
        });

        //analytics button action
        MaterialButton analBtn = (MaterialButton) findViewById(R.id.analyticsBtn);
        analBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, AnalyticsView.class));
        });

        //water intake
        MaterialButton waterBtn = (MaterialButton) findViewById(R.id.waterBtn);
        waterBtn.setOnClickListener(view -> {
            WaterDialog dialog = new WaterDialog();
            dialog.show(getSupportFragmentManager(), "testingtag");
        });

        //creating notification channel
        NotificationUtil.createNotificationChannel("notifChannel", "BasicNotifChannel", this, "highPrioChannel");

//        FirebaseMessaging.getInstance().getToken().addOnSuccessListener((token)->{
//            System.out.println("FCM Token : " + token);
//        });

        //in app notif
//        tds_warn_dialog = new AlertDialog.Builder(this)
//                .setTitle("The Water Quality is not drinkable")
//                .setMessage("Please change the water to unlock the app")
//                .setCancelable(false)
//                .create();

        //setting up tds warning
        tds_warn = getSnackBar("ℹ️ Water quality is not safe for consumption", "#A01010", Color.WHITE);

        water_low_dialog = new AlertDialog.Builder(this)
                .setTitle("Low Water Levels Detected")
                .setMessage("Please refill the container to unlock the app")
                .setCancelable(false)
                .create();

        containerLevel = findViewById(R.id.containerlevelvalue);
//        drankAmount = findViewById(R.id.drunkAmount);
//        amountLeft = findViewById(R.id.toDrinkAmount);

        //realtime db code
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://water-dispenser-7819c-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("notif/tds/badquality");
        DatabaseReference water_level_ref = db.getReference("notif/ultrasonic/waterlow");

        TextView waterQualityValue = findViewById(R.id.waterQualityValue);
        TextView containerAmountValue = findViewById(R.id.containerAmountValue);

        //checking if the water quality is already set to bad on startup
        ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    runOnUiThread(()->{
                        Boolean waterQualityBad = task.getResult().getValue(Boolean.class);
                        if (waterQualityBad){
//                            tds_warn_dialog.show();
                            tds_warn.show();
                            waterQualityValue.setText("❌");
                        } else {
                            waterQualityValue.setText("✅");
                        }
                    });
                }
            }
        });

        //adding listener for water quality warning
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if ((Boolean) snapshot.getValue()) {
//                        tds_warn_dialog.show();
                        tds_warn.show();
                        waterQualityValue.setText("❌");
                    } else {
//                        tds_warn_dialog.dismiss();
                        tds_warn.dismiss();
                        waterQualityValue.setText("✅");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //nothing for now
            }
        });

        //checking if th water level is low
        water_level_ref.get().addOnSuccessListener(snap -> {
            if (snap.exists() && (Boolean) snap.getValue()) {
                water_low_dialog.show();
            }
            ;
        });

        water_level_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if ((Boolean) snapshot.getValue()) {
                        water_low_dialog.show();
                    } else {
                        water_low_dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //nothing
            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        Timestamp startOfDayTimestamp = new Timestamp(startOfDay);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();
        Timestamp endOfDayTimestamp = new Timestamp(endOfDay);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DatabaseReference walterLevelRef = db.getReference("ContainerWaterLevel/containerLevel");
        CollectionReference totalDrankAmount = firestore.collection("TotalDrankAmount");
        AtomicReference<DocumentReference> totalDrankAmountRef = new AtomicReference<>();
        DocumentReference waterLimitRef = firestore.collection("WaterLimit").document("limitID_01");

        //setting the initial value for container level
        walterLevelRef.get().addOnCompleteListener((task) -> {
            if (task.isSuccessful()) {

                Double value = task.getResult().getValue(Double.class);
                String level = getWaterLevel(value);

                //setting the value to the text view (UI)
                runOnUiThread(() -> {
                    containerAmountValue.setText(value.toString());
                    containerLevel.setText(level);
                });
            }
        });

        //listener to update the water level
        walterLevelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double value = snapshot.getValue(Double.class);
                String level = getWaterLevel(value);

                runOnUiThread(() -> {
                    containerAmountValue.setText(value.toString());
                    containerLevel.setText(level);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getWaterLevel(Double val){
        if (val <= 500){
            return "Low";
        } else if (val <= 1250){
            return "Normal";
        } else {
            return "Full";
        }
    }

    private Snackbar getSnackBar(String message, String color, int text_color){
        Snackbar bar = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            bar = Snackbar.make(requireViewById(android.R.id.content) , message, Snackbar.LENGTH_INDEFINITE);
        }
        bar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color))); //"#A01010"
        TextView text = (TextView) bar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        text.setTextColor(text_color);
        bar.setActionTextColor(text_color);

        return bar;
    }

}
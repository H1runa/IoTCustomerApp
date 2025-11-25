package com.hiruna.iotmobileapp.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.circularreveal.CircularRevealHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotmobileapp.R;
import com.yy.mobile.rollingtextview.CharOrder;
import com.yy.mobile.rollingtextview.RollingTextView;
import com.yy.mobile.rollingtextview.strategy.CharOrderStrategy;
import com.yy.mobile.rollingtextview.strategy.Direction;
import com.yy.mobile.rollingtextview.strategy.Strategy;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class WaterDialog extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.water_dialog, container, false);

        TextView progressText = view.findViewById(R.id.progressText);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        RollingTextView drunkAmountText = view.findViewById(R.id.drunkAmountText);
        RollingTextView toDrinkAmountText = view.findViewById(R.id.toDrinkAmountText);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference waterLimitRef = firestore.collection("WaterLimit").document("limitID_01");
        CollectionReference tdrankAmountCollection = firestore.collection("TotalDrankAmount");

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

        AtomicLong waterLimit = new AtomicLong(0L);
        AtomicLong tdrankAmount = new AtomicLong(0L);

        //rolling text
        drunkAmountText.setAnimationDuration(500L);
        drunkAmountText.setCharStrategy(Strategy.NormalAnimation());
        drunkAmountText.addCharOrder(CharOrder.Number);
        drunkAmountText.setAnimationInterpolator(new AccelerateDecelerateInterpolator());

        toDrinkAmountText.setAnimationDuration(500L);
        toDrinkAmountText.setCharStrategy(Strategy.NormalAnimation());
        toDrinkAmountText.addCharOrder(CharOrder.Number);
        toDrinkAmountText.setAnimationInterpolator(new AccelerateDecelerateInterpolator());



        //retrieving water limit from firestore
        waterLimitRef.get().addOnSuccessListener(snap->{
           if (snap.exists()){
               waterLimit.set((Long)snap.get("waterLevel"));
           }else {
               System.err.println("ERROR: Water Limit snap is empty");
           }

            //retriving total drank amount from firebase
            tdrankAmountCollection
                    .whereGreaterThanOrEqualTo("drankDate", startOfDayTimestamp)
                    .whereLessThanOrEqualTo("drankDate", endOfDayTimestamp)
                    .get().addOnSuccessListener(s -> {
                        if (!s.isEmpty()){
                            DocumentSnapshot doc = s.getDocuments().get(0);
                            tdrankAmount.set((Long)doc.get("tdrankAmount"));

                            //updating the ui
                            drunkAmountText.setText(String.valueOf(tdrankAmount.get()));
                            toDrinkAmountText.setText(String.valueOf((waterLimit.get()-tdrankAmount.get())));
                            Float progress = ((float)tdrankAmount.get()/(float) waterLimit.get())*100;
                            DecimalFormat df = new DecimalFormat("0.00");
                            progressText.setText("Water Intake Progress: " + df.format(progress) + "%");
                            progressBar.setMin(0);
                            progressBar.setMax((int) waterLimit.get());
                            progressBar.setProgress((int) tdrankAmount.get(), true);


                        }else {
                            System.err.println("ERROR: Total Drank Amount snap is empty");
                        }
                    });

        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}

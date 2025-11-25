package com.hiruna.iotmobileapp.custom_preference.preference_fragment;


import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotmobileapp.R;
import com.hiruna.iotmobileapp.custom_preference.TimePreference;

import java.util.Calendar;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    TimePicker mTimePicker;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference timeRef = firestore.collection("SleepTime").document("sleepID_01");
    public static TimePreferenceDialogFragmentCompat newInstance(String key){
        final TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;


    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mTimePicker = (TimePicker) view.findViewById(R.id.edit);

        timeRef.get().addOnSuccessListener((snap)->{
            if (snap.exists()){
                Timestamp timestamp = snap.getTimestamp("sleepTime");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp.toDate().getTime());

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);

                mTimePicker.setHour(hour);
                mTimePicker.setMinute(minutes);
            } else {
                System.err.println("ERROR: Timestamp Snap does not exist");
            }
        }).addOnFailureListener((e)->{
            System.err.println("ERROR: Retrieving timestamp failed");
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (mTimePicker != null){
            int hour = mTimePicker.getHour();
            int minutes =  mTimePicker.getMinute();

            int minutesAfterMid = (hour*60)+minutes;

            DialogPreference pref = getPreference();
            if (pref instanceof TimePreference){
                TimePreference timePreference = (TimePreference) pref;
                timePreference.setTime(minutesAfterMid);
            }else {
                System.err.println("ERROR: Preference is not an instance of time preference");
            }
        } else {
            System.err.println("ERROR: Timepicker is null");
        }
    }
}

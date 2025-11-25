package com.hiruna.iotcustomerapp.custom_preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotcustomerapp.R;

import java.util.Calendar;
import java.util.HashMap;

public class TimePreference extends DialogPreference {

    private int mTime;
    private int mDialogResId = R.layout.pref_dialog_time;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference timeRef = firestore.collection("SleepTime").document("sleepID_01");
    public TimePreference(@NonNull Context context) {
        this(context, null);
    }

    public TimePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getTime(){
        return mTime;
    }

    public void setTime(int time){
        mTime = time;
        System.out.println("TIME SET TO : "+time);

        int hour = time/60;
        int minute = time%60;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Timestamp timestamp = new Timestamp(calendar.getTime());

        HashMap<String, Timestamp> data = new HashMap<>();
        data.put("sleepTime", timestamp);

        timeRef.set(data).addOnSuccessListener((nothing)->{
            System.out.println("Time updated on firebase");
        }).addOnFailureListener((e)->{
            System.err.println("ERROR: Failed to save timestamp to firebase");
        });
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        //leaving this empty
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogResId;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView title = holder.itemView.findViewById(R.id.title);
        CharSequence title_str = getTitle();

        TextView summary = holder.itemView.findViewById(R.id.summary);
        CharSequence summary_str = getSummary();

        if (title != null && title_str != null){
            title.setText(title_str);
        }
        if (summary != null && summary_str != null){
            summary.setText(summary_str);
        }

    }
}

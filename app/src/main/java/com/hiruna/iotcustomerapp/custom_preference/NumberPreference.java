package com.hiruna.iotcustomerapp.custom_preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceViewHolder;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotcustomerapp.R;

import java.util.HashMap;

public class NumberPreference extends EditTextPreference {

    public NumberPreference(@NonNull Context context) {
        super(context);
        init();
    }

    public NumberPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NumberPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        //numbers only
        setOnBindEditTextListener(editText->{
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        });

        //firebase
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference waterLimitRef = firestore.collection("WaterLimit").document("limitID_01");

        waterLimitRef.get().addOnSuccessListener(snap ->{
            if (snap.exists()){
                Object waterLimit = snap.get("waterLevel");
                if (waterLimit != null){
                    String waterLimitString = waterLimit.toString();
                    setText(waterLimitString);
                    setSummary(waterLimitString);
                } else {
                    System.err.println("ERROR: Snap null");
                }
            } else {
                System.err.println("ERROR: Snap doesnt exist");
            }
        }).addOnFailureListener(e -> {
            System.err.println("ERROR: Failed retrieving water limit");
        });

        //saving to firebase
        setOnPreferenceChangeListener((pref, newValue)->{
            Long waterLevel = Long.valueOf((String) newValue);

            HashMap<String, Long> data = new HashMap<>();
            data.put("waterLevel", waterLevel);

            waterLimitRef.set(data).addOnSuccessListener((nothing)->{
                System.out.println("WATER LIMIT SET");
                String waterLevelString = waterLevel.toString();
                setText(waterLevelString);
                setSummary(waterLevelString);
            }).addOnFailureListener(e->{
                System.err.println("ERROR: Failed to set water level");
            });

            return true;
        });
    }




    @Override
    protected void onSetInitialValue(Object defaultValue) {
        //none
    }

    @Override
    protected boolean persistString(String value) {
        //none
        return true;
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        //none
        return "";
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

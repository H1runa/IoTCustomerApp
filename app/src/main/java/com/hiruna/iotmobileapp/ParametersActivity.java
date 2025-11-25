package com.hiruna.iotmobileapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotmobileapp.custom_preference.TimePreference;
import com.hiruna.iotmobileapp.custom_preference.preference_fragment.TimePreferenceDialogFragmentCompat;

public class ParametersActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private static Long waterLevel = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onDisplayPreferenceDialog(@NonNull Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof TimePreference){
                dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
            }
            if (dialogFragment != null){
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(), "timePrefManager");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}
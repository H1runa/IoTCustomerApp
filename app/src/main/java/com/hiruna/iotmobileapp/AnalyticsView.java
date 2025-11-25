package com.hiruna.iotmobileapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hiruna.iotmobileapp.ui.main.PlaceholderFragment;
import com.hiruna.iotmobileapp.ui.main.SectionsPagerAdapter;
import com.hiruna.iotmobileapp.databinding.ActivityAnalyticsViewBinding;

public class AnalyticsView extends AppCompatActivity {

    private ActivityAnalyticsViewBinding binding;
    private Snackbar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAnalyticsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        //setting up listener for snackbar
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 1){
                    if (PlaceholderFragment.badSleepCounter > 1){
                        bar = Snackbar.make(binding.getRoot(), "Unhealthy sleeping patterns detected", Snackbar.LENGTH_INDEFINITE)
                                .setAction("X", v->{});
                        bar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A01010")));
                        TextView text = (TextView) bar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                        text.setTextColor(Color.WHITE);
                        bar.setActionTextColor(Color.WHITE);
                        bar.show();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 1){
                    bar.dismiss();
                    bar = null;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
}
package com.hiruna.iotmobileapp.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hiruna.iotmobileapp.R;
import com.hiruna.iotmobileapp.databinding.FragmentAnalyticsViewBinding;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private FragmentAnalyticsViewBinding binding;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static int badSleepCounter = 0;
    private static Float water_limit;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAnalyticsViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        int index = getArguments().getInt(ARG_SECTION_NUMBER);
        if (index == 1){
            BarChart chart = binding.tdrinkAmountChart;
            chart.setVisibility(View.VISIBLE);
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(true);

            CollectionReference ref = firestore.collection("TotalDrankAmount");
            ref.get().addOnSuccessListener(snap -> {
                ArrayList<BarEntry> list = new ArrayList<>();
                ArrayList<String> xaxist_list = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();

                if (!snap.isEmpty()){
                    List<DocumentSnapshot> docList = snap.getDocuments();
                    for (int i = 0; i<docList.size(); i++){
                        if (i >= 7){
                            break;
                        }
                        Long amount = (Long)docList.get(i).get("tdrankAmount");
                        list.add(new BarEntry(i, amount));
                        Timestamp timestamp = (Timestamp)docList.get(i).get("drankDate");
                        Date date = timestamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
                        String label = sdf.format(date);

//                        System.out.println(water_limit);

//                        System.out.println(label);

                        xaxist_list.add(String.valueOf(label));
                    }

                    XAxis xAxis = chart.getXAxis();
                    xAxis.setTextColor(Color.WHITE);
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(xaxist_list));

                    //starting from 0
                    YAxis leftAxis = chart.getAxisLeft();
                    leftAxis.setTextColor(Color.WHITE);
                    leftAxis.setAxisMinimum(0f);          // start from 0
                    leftAxis.setAxisMaximum(2500f);       // set a bit above your largest value
                    leftAxis.setGranularity(500f);        // interval between ticks
                    leftAxis.setGranularityEnabled(true);

                    BarDataSet set = new BarDataSet(list, "ml");
                    set.setValueTextColor(Color.WHITE);
                    set.setValueTextSize(10F);

                    firestore.collection("WaterLimit").document("limitID_01").get()
                            .addOnSuccessListener(snapshot -> {
                                Long raw = (Long)snapshot.get("waterLevel");
                                Float float_raw = raw.floatValue();

                                for (int i =0; i<list.size(); i++){
                                    if (list.get(i).getY() < float_raw/2){
                                        colors.add(Color.parseColor("#8B0000"));
                                        set.setColors(colors);
                                        BarData data = new BarData(set);

                                        chart.setData(data);
                                        chart.invalidate();

                                    } else {
                                        colors.add(Color.parseColor("#006400"));
                                        BarData data = new BarData(set);

                                        chart.setData(data);
                                        chart.invalidate();
                                    }
                                }
                            });


                }
            });
        } else if (index == 2){
            //setting up line chart
            HorizontalBarChart chart = binding.sleepChart;
            chart.setVisibility(View.VISIBLE);
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);

            CollectionReference ref = firestore.collection("SleepHours");
            ref.get().addOnSuccessListener(snap -> {
                if (!snap.isEmpty()){
                    List<DocumentSnapshot> list = snap.getDocuments();
                    ArrayList<BarEntry> entries = new ArrayList<>();
                    ArrayList<String> label_entries = new ArrayList<>();
                    ArrayList<Integer> colors = new ArrayList<>();
                    int offset = 0;
                    for (int i = 0; i<list.size(); i++){
                        if (i >= 7){
                            break;
                        }

                        //getting the sleep duration
                        Timestamp startTimestamp = (Timestamp)list.get(i).get("sleepStartTime");
                        Instant startDate = startTimestamp.toInstant();

                        Timestamp endTimestamp = (Timestamp)list.get(i).get("sleepEndTime");
                        Instant endDate = endTimestamp.toInstant();

                        Duration duration = Duration.between(startDate, endDate);
                        Long hours = duration.toHours();

                        //getting the data
                        Date start = startTimestamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
                        String formatted_date = sdf.format(start);

//                        System.out.println(hours);

                        if (label_entries.contains(formatted_date)){
                            int label_index = label_entries.indexOf(formatted_date);
                            BarEntry barEntry = entries.get(label_index);
                            Long got_hours = (long) barEntry.getY();
                            Long new_hours = got_hours+hours;
                            entries.set(label_index, new BarEntry(label_index, (new_hours)));
                            if (new_hours < 8){
                                colors.set(label_index, Color.parseColor("#8B0000"));
                            }
                            offset++;
                            continue;
                        }

                        entries.add(new BarEntry(i-offset, hours));
                        if (hours < 8){
                            colors.add(i-offset, Color.parseColor("#8B0000"));
                            badSleepCounter++;
                        } else {
                            colors.add(i-offset, Color.parseColor("#006400"));
                        }
                        label_entries.add(formatted_date);
                    }

                    XAxis xAxis = chart.getXAxis();
                    xAxis.setTextColor(Color.WHITE);
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(label_entries));

                    //starting from 0
                    YAxis leftAxis = chart.getAxisLeft();
                    leftAxis.setTextColor(Color.WHITE);
                    leftAxis.setAxisMinimum(0f);          // start from 0
                    leftAxis.setAxisMaximum(24f);       // set a bit above your largest value
                    leftAxis.setGranularity(1f);        // interval between ticks
                    leftAxis.setGranularityEnabled(true);

                    YAxis rightAxit = chart.getAxisRight();
                    rightAxit.setEnabled(false);
                    chart.setFitBars(true);

                    BarDataSet set = new BarDataSet(entries, "hours");
                    set.setValueTextColor(Color.WHITE);
                    set.setValueTextSize(10F);
                    set.setColors(colors);
                    BarData data = new BarData(set);

                    chart.setData(data);
                    chart.invalidate();
                } else {
                    System.err.println("ERROR: Sleep Hours snap returned empty");
                }
            });
        } else if (index == 3){
            LineChart chart = binding.heartRateChart;
            chart.setVisibility(View.VISIBLE);
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // Example: get heart rate data for today
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Timestamp startOfDay = new Timestamp(cal.getTime());

            cal.add(Calendar.DAY_OF_MONTH, 1);
            Timestamp endOfDay = new Timestamp(cal.getTime());

            firestore.collection("heartRate")
                    .whereGreaterThanOrEqualTo("datetime", startOfDay)
                    .whereLessThan("datetime", endOfDay)
                    .orderBy("datetime")
                    .get()
                    .addOnSuccessListener(snap -> {
                        ArrayList<Entry> entries = new ArrayList<>();
                        ArrayList<String> labels = new ArrayList<>();
                        int i = 0;

                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Timestamp ts = doc.getTimestamp("datetime");
                            long bpm = doc.getLong("averageBPM");

                            entries.add(new Entry(i, bpm));

                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            labels.add(sdf.format(ts.toDate()));

                            i++;
                        }

                        LineDataSet dataSet = new LineDataSet(entries, "BPM");
                        dataSet.setDrawCircles(false);
                        dataSet.setLineWidth(2f);
                        dataSet.setColor(Color.RED);
                        dataSet.setDrawValues(false);

                        LineData data = new LineData(dataSet);
                        chart.setData(data);

                        XAxis xAxis = chart.getXAxis();
                        xAxis.setTextColor(Color.WHITE);
                        xAxis.setGranularity(1f);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

                        YAxis leftAxis = chart.getAxisLeft();
                        leftAxis.setTextColor(Color.WHITE);
                        leftAxis.setAxisMinimum(0f);

                        chart.invalidate();
                    });
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TimePicker;
import android.view.View.OnClickListener;

public class MyActivity extends Activity
{

    private AlarmScheduler alarmScheduler;
    AlarmTimePicker alarmTimePicker;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setAlarmScheduler(new AlarmSchedulerImpl(this));


        ((Button)findViewById(R.id.setAlarmButton)).setOnClickListener(new AlarmSetButtonClickListener());
        ((Button)findViewById(R.id.deleteAlarmButton)).setOnClickListener(new AlarmDeleteButtonClickListener());
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        layout.setBackgroundColor(Color.WHITE);

        alarmTimePicker = (CustomTimePicker)this.findViewById(R.id.alarmTimePicker);
    }

    public void setAlarmScheduler(AlarmScheduler alarmScheduler) {
        this.alarmScheduler = alarmScheduler;

    }

    public AlarmScheduler getAlarmScheduler() {
        return alarmScheduler;
    }

    public class AlarmSetButtonClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            alarmScheduler.addAlarm(MyActivity.this, alarmTimePicker.getHours(), alarmTimePicker.getMinutes(), alarmTimePicker.getInterval());
            FileHandler.saveAlarm(alarmTimePicker.getHours(), alarmTimePicker.getMinutes(), MyActivity.this.getApplicationContext());
        }
    }

    public class AlarmDeleteButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            alarmScheduler.deleteAlarm(MyActivity.this);
        }
    }


}

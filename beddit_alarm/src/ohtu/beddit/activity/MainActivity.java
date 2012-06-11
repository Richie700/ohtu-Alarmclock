package ohtu.beddit.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.*;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.Toast;
import ohtu.beddit.R;
import ohtu.beddit.alarm.AlarmService;
import ohtu.beddit.alarm.AlarmServiceImpl;
import ohtu.beddit.alarm.AlarmTimeChangedListener;
import ohtu.beddit.alarm.AlarmTimePicker;
import ohtu.beddit.views.timepicker.CustomTimePicker;
import ohtu.beddit.io.PreferenceService;

public class MainActivity extends Activity implements AlarmTimeChangedListener
{

    private AlarmService alarmService;
    private AlarmTimePicker alarmTimePicker;
    private Button addAlarmButton;
    private Button deleteAlarmButton;


    /** Called when the alarm is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setAlarmService(new AlarmServiceImpl(this));

        //initialize default values for settings if called for the first time
        PreferenceManager.setDefaultValues(this, R.xml.prefs, true);
        PreferenceManager.setDefaultValues(this, R.xml.advancedprefs, true);
        setUI();

        // Update buttons and clock handles
        updateButtons();
        setClockHands();
        checkAuth();
        //myToast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
    }

    private void checkAuth() {
        String token = PreferenceService.getToken(this);
        String username = PreferenceService.getUsername(this, 0);
        if (token != null){
            Log.v("Token:", token);
        }
        if (token == null || token.equals("") || username == null || username.equals("")) {
            Intent myIntent = new Intent(this, AuthActivity.class);
            startActivityForResult(myIntent,2);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        updateButtons();
        updateColours();
        update24HourMode();
    }



    private void setUI() {
        //Set clock, buttons and listeners
        alarmTimePicker = (CustomTimePicker)this.findViewById(R.id.alarmTimePicker);
        alarmTimePicker.addAlarmTimeChangedListener(this);

        addAlarmButton = (Button) findViewById(R.id.setAlarmButton);
        addAlarmButton.setOnClickListener(new AlarmSetButtonClickListener());
        deleteAlarmButton = (Button)findViewById(R.id.deleteAlarmButton);
        deleteAlarmButton.setOnClickListener(new AlarmDeleteButtonClickListener());

        updateColours();
    }

    private void updateColours(){
        String theme = PreferenceService.getColourTheme(this);
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        if(theme.equals("dark")){
            layout.setBackgroundColor(Color.BLACK);
            alarmTimePicker.setBackgroundColor(Color.BLACK);
            alarmTimePicker.setForegroundColor(Color.WHITE);
            alarmTimePicker.setSpecialColor(Color.argb(255,255,89,0));
        }
        else if(theme.equals("light")){
            layout.setBackgroundColor(Color.WHITE);
            alarmTimePicker.setBackgroundColor(Color.WHITE);
            alarmTimePicker.setForegroundColor(Color.BLACK);
            alarmTimePicker.setSpecialColor(Color.argb(255,255,89,0));
        }
    }

    private void update24HourMode(){
        alarmTimePicker.set24HourMode(DateFormat.is24HourFormat(this));
    }

    private void setClockHands() {
        alarmTimePicker.setHours(alarmService.getAlarmHours(this));
        alarmTimePicker.setMinutes(alarmService.getAlarmMinutes(this));
        alarmTimePicker.setInterval(alarmService.getAlarmInterval(this));
    }

    @Override
    public void onAlarmTimeChanged(int hours, int minutes, int interval) {
        alarmService.changeAlarm(this, hours, minutes, interval);

        if (alarmService.isAlarmSet(this))
            showMeTheToast(getString(R.string.toast_alarmupdated));

    }

    private Toast myToast = null;

    private void showMeTheToast(final String message) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                myToast.setText(message);
                myToast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
                myToast.show();
            }
        });
    }





    public class AlarmSetButtonClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            alarmService.addAlarm(MainActivity.this, alarmTimePicker.getHours(), alarmTimePicker.getMinutes(), alarmTimePicker.getInterval());
            MainActivity.this.updateButtons();
            // Tell the user about what we did.
            showMeTheToast(getString(R.string.toast_alarmset));
        }
    }


    public class AlarmDeleteButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            alarmService.deleteAlarm(MainActivity.this);
            MainActivity.this.updateButtons();
            showMeTheToast(getString(R.string.toast_alarmremoved));

        }
    }

    // Set buttons to on/off
    public void updateButtons(){
        if (alarmService.isAlarmSet(this.getApplicationContext())){
            addAlarmButton.setEnabled(false);
            deleteAlarmButton.setEnabled(true);
        } else {
            addAlarmButton.setEnabled(true);
            deleteAlarmButton.setEnabled(false);
        }
        Log.v("User Interface", "Buttons updated");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        MenuItem settings = menu.findItem(R.id.settings_menu_button);
        settings.setIntent(new Intent(this.getApplicationContext(), SettingsActivity.class));

        MenuItem help = menu.findItem(R.id.help_menu_button);
        help.setIntent(new Intent(this.getApplicationContext(), HelpActivity.class));

        MenuItem sleepInfo = menu.findItem(R.id.sleep_info_button);
        sleepInfo.setIntent(new Intent(this.getApplicationContext(), SleepInfoActivity.class));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu_button:
                startActivityForResult(item.getIntent(), 2);
                break;
            case R.id.help_menu_button:
                startActivity(item.getIntent());
                break;
            case R.id.sleep_info_button:
                startActivity(item.getIntent());
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (2) : {
                if (resultCode == Activity.RESULT_OK) {
                    Log.v("MainActivity", "Auth fails miserably");
                    //start dialog / whatever
                    createDialog();
                    //then finish
                    //this.finish();
                }
                break;
            }
        }
    }

    public void createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("CRASH!");
        builder.setCancelable(false);
        builder.setPositiveButton("D:", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        Log.v("dialogi", "nakyy: "+alert.isShowing());
        alert.show();
        Log.v("dialogi", "nakyy: "+alert.isShowing());

    }


    // These methods are for tests
    public void setAlarmService(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    public AlarmService getAlarmService() {
        return alarmService;
    }

}

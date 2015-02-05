package com.app.securitysystemalerts;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class EventLog extends Activity {

    private TextView [] eventLog;
    private final int EVENTS_NUM = 10;

    void initializeEventLog() {

        eventLog = new TextView[EVENTS_NUM];
        eventLog[0] = (TextView) findViewById(R.id.ss_row0);
        eventLog[0].setText("Waiting For Connection");
        eventLog[1] = (TextView) findViewById(R.id.ss_row1);
        eventLog[2] = (TextView) findViewById(R.id.ss_row2);
        eventLog[3] = (TextView) findViewById(R.id.ss_row3);
        eventLog[4] = (TextView) findViewById(R.id.ss_row4);
        eventLog[5] = (TextView) findViewById(R.id.ss_row5);
        eventLog[6] = (TextView) findViewById(R.id.ss_row6);
        eventLog[7] = (TextView) findViewById(R.id.ss_row7);
        eventLog[8] = (TextView) findViewById(R.id.ss_row8);
        eventLog[9] = (TextView) findViewById(R.id.ss_row9);
    }

    void writeEventLog(String events) {

        try {
            if (events != null) {
                StringTokenizer Tok = new StringTokenizer(events, "\n");
                for (int i = 0; i < EVENTS_NUM; i++) {
                    if (Tok.hasMoreElements()) {
                        eventLog[i].setText(String.valueOf(Tok.nextElement()));
                    } else {
                        eventLog[i].setText("");
                    }
                }
            }
        }
        catch (NullPointerException | NoSuchElementException e) {
            DebugLog.writeLog(e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        DebugLog.writeLog("Create EventLog.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventlog);

        initializeEventLog();

        final Button logoutButton = (Button) findViewById(R.id.logout);
        final Button exitButton = (Button) findViewById(R.id.exit);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DebugLog.writeLog("Logout pressed, stop AlertService, exit EventLog and start Settings.");
                stopService(new Intent(EventLog.this, AlertsService.class));
                startActivity(new Intent(EventLog.this, Settings.class));
                finish();
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DebugLog.writeLog("Exit pressed, stop AlertService and exit EventLog.");
                stopService(new Intent(EventLog.this, AlertsService.class));
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancelAll();
                finish();
            }
        });

        IntentFilter intentFilter = new IntentFilter("AlertsService");
        EventLogReceiver eventLogReceiver = new EventLogReceiver();
        android.support.v4.content.LocalBroadcastManager.getInstance(this).registerReceiver(
            eventLogReceiver, intentFilter);

        Intent intent = new Intent(EventLog.this, AlertsService.class);
        Bundle extras = getIntent().getExtras();
        intent.putExtras(extras);
        startService(intent);
    }

    @Override
    public void onDestroy() {

        DebugLog.writeLog("Destroy EventLog");
        super.onDestroy();
    }

    @Override
    public void onPause() {

        DebugLog.writeLog("Pause EventLog");
        super.onPause();
    }

    @Override
    public void onResume() {

        DebugLog.writeLog("Resume EventLog");
        super.onResume();
    }

    public void onBackPressed() {
        moveTaskToBack (true);
    }

    private final class EventLogReceiver extends BroadcastReceiver
    {
        private EventLogReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("Status").equals("Authentication Failure")) {
                DebugLog.writeLog("Authentication failure, exit EventLog and restart Settings.");
                startActivity(new Intent(EventLog.this, Settings.class).putExtra("Message", intent.getStringExtra("EventLog")));
                finish();
            }
            else if (intent.getStringExtra("Status").equals("Offline Mode")) {
                DebugLog.writeLog("Connection problem, write message in EventLog and retry.");
                writeEventLog("Network connection problem, retry in 10 seconds.");
                return;
            }
            else {
                writeEventLog(intent.getStringExtra("EventLog"));
            }
        }
    }
}

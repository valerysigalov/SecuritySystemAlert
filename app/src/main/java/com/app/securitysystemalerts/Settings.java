package com.app.securitysystemalerts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Settings extends Activity {

    // Security System Settings
    private EditText ssLoginUrl;
    private EditText ssUsername;
    private EditText ssPassword;

    // Email Settings
    private EditText smtpHost;
    private EditText smtpPort;

    private EditText emailUsername;
    private EditText emailPassword;

    private EditText toEmail;
    private EditText ccEmail;

    // SMS Settings
    private Spinner operator;
    private EditText toNumber;
    private EditText ccNumber;

    private Button applyButton;

    private void initializeViews() {

        // Security System Settings
        ssLoginUrl = (EditText) findViewById(R.id.ss_login_url);
        ssUsername = (EditText) findViewById(R.id.ss_username);
        ssPassword = (EditText) findViewById(R.id.ss_password);

        // Email Settings
        smtpHost = (EditText) findViewById(R.id.ss_smtp_host);
        smtpPort = (EditText) findViewById(R.id.ss_smtp_port);

        emailUsername = (EditText) findViewById(R.id.ss_email_username);
        emailPassword = (EditText) findViewById(R.id.ss_email_password);

        toEmail = (EditText) findViewById(R.id.ss_to_email);
        ccEmail = (EditText) findViewById(R.id.ss_cc_email);

        // SMS Settings
        operator = (Spinner) findViewById(R.id.ss_operator);
        toNumber = (EditText) findViewById(R.id.ss_to_phone);
        ccNumber = (EditText) findViewById(R.id.ss_cc_phone);

        applyButton = (Button) findViewById(R.id.apply);
    }

    private void saveSettings() {

        DebugLog.writeLog("Save Settings");
        SharedPreferences.Editor edit = getPreferences(MODE_PRIVATE).edit();
        edit.putString("ssLoginUrl", ssLoginUrl.getText().toString());
        edit.putString("ssUsername", ssUsername.getText().toString());
        edit.putString("ssPassword", ssPassword.getText().toString());
        edit.putString("smtpHost", smtpHost.getText().toString());
        edit.putString("smtpPort", smtpPort.getText().toString());
        edit.putString("emailUsername", emailUsername.getText().toString());
        edit.putString("emailPassword", emailPassword.getText().toString());
        edit.putString("toEmail", toEmail.getText().toString());
        edit.putString("ccEmail", ccEmail.getText().toString());
        edit.putString("operator", operator.getSelectedItem().toString());
        edit.putString("toNumber", toNumber.getText().toString());
        edit.putString("ccNumber", ccNumber.getText().toString());
        edit.commit();
    }

    private void restoreSettings() {

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        if (pref != null) {

            DebugLog.writeLog("Restore Saved Settings");
            String value;
            value = pref.getString("ssLoginUrl", null);
            if (value != null) {
                ssLoginUrl.setText(value);
            }
            value = pref.getString("ssUsername", null);
            if (value != null) {
                ssUsername.setText(value);
            }
            value = pref.getString("ssPassword", null);
            if (value != null) {
                ssPassword.setText(value);
            }
            value = pref.getString("smtpHost", null);
            if (value != null) {
                smtpHost.setText(value);
            }
            value = pref.getString("smtpPort", null);
            if (value != null) {
                smtpPort.setText(value);
            }
            value = pref.getString("emailUsername", null);
            if (value != null) {
                emailUsername.setText(value);
            }
            value = pref.getString("emailPassword", null);
            if (value != null) {
                emailPassword.setText(value);
            }
            value = pref.getString("toEmail", null);
            if (value != null) {
                toEmail.setText(value);
            }
            value = pref.getString("ccEmail", null);
            if (value != null) {
                ccEmail.setText(value);
            }
            value = pref.getString("operator", null);
            if (value != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        R.array.mobile_operators, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                operator.setAdapter(adapter);
                int pos = adapter.getPosition(value);
                operator.setSelection(pos);
            }
            value = pref.getString("toNumber", null);
            if (value != null) {
                toNumber.setText(value);
            }
            value = pref.getString("ccNumber", null);
            if (value != null) {
                ccNumber.setText(value);
            }
        }
    }

    private void setInputMethod() {

        ssLoginUrl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ssLoginUrl, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ssUsername.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ssUsername, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ssPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ssPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        smtpHost.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(smtpHost, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        smtpPort.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(smtpPort, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        emailUsername.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(emailUsername, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        emailPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(emailPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        toEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(toEmail, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ccEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ccEmail, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        toNumber.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(toNumber, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ccNumber.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ccNumber, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void initializeListener() {

        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this,
                        EventLog.class);
                Bundle extra = new Bundle();
                extra.putString("ssLoginUrl", ssLoginUrl.getText().toString());
                extra.putString("ssUsername", ssUsername.getText().toString());
                extra.putString("ssPassword", ssPassword.getText().toString());
                extra.putString("smtpHost", smtpHost.getText().toString());
                extra.putString("smtpPort", smtpPort.getText().toString());
                extra.putString("emailUsername", emailUsername.getText().toString());
                extra.putString("emailPassword", emailPassword.getText().toString());
                extra.putString("toEmail", toEmail.getText().toString());
                extra.putString("ccEmail", ccEmail.getText().toString());
                extra.putString("operator", operator.getSelectedItem().toString());
                extra.putString("toNumber", toNumber.getText().toString());
                extra.putString("ccNumber", ccNumber.getText().toString());
                intent.putExtras(extra);
                saveSettings();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    public static void showMessage(Context context, String message) {

        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        DebugLog.writeLog("Create Settings");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        String message = getIntent().getStringExtra("Message");
        if (message != null) {
            showMessage(getApplicationContext(), message);
        }

        initializeViews();
        restoreSettings();
        setInputMethod();
        initializeListener();
    }

    @Override
    public void onDestroy() {

        DebugLog.writeLog("Destroy Settings");
        super.onDestroy();
    }

    @Override
    public void onPause() {

        DebugLog.writeLog("Pause Settings");
        super.onPause();
    }

    @Override
    public void onResume() {

        DebugLog.writeLog("Resume Settings");
        super.onResume();
    }
}

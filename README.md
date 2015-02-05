# SecuritySystemAlerts
This application reads the Event Log from your Security System account.

This application logs to your Security System account with the provided credentials 
and periodically checks the Event Log page. The Event Log reflects the actions and 
changes in your Security System like alarm, arm, disarm, etc. The notification about 
these changes will be sent to your phone. The notification email could be sent to 
the two email addresses and the notification SMS message could be sent to the two 
phone numbers. The SMTP credentials should be provided to enable this feature. It 
could be the credentials of your GMail account or some other SMTP server. The 
authentication check is performed with both Security System server and SMTP sever 
and you will be returned back to the settings page in case of the wrong credentials. 
After successful login the event log page with the last 10 events will be displayed. 
In case of connection problem you will be notified that application is working in 
the offline mode and the connection retries are being made every 10 seconds. The 
logout button can be used to change the existing credentials or notification 
receivers list and the exit button can be used to quit the application. The 
application settings are stored locally on your phone and can be removed by 
uninstalling the application.

package cz.martykan.mibandgeocaching;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class CloseNotification extends IntentService {

    public CloseNotification() {
        super("CloseNotification");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            NotificationManager notificationManger =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.cancel(01);
            stopService(new Intent(this, MainService.class));
        }
    }
}

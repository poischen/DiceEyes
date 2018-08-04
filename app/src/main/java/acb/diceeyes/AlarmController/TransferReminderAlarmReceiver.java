package acb.diceeyes.AlarmController;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import acb.diceeyes.R;
import acb.diceeyes.Storage;
import acb.diceeyes.View.PhotoReviewTransferActivity;

import static java.lang.System.currentTimeMillis;

/**
 * Created by anita_000 on 31.07.2018.
 */

public class TransferReminderAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder reviewNotificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.dice)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(context.getResources().getString(R.string.notifications_datatransfer))
                        .setOngoing(true);

        reviewNotificationBuilder.setLights(Color.rgb(230, 74, 25), 2500, 3000);
        reviewNotificationBuilder.setVibrate(new long[] { 1000, 1000, 1000 });

        Intent reviewIntent = new Intent(context, PhotoReviewTransferActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PhotoReviewTransferActivity.class);
        stackBuilder.addNextIntent(reviewIntent);
        PendingIntent reviewPendingIntent =
                stackBuilder.getPendingIntent(
                        (int) currentTimeMillis(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        reviewNotificationBuilder.setContentIntent(reviewPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((R.integer.notification_id_reminderdatatransfer), reviewNotificationBuilder.build());

        Storage storage = new Storage(context);
        storage.setAllPhotosWereTaken(false);
        storage.resetMissedPeriodsCounter();
    }
}
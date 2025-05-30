package com.lksnext.ParkingELadron.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.lksnext.ParkingELadron.R;

public class ReservationNotificationWorker extends Worker {
    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_MESSAGE = "KEY_MESSAGE";
    public static final String KEY_NOTIFICATION_ID = "KEY_NOTIFICATION_ID"; // Opcional

    public ReservationNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String title = getInputData().getString(KEY_TITLE);
        String message = getInputData().getString(KEY_MESSAGE);
        int notificationId = getInputData().getInt(KEY_NOTIFICATION_ID, (int) System.currentTimeMillis());

        if (title == null || message == null) {
            Log.e("ReservationWorker", "Faltan datos para la notificación");
            return Result.failure();
        }

        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "reservation_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Reservas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_parking_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_parking_colored))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(context, R.color.mainOrange));

        notificationManager.notify(notificationId, builder.build());
        Log.d("ReservationWorker", "¡El Worker ha ejecutado la notificación!");
        return Result.success();
    }
}
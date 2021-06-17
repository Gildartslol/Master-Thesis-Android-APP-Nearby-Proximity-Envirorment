package com.example.jorge.androidapp.framework.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.example.jorge.androidapp.R;

public class ApplicationNotificationManager {

    public static String DEFAULT = "DEFAULT";

    public static int ID_MULTIFILE = 1;
    public static int ID_REQUEST = 2;

    public static Notification generateNotificationFileDownloaded(Context context, String filename, String endopointUserName, PendingIntent intent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setContentTitle(context.getString(R.string.notification_multifile_title))
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND |
                        Notification.DEFAULT_VIBRATE)
                .setContentText(context.getString(R.string.notification_multifile_body, filename, endopointUserName))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(intent);
        return builder.build();
    }

    public static Notification generateNotificationFriendRequest(Context context,String username,PendingIntent intent){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT)
                .setSmallIcon(R.drawable.ic_person_black_24dp)
                .setContentTitle(context.getString(R.string.notification_request))
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND |
                        Notification.DEFAULT_VIBRATE)
                .setContentText(context.getString(R.string.notification_friend_request,username))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(intent);
        return builder.build();
    }

    public static Notification generateNotificationFriendRequestEnd(Context context,String username){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT)
                .setSmallIcon(R.drawable.ic_person_black_24dp)
                .setContentTitle(context.getString(R.string.notification_friend_request_end,username))
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND |
                        Notification.DEFAULT_VIBRATE)
                .setContentText(context.getString(R.string.notification_friend_request,username))
                .setPriority(NotificationCompat.PRIORITY_LOW);
        return builder.build();
    }

}

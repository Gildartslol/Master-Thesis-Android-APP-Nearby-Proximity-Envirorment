package com.example.jorge.androidapp.ui.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.framework.nearby.NearbyApplication;
import com.example.jorge.androidapp.framework.nearby.NearbyConnectionsService;
import com.example.jorge.androidapp.framework.notifications.ApplicationNotificationManager;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.network.petition.RequestFriendPetition;
import com.example.jorge.androidapp.ui.activities.chat.ChatActivity;
import com.example.jorge.androidapp.ui.activities.home.HomeActivity;
import com.example.jorge.androidapp.ui.dialogs.DialogManager;

import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {


    protected int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private Thread.UncaughtExceptionHandler androidDefaultUEH;
    private SpotsDialog progressDialog;
    private AlertDialog alertDialog;
    private String TAG = "APP_SHARE_FILE";
    protected DialogManager dialogManager;

    private NearbyConnectionsService nService;
    protected MyNearbyConnectionService service;

    protected String CHANNEL_ID = "1";

    protected NotificationManagerCompat notificationManager;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();
        /*Barra de color blanco*/
        int color = ContextCompat.getColor(this, R.color.white);
        getWindow().setNavigationBarColor(color);

        /*Seteamos el view*/
        setContentView(getLayoutId());

        /*recuperamos el servicio*/

        NearbyApplication appState = ((NearbyApplication) this.getApplication());
        service = appState.service;

        /**
         Intent intent = new Intent(this, NearbyConnectionsService.class);
         if (!isMyServiceRunning(NearbyConnectionsService.class)) {
         ContextCompat.startForegroundService(this, intent);
         startService(intent);
         bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
         } else {
         if (service != null)
         bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
         }
         **/

        /*Toolbar*/
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*Bindeamos las vistas*/
        ButterKnife.bind(this);

        /*Uncaught Exceptions*/
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        setServicesParameters();
        notificationManager = NotificationManagerCompat.from(this);
        /*Logs*/
        Timber.tag(TAG);
        /*Dialogs */
        this.dialogManager = new DialogManager(this);
    }


    @Override
    protected void onStart() {
        super.onStart();

        String[] privatePermissions = getRequiredPermissions();

        /*Pedimos permisos*/

        if (!hasPermissions(this, privatePermissions)) {
            if (!hasPermissions(this, privatePermissions)) {
                if (Build.VERSION.SDK_INT < 23) {
                    ActivityCompat.requestPermissions(
                            this, privatePermissions, REQUEST_CODE_REQUIRED_PERMISSIONS);
                } else {
                    requestPermissions(privatePermissions, REQUEST_CODE_REQUIRED_PERMISSIONS);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //this.dialogManager.getActiveDialog().dismiss();
    }

    public abstract int getLayoutId();

    public abstract void setServicesParameters();

    private void onServiceConnectedStarted() {
        setServicesParameters();
    }

    protected Bundle getBundle() {
        NearbyApplication appState = ((NearbyApplication) this.getApplication());
        return appState.getBundle();
    }

    private Context getContext() {
        return this;
    }


    /***
     ********************************************************************************
     ********************************************************************************
     ************************** SERVICE **********************************************
     ********************************************************************************
     ********************************************************************************
     */

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder bindService) {
            NearbyConnectionsService.MyNearbyBinder binder = (NearbyConnectionsService.MyNearbyBinder) bindService;
            nService = binder.getService();
            service = nService.getConnections();
            onServiceConnectedStarted();
            Utils.createToastShort(getContext(), "Service started");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    protected boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /***
     ********************************************************************************
     ********************************************************************************
     ************************** NOTIFICATIONS ***************************************
     ********************************************************************************
     ********************************************************************************
     */

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ApplicationNotificationManager.DEFAULT;
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected void writeToPreferences(String key, String value) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected String readFromPreferences(String key) {
        SharedPreferences sharedPref = getSharedPreferences();
        return sharedPref.getString(key, "");
    }


    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("SharedContentApp", "Uncaught exception is: ", ex);
            androidDefaultUEH.uncaughtException(thread, ex);

            /**
             AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
             mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent.);
             System.exit(2);
             */
        }
    };


    /***
     ********************************************************************************
     ********************************************************************************
     ************************** PERMISOS ********************************************
     ********************************************************************************
     ********************************************************************************
     */


    protected boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /**
     * Called when the user has accepted (or denied) our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected boolean isAppInBackground() {
        NearbyApplication appState = ((NearbyApplication) this.getApplication());
        return appState.getAppLifeCycleListener().isAppInBackGround();
    }

    protected abstract String getTag();


    /***
     ********************************************************************************
     ********************************************************************************
     ************************** tAGS ********************************************
     ********************************************************************************
     ********************************************************************************
     */


    @CallSuper
    public void logV(String msg) {
        Timber.tag(TAG);
        Timber.v(getTag() + msg);
    }

    @CallSuper
    public void logD(String msg) {
        Timber.tag(TAG);
        Timber.d(getTag() + msg);
    }

    @CallSuper
    public void logW(String msg) {
        Timber.tag(TAG);
        Timber.w(getTag() + msg);
    }

    @CallSuper
    public void logI(String msg) {
        Timber.tag(TAG);
        Timber.i(getTag() + msg);
    }

    @CallSuper
    public void logI(String msg, Object... args) {
        Timber.tag(TAG);
        Timber.i(getTag() + msg, args);
    }

    @CallSuper
    public void logW(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.w(e, getTag() + msg);
    }

    @CallSuper
    public void logE(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.e(e, getTag() + msg);
    }

    @CallSuper
    public void logI(String msg, Throwable e) {
        Timber.tag(TAG);
        Timber.i(e, getTag() + msg);
    }

}

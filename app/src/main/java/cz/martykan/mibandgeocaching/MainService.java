package cz.martykan.mibandgeocaching;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.martykan.mibandgeocaching.bluetooth.UserPreferences;

public class MainService extends Service {

    Notification.Builder builder;
    NotificationManager notificationManger;

    String name;
    String lat;
    String lng;

    private LocationManager mLocationManager;

    float distance;
    int prevRange;

    private UserPreferences userPreferences;

    public MainService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MiBandGeo", "started");
        if (intent != null) {
            Log.i("MiBandGeo", "Intent:" + intent.getDataString());

            String uri = intent.getDataString();

            if(uri.contains("maps.google")){
                name = "Geocache";
                lat = uri.substring(uri.indexOf("?daddr=") + 7, uri.indexOf(","));
                lng = uri.substring(uri.indexOf(",") + 1, uri.indexOf("&"));
            }
            else {
                name = java.net.URLDecoder.decode(uri.substring(uri.indexOf("(") + 1, uri.indexOf(")")));
                lat = uri.substring(uri.indexOf(":") + 1, uri.indexOf(","));
                lng = uri.substring(uri.indexOf(",") + 1, uri.indexOf("?"));
            }

            final Intent serviceIntent = new Intent(this, MiBandCommunicationService.class);
            startService(serviceIntent);

            checkMACAddressRequired();

            try {
                UserPreferences.loadPreferences(openFileInput(UserPreferences.FILE_NAME));
            }
            catch(FileNotFoundException e) {
                new UserPreferences().savePreferences(getPreferencesOutputStream());
            }

            Intent resultIntent = new Intent(this, CloseNotification.class);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new Notification.Builder(getApplicationContext());
            builder.setContentTitle(name);
            builder.setContentText("Location yet unknown");
            builder.setTicker("Location yet unknown");
            builder.setSmallIcon(R.drawable.archive);
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
            builder.setOngoing(true);
            builder.setContentIntent(resultPendingIntent);

            notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.notify(01, builder.build());

            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
            }

            Criteria criteria = new Criteria();
            String provider = mLocationManager.getBestProvider(criteria, true);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, mLocationListener);
        }

        return START_NOT_STICKY;
    }

    private void checkMACAddressRequired()
    {
        boolean showAlert = true;

        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            for (BluetoothDevice pairedDevice : pairedDevices) {
                if (pairedDevice.getName().equals("MI") && pairedDevice.getAddress().startsWith(MiBandConstants.MAC_ADDRESS_FILTER)) {
                    showAlert = false;
                }
            }

            if(showAlert)
            {
                if(!userPreferences.getMiBandMAC().equals(""))
                    showAlert = false;
            }

            if(showAlert)
            {

            }
        }
    }

    public void getDistance(Location me){
        Location cache = new Location("cache");
        cache.setLatitude(Double.parseDouble(lat));
        cache.setLongitude(Double.parseDouble(lng));
        Log.e("MiBandGeo", me.getLatitude() + "");
        Log.e("MiBandGeo", me.getLongitude() + "");
        distance = cache.distanceTo(me);
        Log.e("MiBandGeo", distance + "");

        analyzeDistance();

        builder.setContentText("Distanace: " + (int) distance  + " m");
        builder.setTicker("Distanace: " + (int) distance + " m");
        notificationManger.notify(01, builder.build());
    }

    public void analyzeDistance(){
        if((distance <= 25) && prevRange != 25){
            prevRange = 25;
            vibrate((long) 0);
            setColour(0, 255, 0);
        }
        if((distance <= 50 && distance > 25) && prevRange != 50){
            prevRange = 50;
            vibrate((long) 0);
            setColour(0, 255, 255);
        }
        if((distance <= 100 && distance > 50) && prevRange != 100){
            prevRange = 100;
            vibrate((long) 0);
            setColour(255, 255, 0);
        }
        if((distance <= 150 && distance > 100) && prevRange != 150){
            prevRange = 150;
            vibrate((long) 0);
            setColour(255, 0, 0);
        }
        if((distance <= 200 && distance > 150) && prevRange != 200){
            prevRange = 200;
            vibrate((long) 0);
            setColour(255, 0, 255);
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.e("MiBandGeo", "position recieved");
            if(location != null) {
                getDistance(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onDestroy() {
        Log.e("MiBandGeo", "stopped");
    }

    private void setColour(int r, int g, int b)
    {
        Intent intent = new Intent("colour");
        intent.putExtra("red", r);
        intent.putExtra("green", g);
        intent.putExtra("blue", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i("Intent", "sent colour");
    }

    private void vibrate(final long duration)
    {
        Intent intent = new Intent("vibrate");
        intent.putExtra("duration", duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private FileOutputStream getPreferencesOutputStream()
    {
        try
        {
            return openFileOutput(UserPreferences.FILE_NAME, Context.MODE_PRIVATE);
        }
        catch(FileNotFoundException e)
        {
            try
            {
                new UserPreferences().savePreferences(openFileOutput(UserPreferences.FILE_NAME, Context.MODE_PRIVATE));
            }
            catch(FileNotFoundException ignored)
            {
            }
        }
        return null;
    }
}

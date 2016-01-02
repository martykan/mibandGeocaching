package cz.martykan.mibandgeocaching;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.martykan.mibandgeocaching.bluetooth.UserPreferences;

public class MainActivity extends AppCompatActivity {

    private UserPreferences userPreferences;
    int prevRange = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent serviceIntent = new Intent(this, MiBandCommunicationService.class);
        startService(serviceIntent);

        try {
            UserPreferences.loadPreferences(openFileInput(UserPreferences.FILE_NAME));
        }
        catch(FileNotFoundException e) {
            new UserPreferences().savePreferences(getPreferencesOutputStream());
        }

        userPreferences = UserPreferences.getInstance();

        checkMACAddressRequired();

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");

        Button buttonMAC = (Button) findViewById(R.id.button);
        buttonMAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMACAddress();
            }
        });

        Button buttonBlink = (Button) findViewById(R.id.button2);
        buttonBlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
    }

    public void test(){
        if(prevRange == 25){
            prevRange = 50;
            vibrate((long) 0);
            setColour(0, 255, 0);
        }
        if(prevRange == 50){
            prevRange = 100;
            vibrate((long) 0);
            setColour(0, 255, 255);
        }
        if(prevRange == 100){
            prevRange = 150;
            vibrate((long) 0);
            setColour(255, 255, 0);
        }
        if(prevRange == 150){
            prevRange = 200;
            vibrate((long) 0);
            setColour(255, 0, 0);
        }
        if(prevRange == 200){
            prevRange = 25;
            vibrate((long) 0);
            setColour(255, 0, 255);
        }
    }

    public void setMACAddress(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.set_MAC_address));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        String lastMAC = userPreferences.getMiBandMAC();
        if(lastMAC.equals(""))
            lastMAC = MiBandConstants.MAC_ADDRESS_FILTER;
        input.setText(lastMAC);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String macAddress = input.getText().toString().trim().toUpperCase();
                if(macAddress.equals("")||macAddress.equals(MiBandConstants.MAC_ADDRESS_FILTER.toUpperCase()))
                {
                    userPreferences.setMiBandMAC("");
                    userPreferences.savePreferences(getPreferencesOutputStream());
                    Snackbar.with(getApplicationContext()).text(getResources().getString(R.string.set_MAC_address_removed)).show(MainActivity.this);
                }
                else {
                    Pattern p = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
                    Matcher m = p.matcher(macAddress);
                    if (m.find()) {
                        userPreferences.setMiBandMAC(macAddress);
                        userPreferences.savePreferences(getPreferencesOutputStream());
                        Snackbar.with(getApplicationContext()).text(getResources().getString(R.string.set_MAC_address_ok)).show(MainActivity.this);
                    } else {
                        Snackbar.with(getApplicationContext()).text(getResources().getString(R.string.set_MAC_address_error)).show(MainActivity.this);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
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
                Snackbar.with(getApplicationContext()).text(getResources().getString(R.string.alert_MAC_address)).show(MainActivity.this);
            }
        }
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

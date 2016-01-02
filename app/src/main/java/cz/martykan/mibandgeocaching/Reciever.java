package cz.martykan.mibandgeocaching;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Reciever extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        Intent serviceIntent = new Intent(this, MainService.class);
        serviceIntent.setData(intent.getData());
        startService(serviceIntent);

        TextView text = new TextView(this);
        text.setText(intent.getDataString());
        setContentView(text);

        finish();
    }
}

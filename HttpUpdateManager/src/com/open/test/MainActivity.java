package com.open.test;

import com.open.HttpDownloadManager.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button downLoadButton = (Button) findViewById(R.id.download_button);
        downLoadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateScheduleHelper.startOneShotSchedule(MainActivity.this, UpdateScheduleReceiver.ACTION_INTENT_UPDATE, 5 * 1000);
            }
        });
    }
}

package com.anbaoxing.bledemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class GuideActivity extends AppCompatActivity {

    private Button btnManualConnect,btnAutoConnect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        btnAutoConnect = (Button)findViewById(R.id.btn_auto_connect);
        btnManualConnect = (Button)findViewById(R.id.btn_manual_connect);

        btnAutoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoMainAty = new Intent(GuideActivity.this,BleAutoConnectActivity.class);
                startActivity(gotoMainAty);
            }
        });
        btnManualConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoScanAty = new Intent(GuideActivity.this,ScanBleActivity.class);
                startActivity(gotoScanAty);
            }
        });
    }
}

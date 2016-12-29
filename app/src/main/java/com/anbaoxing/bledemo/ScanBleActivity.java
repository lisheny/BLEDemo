package com.anbaoxing.bledemo;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anbaoxing.autoble4_0.ScanActivity;

import java.util.ArrayList;

/**
 * 扫描蓝牙设备
 */
public class ScanBleActivity extends ScanActivity {

    private Button btnScan,btnConnect;
    private TextView tvShowBle;
    private ProgressBar myProgressBar;

    //设备信息数组
    private ArrayList<BluetoothDevice> mLeDevices;

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                if (message.getData().getBoolean("mScanning")) {
                    btnScan.setText("扫描中");
                    myProgressBar.setVisibility(View.VISIBLE);
                    btnConnect.setVisibility(View.GONE);
                    tvShowBle.setText("");
                } else {
                    btnScan.setText("扫描停止");
                    myProgressBar.setVisibility(View.INVISIBLE);
                    if (mLeDevices.size()>0) btnConnect.setVisibility(View.VISIBLE);

                    //把扫描到的设备show出来
                    String allBle = "";
                    for (int i =0; i<mLeDevices.size();i++ ){
                        allBle = allBle+"Mac地址："+String.valueOf(mLeDevices.get(i).getAddress())+" \n设备名："+
                                String.valueOf(mLeDevices.get(i).getName())+"\n";
                    }
                    tvShowBle.setText(allBle);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ble);

        mLeDevices = new ArrayList<BluetoothDevice>();

        btnScan = (Button) findViewById(R.id.btn_scan);
        btnConnect = (Button)findViewById(R.id.btn_connect);
        tvShowBle =(TextView)findViewById(R.id.tv_show_ble);
        myProgressBar = (ProgressBar) findViewById(R.id.my_progressbar);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //控制扫描
                if (ismScanning()) {
                    scanLeDevice(false);
                } else {
                    scanLeDevice(true);
                    myProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //把目标蓝牙信息传到ConnectBleActivity进行连接
                Intent intent = new Intent(ScanBleActivity.this, ConnectBleActivity.class);
                intent.putExtra(ConnectBleActivity.EXTRAS_DEVICE_NAME,  mLeDevices.get(0).getName());
                intent.putExtra(ConnectBleActivity.EXTRAS_DEVICE_ADDRESS,  mLeDevices.get(0).getAddress());
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 重写这个方法，获得扫描到的设备信息
     *
     * @param device     设备
     * @param rssi       信号
     * @param scanRecord ...
     */
    @Override
    protected void showScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        super.showScanDevice(device, rssi, scanRecord);
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
        Log.i("ScanBleActivity", "蓝牙Mac地址：" + device.getAddress() + " 设备名：" + device.getName());
    }

    /**
     * 重写这个方法得到扫描状态（注意：该方法是异步执行的）
     *
     * @param mScanning 扫描状态
     */
    @Override
    protected void scanStatus(Boolean mScanning) {
        super.scanStatus(mScanning);

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putBoolean("mScanning", mScanning);
        message.setData(bundle);
        handler.sendMessage(message);
        message.what = 1;
        Log.i("ScanBleActivity", String.valueOf(mScanning));
    }
}

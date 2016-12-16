package com.anbaoxing.bledemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anbaoxing.autoble4_0.BleUtils;
import com.anbaoxing.autoble4_0.ConnectActivity;
import com.anbaoxing.autoble4_0.ToastUtil;

public class ConnectBleActivity extends ConnectActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;

    private Button btnSendOrder,btnDisconnect,btnConnect;
    private TextView tvShowStatus ;

    private String
            myGattService ="0000fff0-0000-1000-8000-00805f9b34fb",
            myWriteCharacteristic = "0000fff2-0000-1000-8000-00805f9b34fb",
            myReadCharcteristic = "0000fff1-0000-1000-8000-00805f9b34fb";
    //要扫描的蓝牙的特有服务UUID
    private String scanServiceUUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ble);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //调用蓝牙功能必须在这里配置好UUID
        setmDeviceAddress(mDeviceAddress);
        setMyGattService(myGattService);
        setMyWriteCharacteristic(myWriteCharacteristic);
        setMyReadCharcteristic(myReadCharcteristic);
        setScanServiceUUID(scanServiceUUID);

        btnSendOrder = (Button)findViewById(R.id.btn_send_order);
        btnDisconnect = (Button)findViewById(R.id.btn_disconnect);
        btnConnect = (Button)findViewById(R.id.btn_connect);
        tvShowStatus = (TextView)findViewById(R.id.tv_ble_state);

        btnSendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()){
                    bluetoothControl("ff03F13428");
                }else {
                    ToastUtil.showToast(ConnectBleActivity.this,"蓝牙未连接");
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disConnect();
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    @Override
    protected void displayData(byte[] data) {
        super.displayData(data);
        if (data != null) {
            String s = BleUtils.byte2HexStr(data);
            Log.i("ConnectBleAty---->", "蓝牙返回数据：" + s);
            //转化为二进制字符串处理
//            String bity2binStr = BleUtils.byte2BinStr(data);
        }
    }

    @Override
    protected boolean bleStatus(Boolean isConnected) {
        tvShowStatus.setText("连接状态： "+String.valueOf(isConnected));
        Log.i("ConnectBleAty---->", "蓝牙状态：" + isConnected);
        return super.bleStatus(isConnected);
    }
}

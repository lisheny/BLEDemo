package com.anbaoxing.bledemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anbaoxing.autoble4_0.AutoConnectActivity;
import com.anbaoxing.autoble4_0.BleUtils;
import com.anbaoxing.autoble4_0.ToastUtil;

/**
 * 自动连接Demo show
 */
public class BleAutoConnectActivity extends AutoConnectActivity {

    private Button btnSendOrder,btnDisconnect,btnConnect;
    private TextView tvShowStatus ;

    private String mDeviceName = "LEPU BP";
    //这里的UUID配置是你要连接的蓝牙里特有的，这里展示的是一个血压计的例子
    private String
            myGattService ="0000fff0-0000-1000-8000-00805f9b34fb",
            myWriteCharacteristic = "0000fff2-0000-1000-8000-00805f9b34fb",
            myReadCharcteristic = "0000fff1-0000-1000-8000-00805f9b34fb";

    //要扫描的蓝牙的特有服务UUID
    private String scanServiceUUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //调用蓝牙功能必须在这里配置好UUID
        setmDeviceName(mDeviceName);
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
                    //将蓝牙指令以字符串形式输入即可，下面这条指令"ff03F13428" 是例子中让血压计进行测量
                    bluetoothControl("ff03F13428");
                }else {
                    ToastUtil.showToast(BleAutoConnectActivity.this,"蓝牙未连接");
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //断开蓝牙
                disConnect();
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //连接蓝牙
                connect();
            }
        });

    }

    /**
     * 重写这个方法获得蓝牙返回的数据
     * @param data 蓝牙返回的字节数组
     */
    @Override
    protected void displayData(byte[] data) {
        super.displayData(data);
        if (data != null) {
            String s = BleUtils.byte2HexStr(data);
            Log.i("BleAutoConnectAty---->", "蓝牙返回数据：" + s);
            //转化为二进制字符串处理
//            String bity2binStr = BleUtils.byte2BinStr(data);
        }
    }

    /**
     * 重写这个方法获得蓝牙的连接状态
     * @param isConnected .
     * @return  返回蓝牙状态
     */
    @Override
    protected boolean bleStatus(Boolean isConnected) {
        tvShowStatus.setText("连接状态： "+String.valueOf(isConnected));
        Log.i("BleAutoConnectAty---->", "蓝牙状态：" + isConnected);
        return super.bleStatus(isConnected);
    }
}

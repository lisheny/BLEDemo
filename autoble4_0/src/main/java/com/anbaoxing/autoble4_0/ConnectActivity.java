package com.anbaoxing.autoble4_0;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

/**
 * 手动连接Demo
 * 连接控制蓝牙
 * Created by LENOVO on 2016/12/16.
 */
public class ConnectActivity extends BaseActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private final static String TAG = AutoConnectActivity.class.getSimpleName();

    public boolean isConnected() {
        return isConnected;
    }

    //是否已连接
    private boolean isConnected = false;
    private String mDeviceName;
    private BluetoothAdapter mBluetoothAdapter;

    public String getScanServiceUUID() {
        return scanServiceUUID;
    }

    public void setScanServiceUUID(String scanServiceUUID) {
        this.scanServiceUUID = scanServiceUUID;
    }

    public String getMyReadCharcteristic() {
        return myReadCharcteristic;
    }

    public void setMyReadCharcteristic(String myReadCharcteristic) {
        this.myReadCharcteristic = myReadCharcteristic;
    }

    public String getMyWriteCharacteristic() {
        return myWriteCharacteristic;
    }

    public void setMyWriteCharacteristic(String myWriteCharacteristic) {
        this.myWriteCharacteristic = myWriteCharacteristic;
    }

    public String getMyGattService() {
        return myGattService;
    }

    public void setMyGattService(String myGattService) {
        this.myGattService = myGattService;
    }

    private BluetoothLeService mBluetoothLeService;

    private BluetoothGattService dataInteracctionService;
    private BluetoothGattCharacteristic writeCharacteristic, readCharacteristic;

    //蓝牙操作要用到的UUID，根据不同的蓝牙服务商定的，要在继承这个Activity的子Activity中配置
    //这里设了默认值仅为示范作用，并不会对调用有任何影响
    private String
            myGattService = "0000fff0-0000-1000-8000-00805f9b34fb",
            myWriteCharacteristic = "0000fff2-0000-1000-8000-00805f9b34fb",
            myReadCharcteristic = "0000fff1-0000-1000-8000-00805f9b34fb";

    //要扫描的蓝牙的特有服务UUID
    private String scanServiceUUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    public String getmDeviceAddress() {
        return mDeviceAddress;
    }

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    private String mDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册广播
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //绑定服务
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(getmDeviceAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        mBluetoothAdapter = null;
        mDeviceAddress = null;

        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.   用户选择不支持蓝牙。
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 写入蓝牙指令
     *
     * @param wriDate 要写入的指令
     */
    public boolean bluetoothControl(String wriDate) {
        try {
            mBluetoothLeService.setCharacteristicNotification(writeCharacteristic, true);
            Log.i("wriData", wriDate);
            byte[] bytes = BleUtils.getHexBytes(wriDate);
            writeCharacteristic.setValue(bytes);
            mBluetoothLeService.writeCharacteristic(writeCharacteristic);
            Log.i("指令写入中：", wriDate);
            return true;
        } catch (Exception e) {
            Log.e("指令写入不成功：", wriDate + " ==> " + e);
            return false;
        }
    }

    /**
     * 断开蓝牙
     */
    public void disConnect() {
        bleStatus(false);
        mBluetoothLeService.disconnect();
    }

    /**
     * 连接蓝牙
     */
    public void connect() {
        mBluetoothLeService.connect(mDeviceAddress);
    }

    /**
     * 接收蓝牙数据处理
     *
     * @param data 蓝牙返回的字节数组
     */
    protected void displayData(byte[] data) {

    }

    protected boolean bleStatus(Boolean isConnected) {
        return isConnected;
    }

    /**
     * Code to manage Service lifecycle.  代码管理服务生命周期。
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("MianActivity", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            // 自动连接到设备成功启动初始化
            mBluetoothLeService.connect(getmDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.STATUS_133);

        return intentFilter;
    }

    /**
     * 广播动态更新数据
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            /**
             * 连接错误 133 状态
             */
            if (BluetoothLeService.STATUS_133.equals(action)) {
                ToastUtil.showToast(ConnectActivity.this,"连接失败 状态 133 ，请重连");
            }
            /*
            蓝牙连接成功时接收到的广播
            */
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                bleStatus(true);

                Log.i(TAG, "蓝牙连接成功");
                ToastUtil.showToast(ConnectActivity.this, "蓝牙连接成功");
            }

            /*
            蓝牙断开
             */
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                bleStatus(false);

                Log.i(TAG, "蓝牙已断开连接");
                ToastUtil.showToast(ConnectActivity.this, "蓝牙已断开连接");
            }

           /*
           读取数据
            */
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))

            {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                displayData(data);
            }

            /*
            找到Gatt服务
             */
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
                try {
                    //蓝牙数据交互服务UUID
                    dataInteracctionService = mBluetoothLeService.getSupportedGattServices(UUID.fromString(getMyGattService()));

                    //写数据characteristic UUID：
                    writeCharacteristic = dataInteracctionService.getCharacteristic(UUID.fromString(getMyWriteCharacteristic()));

                    //读数据characteristic UUID：
                    readCharacteristic = dataInteracctionService.getCharacteristic(UUID.fromString(getMyReadCharcteristic()));

                    //打开读数据监听
                    mBluetoothLeService.setCharacteristicNotification(readCharacteristic, true);
                } catch (Exception e) {
                    Log.e("ConnectActivity", "无法配对读写特征值，请校正");
                    ToastUtil.showToast(ConnectActivity.this, "无法配对读写特征值，请校正");
                }

        }
    };
}

package com.anbaoxing.autoble4_0;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * (自动连接)
 * 控制蓝牙的一个管理Activity
 */
public class AutoConnectActivity extends BaseActivity {

    private final static String TAG = AutoConnectActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    //是否已连接
    private boolean isConnected = false;

    //true：需要继续扫描，false:停止扫描
    private boolean mScanning = true;

    private Timer mServiceTimer;
    private TimerTask mServiceTimeTask;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private String mDeviceName = "LEPU BP";

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

    public String getmDeviceName() {
        return mDeviceName;
    }

    public void setmDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean ismScanning() {
        return mScanning;
    }

    /**
     * 想要开启扫描的时候设为true
     *
     * @param mScanning 布尔值，是否扫描
     */
    public void setmScanning(boolean mScanning) {
        this.mScanning = mScanning;
    }

    public String getmDeviceAddress() {
        return mDeviceAddress;
    }

    /**
     * 设置想要连接的指定的蓝牙Mac地址
     *
     * @param mDeviceAddress 蓝牙Mac地址
     */
    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    public String getMyGattService() {
        return myGattService;
    }

    /**
     * 设置包含读和写特征值的服务UUID
     *
     * @param myGattService 服务UUID
     */
    public void setMyGattService(String myGattService) {
        this.myGattService = myGattService;
    }

    public String getMyWriteCharacteristic() {
        return myWriteCharacteristic;
    }

    /**
     * 设置写入蓝牙指令的UUID
     *
     * @param myWriteCharacteristic 写入蓝牙指令的UUID
     */
    public void setMyWriteCharacteristic(String myWriteCharacteristic) {
        this.myWriteCharacteristic = myWriteCharacteristic;
    }

    public String getMyReadCharcteristic() {
        return myReadCharcteristic;
    }

    /**
     * 设置读取蓝牙设备传输数据的UUID
     *
     * @param myReadCharcteristic 传输数据的UUID
     */
    public void setMyReadCharcteristic(String myReadCharcteristic) {
        this.myReadCharcteristic = myReadCharcteristic;
    }

    public String getScanServiceUUID() {
        return scanServiceUUID;
    }

    /**
     * 设置要扫描的蓝牙设备特有的服务的UUID
     *
     * @param scanServiceUUID 服务的UUID
     */
    public void setScanServiceUUID(String scanServiceUUID) {
        this.scanServiceUUID = scanServiceUUID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册广播
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.showToast(AutoConnectActivity.this, "手机不支持Ble");
            finish();
        }

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            ToastUtil.showToast(AutoConnectActivity.this, "手机不支持Ble");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //绑定服务
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //如果还需要继续扫描或蓝牙未连接则
        //延时ns后执行mServiceTimeTask里的scanLeDevice扫描
        if (mServiceTimer == null) {
            mServiceTimer = new Timer();
            mServiceTimeTask = new TimerTask() {
                @Override
                public void run() {
                    if (isConnected) {
                        scanLeDevice(false);
                    } else {
                        if (mScanning) {
                            scanLeDevice(true);
                            mScanning = false;
                        } else {
                            scanLeDevice(false);
                            mScanning = true;
                        }
                    }
                }
            };
            mServiceTimer.schedule(mServiceTimeTask, 5000, 5000);
        }

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(getmDeviceAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanLeDevice(false);

        mServiceTimer.cancel();
        mServiceTimeTask.cancel();
        mServiceTimer = null;
        mServiceTimeTask = null;
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
            Log.i("指令写入成功：", wriDate);
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

    private void scanLeDevice(final boolean enable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    //string类型转化为UUID，开始搜索时仅仅索搜拥有该UUID服务的设备
                    //特别说明：startLeScan(new UUID[]{uuids}, mLeScanCallback)
                    //这个方法需要Android 5.0以上，所以5.0以下但支持蓝牙4.0的话是
                    //扫描不到的
//                    String uuid = getScanServiceUUID();
//                    UUID uuids = UUID.fromString(uuid);
//                    mBluetoothAdapter.startLeScan(new UUID[]{uuids}, mLeScanCallback);
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    Log.d("ble", "正在扫描");
                } else {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.d("ble", "停止扫描");
                }
            }
        }).start();

    }

    /**
     * Device scan callback.    设备扫描回调。
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("设备名：", device.getName());
                        if (device.getName().equals(getmDeviceName())) {
                            mDeviceAddress = device.getAddress();

                            mScanning = false;
                            scanLeDevice(mScanning);
                            Log.i(TAG, "扫描到的设备：" + getmDeviceAddress());

                            //扫描到设备，发送广播进行连接
                            if (getmDeviceAddress() != null) {

                                //连接一个已存在的蓝牙通常是比较慢的，所以清理掉，建立新连接
                                if (mBluetoothLeService.mBluetoothGatt != null){
                                    mBluetoothLeService.close();
                                }

                                Intent intent = new Intent(BluetoothLeService.SCAN_BLE);
                                sendBroadcast(intent);
                            }
                        }
                    } catch (Exception ignored) {
                         Log.d(TAG,"找不到设备名");
                    }
                }
            });
        }
    };

    /**
     * Code to manage Service lifecycle.  管理服务生命周期。
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
        intentFilter.addAction(BluetoothLeService.SCAN_BLE);
        intentFilter.addAction(BluetoothLeService.STATUS_133);
        return intentFilter;
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
                Log.i(TAG,"5s 内自动重连");
            }
            /*
           蓝牙连接成功时接收到的广播
            */
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                bleStatus(true);

                Log.i(TAG, "蓝牙连接成功");
                ToastUtil.showToast(AutoConnectActivity.this, "蓝牙连接成功");
            }

            /*
            蓝牙断开
             */
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                bleStatus(false);
                Log.i(TAG, "蓝牙已断开连接");
                ToastUtil.showToast(AutoConnectActivity.this, "蓝牙已断开连接");
            }

            /*
            查找到蓝牙设备时，接受广播进行连接
             */
            if (BluetoothLeService.SCAN_BLE.equals(action)) {
                mBluetoothLeService.connect(getmDeviceAddress());
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
                    ToastUtil.showToast( AutoConnectActivity.this, "无法配对读写特征值，请校正");
                }
        }
    };
}

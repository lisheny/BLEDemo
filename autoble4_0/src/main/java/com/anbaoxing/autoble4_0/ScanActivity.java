package com.anbaoxing.autoble4_0;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * 手动连接Demo
 * 扫描设备
 * Created by LENOVO on 2016/12/16.
 */
public class ScanActivity extends BaseActivity {

    private BluetoothAdapter mBluetoothAdapter;

    public boolean ismScanning() {
        return mScanning;
    }

    public void setmScanning(boolean mScanning) {
        this.mScanning = mScanning;
    }

    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;

    // 定义扫描周期10s
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.showToast(ScanActivity.this, "手机不支持Ble");
            finish();
        }

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            ToastUtil.showToast(ScanActivity.this, "手机不支持Ble");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //如果蓝牙不可用，弹出蓝牙设置框
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //如果用户不打开蓝牙，结束该Activity
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 扫描设备
     * @param enable 开始/关闭 扫描
     */
    public void scanLeDevice(final boolean enable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    // 扫描周期10s 过去之后停止扫描
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScanning = false;
                            scanStatus(false);
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                    }, SCAN_PERIOD);

                    //这里可以用另一个方法，扫描特定蓝牙：startLeScan(new UUID[]{uuids}, mLeScanCallback)
                    mScanning = true;
                    scanStatus(true);
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                } else {
                    mScanning = false;
                    scanStatus(false);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }
        }).start();
    }

    /**
     * 扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showScanDevice(device,rssi,scanRecord);
                    Log.d("LeScanCallback",device.getAddress());
                }
            });
        }
    };

    /**
     * 展示扫描到的设备
     */
    protected void showScanDevice(final BluetoothDevice device, int rssi, byte[] scanRecord){

    }

    /**
     * 扫描状态
     * @param mScanning .
     */
    protected void scanStatus (Boolean mScanning){

    }
}

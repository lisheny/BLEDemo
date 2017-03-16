# BLEDemo
稍稍封装了一下的蓝牙蓝快开发，有助于做蓝牙的快速开发

> 使用方法（详情可看demo）：



1. 将 autoble4_0 作为依赖项目导入项目中
2. 新建一个扫描的 `ScanBleActivity` 继承 `ScanActivity` ；</br>
   调用 `scanLeDevice(ture)` 开始扫描，`scanLeDevice(false)` 停止扫描；</br>
   重写 `showScanDevice()` 方法，获得扫描到的设备信息；</br>
   重写 `scanStatus()` 方法得到扫描状态（注意：该方法是异步执行的）。
3. 新建一个连接的` ConnectBleActivity` 继承 `ConnectActivity` 配置相关参数：</br>
   
        @Override
           protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_connect_ble);

           //获得要连接的蓝牙
           final Intent intent = getIntent();
           mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
           mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

           //调用蓝牙功能必须在这里配置好UUID
           setmDeviceAddress(mDeviceAddress);
           setMyGattService(myGattService);
           setMyWriteCharacteristic(myWriteCharacteristic);
           setMyReadCharcteristic(myReadCharcteristic);
           setScanServiceUUID(scanServiceUUID);
           } 
发送指令：`bluetoothControl("ff03F13428")`； </br>
调用连接方法 `connect()`；</br>
调用断开连接方法 `disConnect()`；</br>
重写 `displayData(byte[] data)` 方法，获得蓝牙返回的数据；</br>
4. 完成。

###### 关于自动连接，只是将扫描和连接结合起来而已，同样新建一个自动连接的 Activity 继承 `AutoConnectActivity` ，配置和 `ConnectActivity` 差不多。

###### 蓝牙开发坑多开发过的人都知道，下面罗列出一些大坑，请查收：↓↓↓↓↓↓

[Android BLE开发中踩过的坑](http://www.jianshu.com/p/d0eedd17f2df)

[Android蓝牙4.0 BLE开发坑总结](http://m.blog.csdn.net/article/details?id=52459629)

[BLE开发的各种坑](http://www.race604.com/android-ble-tips/)



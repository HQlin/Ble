package com.example.lin.ble;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lin.DBHelper.DBCcontroller;
import com.example.lin.DBHelper.DatabaseHelper;
import com.example.lin.http.HttpProtocol;
import com.example.lin.service.BluetoothLeService;
import com.example.lin.service.SampleGattAttributes;


public class MainActivity extends Activity {
    public static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final static String TAG = MainActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    // 连接蓝牙配置
    private static BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;


    ///////////////////////////////////////以上为蓝牙通信///////////////////////////////////////////////////
    private ImageButton loginBtn,userTypeBtn, openDoorBtn, sendSmsBtn, getKeyBtn,createKey,destroyKey;//主要功能按钮
    private EditText numEt, timeEt;//发送秘钥的编辑框
    private Button numBtn;//发送秘钥的按钮
    private TextView loginTv;//记录登陆状态
    private View layout;//分配密钥窗口
    public static final int REQUSET = 1;

    private String userNameSting,smsString,userNumberSting,c_phoneString;
    private String getkeyResult;//获取密钥结果

    public static String IP_POST = "jmwill.imwork.net:30968";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.
        // Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        init();

        // 扫描线程只需要启动一个
        ScanLeDeviceThread deviceThread = new ScanLeDeviceThread();
        deviceThread.start();
    }

    // 设置蓝牙搜索
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    System.out.println("device123:" + device);
                    // 自动连接
                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }

                    // 创建连接
                    BluetoothLeService.mDeviceAddress = device.getAddress();
                    System.out.println("BluetoothLeService.mDeviceAddress:"
                            + BluetoothLeService.mDeviceAddress);
                    // 模拟秘钥
                    if(BluetoothLeService.mDeviceAddress.equals("00:15:83:00:1C:46")){
                        Intent gattServiceIntent = new Intent(MainActivity.this,
                            BluetoothLeService.class);
                        gattServiceIntent.putExtra("mDeviceAddress",
                                BluetoothLeService.mDeviceAddress);
                        if (mBluetoothLeService != null) {
                            final boolean result = mBluetoothLeService
                                    .connect(BluetoothLeService.mDeviceAddress);
                            Log.d(TAG, "Connect request result=" + result);
                        }
                        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    }

                }
            });
        }
    };

    // 线程循环蓝牙搜索
    class ScanLeDeviceThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            while (true) {
                scanLeDevice(true);
                System.out.println("ScanLeDeviceThread");
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                System.out.println("连接");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;
                System.out.println("断开");
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                System.out.println(intent
                        .getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // 更新GATT的状态
    static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume()");

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not
        // currently enabled,
        // fire an intent to display a dialog asking the user to grant
        // permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService
                    .connect(BluetoothLeService.mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    // 得到服务对象
    public static BluetoothLeService getmBluetoothLeService() {
        return mBluetoothLeService;
    }
    /**
     * 初始化（按钮设置和监听设置）
     */
    void init() {
        loginTv = (TextView) findViewById(R.id.logintv);
        userTypeBtn = (ImageButton) findViewById(R.id.type1);
        openDoorBtn = (ImageButton) findViewById(R.id.type2);
        openDoorBtn.setBackgroundResource(R.drawable.type2);
        sendSmsBtn = (ImageButton) findViewById(R.id.sendsms);
        sendSmsBtn.setBackgroundResource(R.drawable.sendsms);

        getKeyBtn = (ImageButton) findViewById(R.id.getkey);
        getKeyBtn.setBackgroundResource(R.drawable.getkey);
        loginBtn = (ImageButton) findViewById(R.id.login);
        loginBtn.setBackgroundResource(R.drawable.login);

        createKey = (ImageButton)findViewById(R.id.createKey);
        destroyKey = (ImageButton)findViewById(R.id.destroyKey);


        userTypeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                updateDBtype();
            }
        });

        openDoorBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //getmBluetoothLeService().writeCharacteristic("OPEN");
                if (DBCcontroller.queryBD(MainActivity.this, "type")
                        .equals("2")) {
                    if(!DBCcontroller.queryBD(MainActivity.this, "keyduration").equals("no time")){
                        if (!DBCcontroller.queryBD(MainActivity.this, "keyduration")
                                .equals("infinite time")) {
                            long keyduration = Long.parseLong(DBCcontroller.queryBD(MainActivity.this, "keyduration"));
                            long time = System.currentTimeMillis() - keyduration;
                            System.out.println("time:" + time + "," + keyduration + "," + DBCcontroller.queryBD(MainActivity.this, "keyduration"));
                            // 手动开门
                            if (time < 0) {
                                if (getmBluetoothLeService() != null) {
                                    bleSend(DBCcontroller.queryBD(MainActivity.this, "key"));
                                    //getmBluetoothLeService().writeCharacteristic(DBCcontroller.queryBD(MainActivity.this, "key"));
                                    Toast.makeText(MainActivity.this, "手动开锁！",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "手动开锁失败！",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(MainActivity.this, "没有开锁权限！",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (getmBluetoothLeService() != null) {
                                getmBluetoothLeService().writeCharacteristic(
                                        DBCcontroller.queryBD(MainActivity.this,
                                                "key"));
                                Toast.makeText(MainActivity.this, "手动开锁！",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "手动开锁失败！",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });
        sendSmsBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LayoutInflater inflater = getLayoutInflater();
                layout = inflater.inflate(R.layout.sendsms,
                        (ViewGroup) findViewById(R.id.dialog));
                numEt = (EditText) layout.findViewById(R.id.etnum);
                timeEt = (EditText) layout.findViewById(R.id.ettime);
                numBtn = (Button)layout.findViewById(R.id.numbt);
                numBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        startActivityForResult(new Intent(
                                Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
                    }
                });
                new AlertDialog.Builder(MainActivity.this).setTitle("请输入")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(layout)
                        .setPositiveButton("确定", new mylistener())
                        .setNegativeButton("取消", null).show();
            }

        });
        getKeyBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (smsString != null) {
                    smsString = smsString.substring(0, 11);
                    System.out.println("sms: " + smsString);
                }
                GetkeyHandle handle = new GetkeyHandle();
                handle.start();
                Toast.makeText(MainActivity.this, "正在获取秘钥，请稍等！",
                        Toast.LENGTH_SHORT).show();
            }
        });
        loginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method
                Intent intent = new Intent(MainActivity.this,
                        LoginActivity.class);
                // 发送意图标示为REQUSET=1
                startActivityForResult(intent, REQUSET);
            }
        });

        createKey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,InstallLockActivity.class);
                intent.putExtra("ACCOUNT",DBCcontroller.queryBD(MainActivity.this, "phonenum"));
                intent.putExtra("PWD",DBCcontroller.queryBD(MainActivity.this, "pwd"));
                startActivity(intent);
            }
        });

        destroyKey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DestroyKeyActivity.class);
                intent.putExtra("ACCOUNT", DBCcontroller.queryBD(MainActivity.this, "phonenum"));
                intent.putExtra("PWD", DBCcontroller.queryBD(MainActivity.this, "pwd"));
                startActivity(intent);
            }
        });

        if (!DBCcontroller.queryBD(MainActivity.this, "id").equals("1"))
            DBCcontroller.insertDB(MainActivity.this);
        updateTypeButton();

    }

    // Code to manage Service lifecycle.
    // 蓝牙后台服务连接
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(BluetoothLeService.mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    /**
     * 对话框分配秘钥监听
     * @author lin
     *
     */
    class mylistener implements android.content.DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            c_phoneString = numEt.getText().toString();
            SendsmsHandle handle = new SendsmsHandle();
            handle.start();
        }

    }
    /**
     * 分配秘钥线程
     * @author lin
     *
     */
    class SendsmsHandle extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            boolean isSucceed = false;
            List params = new ArrayList();
            params.add(new BasicNameValuePair("account",DBCcontroller.queryBD(MainActivity.this, "phonenum")));
            params.add(new BasicNameValuePair("psw", DBCcontroller.queryBD(MainActivity.this, "pwd")));
            params.add(new BasicNameValuePair("owner", c_phoneString));
            params.add(new BasicNameValuePair("lock_seq", "123"));
            params.add(new BasicNameValuePair("validity", timeEt.getText().toString()));

            System.out.println("distribute_key:" + DBCcontroller.queryBD(MainActivity.this, "phonenum") + " " +
                    DBCcontroller.queryBD(MainActivity.this, "pwd") + " " +
                    c_phoneString + " " +
                    timeEt.getText().toString());

            String result = HttpProtocol.gotoHttpPost("http://"+ IP_POST +"/api/distribute_key/", params);
            System.out.println("distribute_key:"+result);
            if (result!=null){
                if (result.equals("0")){
                    isSucceed = true;
                }
            }

            if (isSucceed) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            } else {
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            }
        }

    }
    /**
     * 回馈 分配秘钥 和 获得秘钥 状态信息
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(MainActivity.this, "分配key失败！", Toast.LENGTH_LONG)
                            .show();
                    break;
                case 1:
                    // 发短信
                    sendSMS(numEt.getText().toString(), "智能锁通知信息！");
                    Toast.makeText(MainActivity.this, "分配key成功！如果数秒后，还未收到短信通知，请检查电话卡信号", Toast.LENGTH_LONG)
                            .show();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "获得key成功！期限为 " + getkeyResult, Toast.LENGTH_LONG)
                            .show();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, "获得key失败！", Toast.LENGTH_LONG)
                            .show();
                    break;
                case 4:
                    Toast.makeText(MainActivity.this, "添加门锁成功！", Toast.LENGTH_LONG)
                            .show();
                    break;
                case 5:
                    Toast.makeText(MainActivity.this, "添加门锁失败！", Toast.LENGTH_LONG)
                            .show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    /**
     * 获取秘钥线程
     * @author lin
     *
     */
    class GetkeyHandle extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            boolean isSucceed = false;
            int index = -1;
            List params = new ArrayList();
            params.add(new BasicNameValuePair("account", DBCcontroller.queryBD(MainActivity.this, "phonenum")));
            params.add(new BasicNameValuePair("psw", DBCcontroller.queryBD(MainActivity.this, "pwd")));
            params.add(new BasicNameValuePair("lock_seq", "123"));

            System.out.println("apply_key: " + DBCcontroller.queryBD(MainActivity.this, "phonenum") + " " + DBCcontroller.queryBD(MainActivity.this, "pwd"));

            String result = HttpProtocol
                    .gotoHttpPost(
                            "http://"+IP_POST+"/api/apply_key/",
                            params);
            System.out.println("apply_key: " + result);
            if(result!=null){
                index = result.indexOf(",");
                if (index != -1){
                    String spStr[] = result.split(",");
                    isSucceed = true;
                    getkeyResult = result;
                    //spStr[0]为日期，spStr[1]为秘钥
                    System.out.println("lin:" + spStr[0] + "," + spStr[1]);
                    if("infinite time".equals(spStr[0])){
                         DBCcontroller.updateDBtype(MainActivity.this, spStr[1], Integer.parseInt(DBCcontroller.queryBD(MainActivity.this, "type")), "123", DBCcontroller.queryBD(MainActivity.this, "phonenum"), DBCcontroller.queryBD(MainActivity.this, "pwd"), ""+spStr[0]);
                    } else{
                        try {
                            DBCcontroller.updateDBtype(MainActivity.this, spStr[1], Integer.parseInt(DBCcontroller.queryBD(MainActivity.this, "type")), "123", DBCcontroller.queryBD(MainActivity.this, "phonenum"), DBCcontroller.queryBD(MainActivity.this, "pwd"), ""+getTimestamp(spStr[0]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (isSucceed) {
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            } else {
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        }

    }

    /**
     * yyyy-MM-dd HH:mm:ss时间格式转时间戳
     * @param s
     * @return
     * @throws ParseException
     */
    public long getTimestamp(String s) throws ParseException {

        Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse(s);
        Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("1970-01-01 08:00:00");
        long l = date1.getTime() - date2.getTime() > 0 ? date1.getTime()
                - date2.getTime() : date2.getTime() - date1.getTime();

        return l;
    }

    /**
     * 直接调用短信接口发短信
     * @param phoneNumber
     * @param message
     */
    public void sendSMS(String phoneNumber,String message){
        String SENT = "sms_sent";
        String DELIVERED = "sms_delivered";

        PendingIntent sentPI = PendingIntent.getActivity(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getActivity(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.i("====>", "Activity.RESULT_OK");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.i("====>", "RESULT_ERROR_NO_SERVICE");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.i("====>", "RESULT_ERROR_NULL_PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("====>", "RESULT_ERROR_RADIO_OFF");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.i("====>", "RESULT_OK");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("====>", "RESULT_CANCELED");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager smsm = SmsManager.getDefault();
        smsm.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode标示请求的标示 resultCode表示有数据
        if (requestCode == MainActivity.REQUSET && resultCode == RESULT_OK) {
            if (data != null) {
                if (!data.getStringExtra(LoginActivity.PHONENUM).equals("")) {
                    DBCcontroller.updateDBtype(MainActivity.this,
                            "OPEN",
                            2,
                            null,
                            data.getStringExtra(LoginActivity.PHONENUM),
                            data.getStringExtra(LoginActivity.PWD),
                            "no time");
                    loginTv.setText("登录成功");
                    updateTypeButton();
                }
            }
        }
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            ContentResolver reContentResolverol = getContentResolver();
            Uri contactData = data.getData();
            @SuppressWarnings("deprecation")
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            userNameSting = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                    null,
                    null);
            while (phone.moveToNext()) {
                userNumberSting = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numEt.setText(userNumberSting);
            }

        }
    }
    /**
     * 更新 模式 按钮图标
     */
    void updateTypeButton() {
        if(DBCcontroller.queryBD(MainActivity.this, "phonenum")!=null){
            if (DBCcontroller.queryBD(MainActivity.this, "type").equals("1")) {
                userTypeBtn.setBackgroundResource(R.drawable.type1);
                openDoorBtn.setBackgroundResource(R.drawable.type22);
                openDoorBtn.setClickable(false);
            } else if (DBCcontroller.queryBD(MainActivity.this, "type").equals("2")){
                userTypeBtn.setBackgroundResource(R.drawable.type2);
                openDoorBtn.setBackgroundResource(R.drawable.type2);
                openDoorBtn.setClickable(true);
            }
            userTypeBtn.setClickable(true);
            createKey.setBackgroundResource(R.drawable.login);
            createKey.setClickable(true);
            destroyKey.setBackgroundResource(R.drawable.login);
            destroyKey.setClickable(true);
            sendSmsBtn.setBackgroundResource(R.drawable.sendsms);
            sendSmsBtn.setClickable(true);
        }else{
            userTypeBtn.setBackgroundResource(R.drawable.type11);
            userTypeBtn.setClickable(false);
            openDoorBtn.setBackgroundResource(R.drawable.type22);
            openDoorBtn.setClickable(false);
            createKey.setBackgroundResource(R.drawable.login1);
            createKey.setClickable(false);
            destroyKey.setBackgroundResource(R.drawable.login1);
            destroyKey.setClickable(false);
            sendSmsBtn.setBackgroundResource(R.drawable.sendsms1);
            sendSmsBtn.setClickable(false);
        }
    }
    /**
     * 更新 模式数据存储
     */
    void updateDBtype() {
        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
                "db_name", 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (DBCcontroller.queryBD(MainActivity.this, "type").equals("1")) {
            values.put("type", 2);
            db.update("user", values, "id = ?", new String[]{"1"});
            Toast.makeText(MainActivity.this, "手动开锁模式！", Toast.LENGTH_SHORT)
                    .show();
            userTypeBtn.setBackgroundResource(R.drawable.type2);
            openDoorBtn.setBackgroundResource(R.drawable.type2);
            openDoorBtn.setClickable(true);
        } else {
            values.put("type", 1);
            db.update("user", values, "id = ?", new String[]{"1"});
            userTypeBtn.setBackgroundResource(R.drawable.type1);
            Toast.makeText(MainActivity.this, "自动开锁模式！", Toast.LENGTH_SHORT)
                    .show();
            openDoorBtn.setBackgroundResource(R.drawable.type22);
            openDoorBtn.setClickable(false);
        }
        db.close();
    }

    // 设置返回监听
//    @Override
//    public void onBackPressed() {
//        // TODO Auto-generated method stub
//        // super.onBackPressed();
//        Toast.makeText(this, "禁止退出！", Toast.LENGTH_LONG).show();
//        // return ;
//    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME,
                    SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME,
                        SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        Toast.makeText(MainActivity.this,"conn yes!",Toast.LENGTH_SHORT).show();
    }

    public static void bleSend(String data) {
        byte[] value = new byte[6];
        value[0] = (byte) 0x00;
        byte[] WriteBytes = new byte[6];
       try{
           // 得到发送属性
           BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(3).get(0);
           if(!characteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb"))
               System.out.println("characteristic erro!");
           System.out.println("send characteristic:" + characteristic.getUuid());

           if (data != null) {
               // write string
               WriteBytes = data.getBytes();
               characteristic.setValue(value[0],BluetoothGattCharacteristic.FORMAT_UINT8, 0);
               characteristic.setValue(WriteBytes);
               mBluetoothLeService.writeCharacteristic(characteristic);
           }
       }catch (Exception e) {
           e.fillInStackTrace();
       }

    }
}

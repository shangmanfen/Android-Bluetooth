package com.mingrisoft.myapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private SharedPreferences pref;private SharedPreferences.Editor editor;
    private BluetoothAdapter mBluetoothAdapter;  // 获取到蓝牙适配器
    private List<String> bluetoothDevices = new ArrayList<String>();// 用来保存搜索到的设备信息
    private ListView lvDevices;
    private ArrayAdapter<String> arrayAdapter;// ListView的字符串数组适配器
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // UUID，蓝牙建立链接需要的
    private BluetoothDevice selectDevice;// 选中发送数据的蓝牙设备
    public BluetoothSocket clientSocket;// 获取到选中设备的客户端串口
    ClientThread clientThread;// 服务端利用线程不断接受客户端信息
    public Context context; View view11;
    public static MainActivity instance2 = null;
    TextView press,temp,pulse,location;boolean IsPaired=false;
    private OutputStream os;// 获取到输出流
    private long firstTime=0;  //记录第几次点击返回
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis()-firstTime>2000){
            Toast.makeText(MainActivity.this,"再次点击返回退出",Toast.LENGTH_SHORT).show();
            firstTime=System.currentTimeMillis();
        }else{
            mBluetoothAdapter.disable();
            MainActivity.this.finish();
            System.exit(0);
        }
    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                Log.e(getPackageName(), "没有权限，请求权限");
                return;
            }
            Log.e(getPackageName(), "已有定位权限");
            //这里可以开始搜索操作
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(getPackageName(), "开启权限permission granted!");
                    //这里可以开始搜索操作
                } else {
                    Log.e(getPackageName(), "没有定位权限，请先开启!");
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        context = getApplicationContext();
        instance2 = this;pref= PreferenceManager.getDefaultSharedPreferences(this);editor=pref.edit();
        press=findViewById(R.id.press);pulse=findViewById(R.id.pulse);temp=findViewById(R.id.temp);location=findViewById(R.id.location);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 获取到蓝牙默认的适配器
        LayoutInflater inflater = getLayoutInflater();
        view11 = inflater.inflate(R.layout.activity_dialog, null);
        lvDevices = view11.findViewById(R.id.foundList);
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1,bluetoothDevices);  // 为listview设置字符换数组适配器
        lvDevices.setAdapter(arrayAdapter);// 为listView绑定适配器
        lvDevices.setOnItemClickListener(this);
        mBluetoothAdapter.enable();
        clientThread = new ClientThread(clientSocket, handler);
    }
    public  void SearchE(View v){
        if(pairing())
            IsPaired=true;
        else
            IsPaired=false;
    }
    private boolean pairing(){
        try {
            mBluetoothAdapter.enable();
            // 点击搜索周边设备，如果正在搜索，则暂停搜索
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();// 用Set集合保持已绑定的设备
            if (devices.size() > 0) {
                for (BluetoothDevice bluetoothDevice : devices) {
                    // 保存到arrayList集合中
                    bluetoothDevices.add(bluetoothDevice.getName() + ":"+ bluetoothDevice.getAddress() + "\n");
                }
            }
            // 因为蓝牙搜索到设备和完成搜索都是通过广播来告诉其他应用的// 这里注册找到设备和完成搜索广播
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            showdialog();
            return true;
        }catch (Exception e){
            return false;
        }
    }
    AlertDialog dialog;AlertDialog.Builder builder;
    private void showdialog(){
        if(dialog==null){
            builder = new AlertDialog.Builder(MainActivity.this);
        //builder.setIcon(R.drawable.logo);
        //builder.setTitle("选择设备");
            builder.setView(view11);
            dialog=builder.show();
        }
        else
            dialog.show();
    }
    @Override    // 点击listView中的设备进行配对
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        Toast.makeText(MainActivity.this,"正在配对，请稍后",Toast.LENGTH_SHORT).show();
        // 获取到这个设备的信息
        String s = arrayAdapter.getItem(position);
        // 对其进行分割，获取到这个设备的地址
        String address = s.substring(s.indexOf(":") + 1).trim();
        // 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        selectDevice = mBluetoothAdapter.getRemoteDevice(address);//通过地址获取到该设备
        try{
            clientSocket = selectDevice.createRfcommSocketToServiceRecord(MY_UUID);// 获取到客户端接口
            clientSocket.connect();// 向服务端发送连接
        }catch (Exception e){}
        Toast.makeText(this, "已连接" + selectDevice.getName(), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        Button startAccept=findViewById(R.id.startAccept);startAccept.setVisibility(View.VISIBLE);
        Button button=findViewById(R.id.button);button.setVisibility(View.GONE);
    }
    private boolean SendMessage(String a){
        if(IsPaired){
        try {
            os = clientSocket.getOutputStream(); // 获取到输出流，向外写数据
            if (os != null) {
                try {
                    String text = a;
                    byte[] buf=text.getBytes();
                    os.write(text.getBytes());
                } catch (IOException e) {
                    IsPaired=false;
                    Toast.makeText(this, "蓝牙连接中断,请重新配对", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            Toast.makeText(this, "要与拐杖配对哦！", Toast.LENGTH_SHORT).show();
           return false;
        } }else{
            Toast.makeText(this, "要与拐杖配对哦！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    public void GetData(View v){
        if(SendMessage("A")) {
            try {
                clientThread.start();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "接收异常！", Toast.LENGTH_SHORT).show();
            }
            if (flag == 1) {
                String a = chatStr.substring(0, 3) + "/" + chatStr.substring(3, 5);
                press.setText(a);
                a = chatStr.substring(5, 7);
                pulse.setText(a);
                a = chatStr.substring(7, 11);
                temp.setText(a);
            }
        }else showdialog();
    }
    public void GetGPS(View v){
        if(SendMessage("B")){
            try {
                clientThread.start();
            }catch (Exception e){Toast.makeText(MainActivity.this,"接收异常！",Toast.LENGTH_SHORT).show();}
            if(flag==2){
                String longitude=chatStr.substring(0,10);
                String latitude=chatStr.substring(10,19);
                Float lon=convertToFloat(longitude,0);
                Float la=convertToFloat(latitude,0);
                location.setText("经度："+longitude+" \n纬度："+latitude);
                editor.putFloat("longitude",lon);
                editor.putFloat("latitude",la);
                editor.commit();
                startActivity(new Intent(MainActivity.this,Map.class));
            }
        }
        else showdialog();
    }
    String chatStr="";int flag=0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            chatStr = chatStr+msg.getData().getString("str");//接收数据
            chatStr=chatStr.trim();
            if(chatStr.length()>=10&chatStr.length()<=18)
                flag=1;
            else if(chatStr.length()>=19)
                flag=2;
            else flag=0;
            Toast.makeText(MainActivity.this, chatStr.trim(), Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(clientThread);
        }
    };
    // 注册广播接收者
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();// 获取到广播的action
            // 判断广播是搜索到设备还是搜索完成
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);// 找到设备后获取其设备
                // 判断这个设备是否是之前已经绑定过了，如果是则不需要添加，在程序初始化的时候已经添加了
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 设备没有绑定过，则将其保持到arrayList集合中
                    bluetoothDevices.add(device.getName() + ":"+ device.getAddress() + "\n");
                    arrayAdapter.notifyDataSetChanged();// 更新字符串数组适配器，将内容显示在listView中
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                //Toast.makeText(MainActivity.this,"搜索完成",Toast.LENGTH_SHORT).show();
            }
        }
    };
    //把String转化为float
    public static float convertToFloat(String number, float defaultValue)
    {
        if (TextUtils.isEmpty(number))
        {
            return defaultValue;
        }
        try {
            return Float.parseFloat(number);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
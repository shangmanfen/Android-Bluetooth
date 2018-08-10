package com.mingrisoft.myapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Lanyakongzhi extends AppCompatActivity {
    private int aaa=0;
    public static Lanyakongzhi instance3=null;
    private static final String TAG = "CAR";
    private static final boolean D = true;
    private BluetoothAdapter mB = null;
    private BluetoothDevice device;
    private BluetoothSocket btsocket = null;
    private OutputStream outStream = null;
    private SharedPreferences sp;
    private String address;
    TextView xinlv;
    TextView xueya;
    TextView tiwen;
    TextView tieshi;
    Button button;
    public Context context;

    //Intent intent = new Intent();
    //String result = intent.getStringExtra("textViewLabel");
    private long firstTime=0;  //记录第几次点击返回
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis()-firstTime>2000){
            Toast.makeText(Lanyakongzhi.this,"再次点击返回退出",Toast.LENGTH_SHORT).show();
            firstTime=System.currentTimeMillis();
        }else{
            finish();
            StartActivity.instance.finish();
            System.exit(0);
        }
    }
    //String address=result;
    //private static String address="result";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanyakongzhi);
        //context.getApplicationContext();
        //BluetoothDevice device=mB.getRemoteDevice(address);
        //final ClientThread clientThread=new ClientThread(device,context);
            //Toast.makeText(Lanyakongzhi.this, "蓝牙地址为空", Toast.LENGTH_SHORT).show();
       xinlv=(TextView)findViewById(R.id.textView4);
       xueya=(TextView)findViewById(R.id.textView5);
       tiwen=(TextView)findViewById(R.id.textView6);
       tieshi=(TextView)findViewById(R.id.textView7);
       button=(Button) findViewById(R.id.button);
        MainActivity.instance2.finish();
    }
    ConnectedThread connectedThread;
    // UUID，蓝牙建立链接需要的
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    // 为其链接创建一个名称
    private final String NAME = "Bluetooth_Socket";
    // 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
    private BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    public BluetoothSocket clientSocket;
    public void aa(View view){
        try
        {
            clientSocket = selectDevice
                    .createRfcommSocketToServiceRecord(MY_UUID);
        }catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
            Toast.makeText(Lanyakongzhi.this, "正在获取数据...", Toast.LENGTH_SHORT).show();
            //Handler handler = new Handler();
        connectedThread = new ConnectedThread(clientSocket, handler);
        connectedThread.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 通过msg传递过来的信息，吐司一下收到的信息
            String chatStr = msg.getData().getString("str");//接收数据
            String A="A";String D="D";
            if(A==chatStr.substring(0,1)&&D==chatStr.substring(chatStr.length()-1,chatStr.length()))
            {
                Toast.makeText(Lanyakongzhi.this, chatStr, Toast.LENGTH_SHORT).show();
            }
        }
    };
}

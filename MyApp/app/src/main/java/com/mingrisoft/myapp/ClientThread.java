package com.mingrisoft.myapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
/*
已建立连接后启动的线程，需要传进来两个参数
socket用来获取输入流，读取远程蓝牙发送过来的消息
handler用来在收到数据时发送消息
*/
public class ClientThread extends Thread {

    private static final int CONNECT_BREAKDOWN = 9;//连接中断
    private static final int RECEIVE_MSG = 7;
    private static final int SEND_MSG=8;
    private boolean isStop;
    private BluetoothSocket socket;
    private Handler handler;
    private InputStream is;
    private OutputStream os;
    int size;
    public ClientThread(BluetoothSocket s,Handler h){
        socket=s;
        handler=h;
        isStop=false;
    }
    public void run(){
        System.out.println("connectedThread.run()");
        byte[] buf;
        while(!isStop){
            size=0;
            buf=new byte[2048];
            try {
                is=socket.getInputStream();
                System.out.println("等待数据");
                size=is.read(buf);
                System.out.println("读取了一次数据");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isStop=true;
                handler.sendEmptyMessage(CONNECT_BREAKDOWN);
            }
            if(size>0){
                //把读取到的数据放进Bundle再放进Message，然后发送出去
                sendMessageToHandler(buf, RECEIVE_MSG);
            }
        }
    }

    private void sendMessageToHandler(byte[] buf,int mode){//将接收到的数据传输到mainActivity中

        String msgStr=new String(buf);//接收到的数据以ascii码显示
        //String msgStr=bytes2HexString(buf,size);//接收到的数据以16进制数显示

        Bundle bundle=new Bundle();
        bundle.putString("str", msgStr);
        Message msg=new Message();
        msg.setData(bundle);
        msg.what=mode;
        handler.sendMessage(msg);
    }

 public void write(byte[] buf){
          try {
              os=socket.getOutputStream();
              os.write(buf);
          } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
          System.out.println(buf.length+"---");
          sendMessageToHandler1(buf, SEND_MSG);
      }

  private void sendMessageToHandler1(byte[] buf,int mode){//将发送的数据传输到mainActivity中
        String msgStr=new String(buf);//接收到的数据以ascii码显示
        Bundle bundle=new Bundle();
        bundle.putString("str", msgStr);
        Message msg=new Message();
        msg.setData(bundle);
        msg.what=mode;
        handler.sendMessage(msg);
    }

    private static String bytes2HexString(byte[] b,int size){
        String ret = "";
        for(int i=0;i<size;i++){
            String hex=Integer.toHexString(b[i]&0xFF);
            if(hex.length()==1){
                hex='0'+hex;
            }
            ret+=hex.toUpperCase();
        }
        return ret;

    }
}



package cn.edu.ouc.wificonnect;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by ALEX.DON.SCOFIELD on 2016/10/17.
 * SocketClient
 */

class SocketClient {
    private InetAddress hostIP;
    private int hostPort;
    private Socket client;
    private Handler clientHandler;
    private Message msg;
    private byte[] sendBuffer = null;

    boolean isConnected() {
        return (client.isConnected() && (!client.isClosed()));
    }

    private Context mainContext;

    SocketClient(Handler handler, Context context) {
        client = new Socket();
        msg = new Message();
        this.clientHandler = handler;
        mainContext = context;
    }

    void connect(InetAddress ipAddress, int portNum) {
        hostIP = ipAddress;
        hostPort = portNum;
        new Thread(connectThread).start();
    }

    void disconnect() {
        if (isConnected()) {
            try {
                client.close();
            } catch (IOException e) {
                Log.e("Error", Log.getStackTraceString(e));
            }
        }
    }

    void send(byte[] buffer) {
        sendBuffer = buffer;
        new Thread(sendThread).start();
    }

    private Runnable connectThread = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 0x01;
            msg.obj = mainContext.getString(R.string.msg_connectsuccess);
            try {
                client = new Socket();
                client.connect(new InetSocketAddress(hostIP, hostPort), 1000);
                ReceiveThread receiveThread = new ReceiveThread(client);
                receiveThread.start();
            } catch (SocketTimeoutException e) {
                Log.e("Error", Log.getStackTraceString(e));
                msg.obj = mainContext.getString(R.string.msg_timeout);
            } catch (Exception e) {
                Log.e("Error", Log.getStackTraceString(e));
                msg.obj = mainContext.getString(R.string.msg_connectfail);
            }
            clientHandler.sendMessage(msg);
        }
    };
    private Runnable sendThread = new Runnable() {
        @Override
        public void run() {
            try {
                OutputStream outputStream = client.getOutputStream();
                outputStream.write(sendBuffer);
            } catch (IOException e) {
                Message msg = new Message();
                msg.what = 0x01;
                msg.obj = e.toString();
                clientHandler.sendMessage(msg);
                Log.e("Error", Log.getStackTraceString(e));
            }
        }
    };

    private class ReceiveThread extends Thread {
        private InputStream inputStream = null;
        private byte[] buffer;
        private String str = null;

        ReceiveThread(Socket s) {
            try {
                inputStream = s.getInputStream();
            } catch (IOException e) {
                Log.e("Error", Log.getStackTraceString(e));
            }
        }

        @Override
        public void run() {
            while (isConnected()) {
                buffer = new byte[512];
                try {
                    if (inputStream.read(buffer) > 0)
                        str = new String(buffer, "UTF-8").trim();
                } catch (IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    break;
                }
                Message msg = new Message();
                msg.what = 0x02;
                msg.obj = str;
                clientHandler.sendMessage(msg);
            }
            msg.what = 0x01;
            msg.obj = mainContext.getString(R.string.text_unconnected);
            disconnect();
            clientHandler.sendMessage(msg);
        }
    }
}

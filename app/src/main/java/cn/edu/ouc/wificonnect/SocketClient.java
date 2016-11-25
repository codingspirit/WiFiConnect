package cn.edu.ouc.wificonnect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.zip.Inflater;

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
        final int competeLength = 921654;
        private byte[] buffer = new byte[1024];
        private byte[] frame = new byte[competeLength];
        private int ptr = 0;
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
                try {
                    int dataLength = inputStream.available();
                    if (dataLength > 0) {
                        if (dataLength < 1024 && ptr == 0) {
                            if (inputStream.read(buffer) > 0)
                                str = new String(buffer, "UTF-8").trim();
                            Message msg = new Message();
                            msg.what = 0x02;
                            msg.obj = str;
                            clientHandler.sendMessage(msg);
                        } else if (dataLength >= 1024) {//long data such as picture
                            if (ptr < competeLength) {
                                if ((ptr + dataLength) < competeLength)
                                    ptr += inputStream.read(frame, ptr, dataLength);
                                else
                                    ptr += inputStream.read(frame, ptr, competeLength - ptr);
                            } else {
                                Message msg = new Message();
                                //byte[] unzip = unZipByte(frame);
                                Bitmap bmp = BitmapFactory.decodeByteArray(frame, 0, frame.length);
                                msg.what = 0x03;
                                msg.obj = bmp;
                                clientHandler.sendMessage(msg);
                                ptr = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    break;
                }
            }
            msg.what = 0x01;
            msg.obj = mainContext.getString(R.string.text_unconnected);
            disconnect();
            clientHandler.sendMessage(msg);
        }
    }

    private byte[] unZipByte(byte[] data) {
        Inflater decompresser = new Inflater();
        decompresser.setInput(data);
        byte result[] = new byte[0];
        ByteArrayOutputStream o = new ByteArrayOutputStream(1);
        try {
            byte[] buf = new byte[102400];
            int got;
            while (!decompresser.finished()) {
                got = decompresser.inflate(buf);
                o.write(buf, 0, got);
            }
            result = o.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            decompresser.end();
        }
        return result;
    }
}

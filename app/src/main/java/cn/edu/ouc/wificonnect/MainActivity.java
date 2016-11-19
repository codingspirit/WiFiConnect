package cn.edu.ouc.wificonnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Switch switchWiFi, switchConnect;
    Button buttonWiFiInfo, buttonSend;
    TextView textSSID, textLocalIP, textGeneral, textRemote;
    WifiManager wifi;
    WifiInfo wifiInfo;
    InetAddress HostIP;
    ImageButton imageButtonUp, imageButtonDown, imageButtonLeft, imageButtonRight, imageButtonFire, imageButtonManual;
    int HostPort;
    EditText editTextPort, editTextHostIP, editTextReceive, editTextSend;
    ViewPager viewPager;
    SocketClient socketClient;
    MyHandler handler = new MyHandler(this);
    List<View> myViews;
    LayoutInflater mylayoutInflater;
    MyPagerAdapter myPagerAdapter;
    View pageGeneral, pageRemote;
    boolean isManual = false;

    //region OnCheckedChangeListener
    final CompoundButton.OnCheckedChangeListener myCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isChecked()) {
                switch (buttonView.getId()) {
                    case R.id.switchWiFi:
                        wifi.setWifiEnabled(true);
                        break;
                    case R.id.switchConnect:// 连接事件
                        editTextHostIP.setEnabled(false);
                        editTextPort.setEnabled(false);
                        switchConnect.setEnabled(false);
                        String ip = editTextHostIP.getText().toString().trim();
                        String port = editTextPort.getText().toString().trim();
                        try {
                            HostIP = InetAddress.getByName(ip);
                            HostPort = Integer.valueOf(port);
                        } catch (Exception e) {
                            Log.e("Error", Log.getStackTraceString(e));
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_wrong_ip), Toast.LENGTH_SHORT).show();
                            editTextHostIP.setEnabled(true);
                            editTextPort.setEnabled(true);
                            switchConnect.setEnabled(true);
                            switchConnect.setChecked(false);
                        }
                        if (switchConnect.isChecked()) {
                            socketClient.connect(HostIP, HostPort);
                        }
                        break;
                }
            } else {
                switch (buttonView.getId()) {
                    case R.id.switchWiFi:
                        wifi.setWifiEnabled(false);
                        break;
                    case R.id.switchConnect:
                        if (socketClient.isConnected()) {
                            socketClient.disconnect();
                        }
                        editTextHostIP.setEnabled(true);
                        editTextPort.setEnabled(true);
                        switchConnect.setEnabled(true);
                        break;
                }
            }
        }
    };
    //endregion

    //region OnClickListener
    final View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonWiFiInfo:
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    break;
                case R.id.buttonSend:// 发送事件
                    byte[] buffer = null;
                    try {
                        buffer = editTextSend.getText().toString().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                    socketClient.send(buffer);
                    break;
                case R.id.textViewRemote:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.textViewGeneral:
                    viewPager.setCurrentItem(0);
                    break;
                //region imageButton
                case R.id.imageButtonDown:
                    try {
                        socketClient.send(getString(R.string.text_down).getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                    break;
                case R.id.imageButtonUp:
                    try {
                        socketClient.send(getString(R.string.text_up).getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                    break;
                case R.id.imageButtonLeft:
                    try {
                        socketClient.send(getString(R.string.text_left).getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                    break;
                case R.id.imageButtonRight:
                    try {
                        socketClient.send(getString(R.string.text_right).getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                    break;
                case R.id.imageButtonFire:
                    try {
                        socketClient.send(getString(R.string.text_fire).getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                    break;
                case R.id.imageButtonManual:
                    if (!isManual) {
                        try {
                            socketClient.send(getString(R.string.text_manual).getBytes("UTF-8"));
                            isManual = true;
                            imageButtonManual.setBackgroundColor(Color.parseColor("#6a0606"));
                        } catch (UnsupportedEncodingException e) {
                            Log.e("Error", Log.getStackTraceString(e));
                        }
                    } else {
                        try {
                            socketClient.send(getString(R.string.text_atuo).getBytes("UTF-8"));
                            isManual = false;
                            imageButtonManual.setBackgroundColor(Color.TRANSPARENT);
                        } catch (UnsupportedEncodingException e) {
                            Log.e("Error", Log.getStackTraceString(e));
                        }
                    }
                    break;
                //endregion
            }
        }
    };
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //region Viewpager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mylayoutInflater = getLayoutInflater();
        myViews = new ArrayList<>();
        pageGeneral = mylayoutInflater.inflate(R.layout.general, new RelativeLayout(this), false);
        pageRemote = mylayoutInflater.inflate(R.layout.remote, new RelativeLayout(this), false);
        myViews.add(pageGeneral);
        myViews.add(pageRemote);
        myPagerAdapter = new MyPagerAdapter(myViews);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setCurrentItem(0);
        //endregion
        //region findViewById
        textGeneral = (TextView) findViewById(R.id.textViewGeneral);
        textRemote = (TextView) findViewById(R.id.textViewRemote);
        switchWiFi = (Switch) pageGeneral.findViewById(R.id.switchWiFi);
        switchConnect = (Switch) pageGeneral.findViewById(R.id.switchConnect);
        buttonWiFiInfo = (Button) pageGeneral.findViewById(R.id.buttonWiFiInfo);
        buttonSend = (Button) pageGeneral.findViewById(R.id.buttonSend);
        textSSID = (TextView) pageGeneral.findViewById(R.id.textViewName);
        textLocalIP = (TextView) pageGeneral.findViewById(R.id.textViewLocalIP);
        editTextHostIP = (EditText) pageGeneral.findViewById(R.id.editTextHostIP);
        editTextPort = (EditText) pageGeneral.findViewById(R.id.editTextPort);
        editTextReceive = (EditText) pageGeneral.findViewById(R.id.editTextReceive);
        editTextSend = (EditText) pageGeneral.findViewById(R.id.editTextSend);
        imageButtonDown = (ImageButton) pageRemote.findViewById(R.id.imageButtonDown);
        imageButtonUp = (ImageButton) pageRemote.findViewById(R.id.imageButtonUp);
        imageButtonLeft = (ImageButton) pageRemote.findViewById(R.id.imageButtonLeft);
        imageButtonRight = (ImageButton) pageRemote.findViewById(R.id.imageButtonRight);
        imageButtonFire = (ImageButton) pageRemote.findViewById(R.id.imageButtonFire);
        imageButtonManual = (ImageButton) pageRemote.findViewById(R.id.imageButtonManual);
        //endregion
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new WiFiConnectChangedReceiver(this), filter);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            switchWiFi.setChecked(true);
        }
        //region bind Listener
        switchWiFi.setOnCheckedChangeListener(myCheckedChangeListener);
        switchConnect.setOnCheckedChangeListener(myCheckedChangeListener);
        buttonWiFiInfo.setOnClickListener(myClickListener);
        buttonSend.setOnClickListener(myClickListener);
        textRemote.setOnClickListener(myClickListener);
        textGeneral.setOnClickListener(myClickListener);
        imageButtonDown.setOnClickListener(myClickListener);
        imageButtonUp.setOnClickListener(myClickListener);
        imageButtonLeft.setOnClickListener(myClickListener);
        imageButtonRight.setOnClickListener(myClickListener);
        imageButtonFire.setOnClickListener(myClickListener);
        imageButtonManual.setOnClickListener(myClickListener);
        //endregion
        socketClient = new SocketClient(handler, getApplicationContext());
    }

    static class MyHandler extends Handler {
        WeakReference<MainActivity> mTarget;

        MyHandler(MainActivity activity) {
            mTarget = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mTarget.get();
            switch (msg.what) {
                case 0x01://连接信息
                    Toast.makeText(mainActivity.getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    if (!mainActivity.socketClient.isConnected()) {
                        mainActivity.editTextHostIP.setEnabled(true);
                        mainActivity.editTextPort.setEnabled(true);
                        mainActivity.switchConnect.setChecked(false);
                    }
                    mainActivity.switchConnect.setEnabled(true);
                    break;
                case 0x02://收到消息
                    mainActivity.editTextReceive.append(msg.obj.toString() + "\n");
                    break;
            }
        }
    }
}

class WiFiConnectChangedReceiver extends BroadcastReceiver {
    private MainActivity activity;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                case WifiManager.WIFI_STATE_ENABLED:
                    activity.switchWiFi.setChecked(true);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    activity.switchWiFi.setChecked(false);
                    break;
            }
        }
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                activity.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                activity.wifiInfo = activity.wifi.getConnectionInfo();
                activity.textSSID.setText(activity.wifiInfo.getSSID());
                int ip = activity.wifiInfo.getIpAddress();
                activity.textLocalIP.setText((activity.getString(R.string.text_localip) + ":" + (ip & 0xff) + "." + (ip >> 8 & 0xff) + "."
                        + (ip >> 16 & 0xff) + "." + (ip >> 24 & 0xff)));
                activity.editTextHostIP.setText((ip & 0xff) + "." + (ip >> 8 & 0xff) + "."
                        + (ip >> 16 & 0xff) + "." + ((ip >> 24 & 0xff) - 1));
                activity.editTextPort.setText(R.string.text_defaultport);
            } else {
                activity.textSSID.setText(activity.getString(R.string.text_unconnected));
                activity.textLocalIP.setText(activity.getString(R.string.text_localip));
            }
        }
    }

    public WiFiConnectChangedReceiver(MainActivity activity) {
        this.activity = activity;
    }
}

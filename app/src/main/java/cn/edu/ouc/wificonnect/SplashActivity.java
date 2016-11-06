package cn.edu.ouc.wificonnect;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SplashActivity extends AppCompatActivity {
private final int SplashTime=3200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        WebView wbGif=(WebView)findViewById(R.id.webViewGif);
        wbGif.setBackgroundColor(Color.parseColor("#6a0606"));
        wbGif.loadUrl("file:///android_res/drawable/loading.gif");
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main=new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(main);
                SplashActivity.this.finish();
            }
        },SplashTime);
    }
}
package com.example.internetconnectivitylistener;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.internetlib.NetworkInfo;

public class MainActivity extends AppCompatActivity implements NetworkInfo.NetworkInfoListener {
    NetworkInfo networkInfo;
    TextView tvConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }  tvConnectivity = findViewById(R.id.tvConnectivity);
        networkInfo = NetworkInfo.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkInfo.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkInfo.removeListener(this);
    }

    @Override
    public void networkStatusChange(NetworkInfo.Network network) {
        if (network.getStatus() == NetworkInfo.NetworkStatus.INTERNET) {
            showText(false);
            Toast.makeText(this, "ONLINE: ${" + network.getType() + "}", Toast.LENGTH_SHORT).show();
        } else {
            showText(true);
            Toast.makeText(this, "OFFLINE: ${" + network.getType() + "}", Toast.LENGTH_SHORT).show();
        }
    }

    void showText(boolean show) {
        if (tvConnectivity != null) {
            if (show) {
                tvConnectivity.setText("No Internet Connection");
                tvConnectivity.setVisibility(View.VISIBLE);
                tvConnectivity.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            } else {
                tvConnectivity.setText("You are connected");
                tvConnectivity.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvConnectivity.setVisibility(View.GONE);
                    }
                }, 1500);
            }
        }
    }
}
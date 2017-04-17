package com.uriolus.barometer.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.things.pio.PeripheralManagerService;
import com.uriolus.barometer.R;
import com.uriolus.barometer.utils.HardwareUtils;
import com.uriolus.barometer.utils.NetUtils;



public class MainActivity extends AppCompatActivity {
    private TextView textIp;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        HardwareUtils.getListOfPWMPorts(true);
        HardwareUtils.getListOfGPIOPorts(true);
        configureHardware();

    }

    private void configureHardware() {
        PeripheralManagerService pioService = new PeripheralManagerService();

    }

    @Override
    protected void onResume() {
        super.onResume();
        showIp();
    }

    private void showIp() {
        //String ip=NetUtils.getIp(this);
        String ip = NetUtils.getLocalIpAddress(true);
        textIp.setText(ip);
    }

    private void findViews() {
        textIp = (TextView) findViewById(R.id.text_ip);
    }






    @Override
    protected void onDestroy() {
        super.onDestroy();



    }
}

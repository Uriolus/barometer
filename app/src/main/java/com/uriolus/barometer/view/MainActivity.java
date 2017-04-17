package com.uriolus.barometer.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorManager.DynamicSensorCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;
import com.google.android.things.pio.PeripheralManagerService;
import com.uriolus.barometer.R;
import com.uriolus.barometer.utils.BoardDefaults;
import com.uriolus.barometer.utils.HardwareUtils;
import com.uriolus.barometer.utils.NetUtils;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textIp;
    private TextView tvTemperature;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Bmx280SensorDriver mTemperatureSensorDriver;
    private SensorManager mSensorManager;
    private DynamicSensorCallback mDynamicSensorCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        configureHardware();

    }

    private void configureHardware() {


        mDynamicSensorCallback = new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    Log.i(TAG, "Temperature sensor connected");
                    mSensorManager.registerListener(MainActivity.this,
                            sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        };
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);

        try {
            mTemperatureSensorDriver = new Bmx280SensorDriver(BoardDefaults.getI2CPort());
            mTemperatureSensorDriver.registerTemperatureSensor();
        } catch (IOException e) {
            Log.e(TAG, "Error configuring sensor", e);
        }
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
        tvTemperature = (TextView) findViewById(R.id.tv_temperature);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyHardware();


    }

    private void destroyHardware() {
        Log.i(TAG, "Closing sensor");
        if (mTemperatureSensorDriver != null) {
            mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);
            mSensorManager.unregisterListener(this);
            mTemperatureSensorDriver.unregisterTemperatureSensor();
            try {
                mTemperatureSensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensor", e);
            } finally {
                mTemperatureSensorDriver = null;
            }
        }
    }
    private void publishTemperature(Float temperature){
        tvTemperature.setText(temperature+"ºC");
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
       publishTemperature( sensorEvent.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "sensor accuracy changed: " + accuracy);
    }
}

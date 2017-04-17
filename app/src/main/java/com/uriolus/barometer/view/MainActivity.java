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
import com.uriolus.barometer.R;
import com.uriolus.barometer.utils.BoardDefaults;
import com.uriolus.barometer.utils.NetUtils;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textIp;
    private TextView tvTemperature, tvPressure;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Bmx280SensorDriver bmx280SensorDriver;
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
        try {
            bmx280SensorDriver = new Bmx280SensorDriver(BoardDefaults.getI2CPort());
            bmx280SensorDriver.registerTemperatureSensor();
            bmx280SensorDriver.registerPressureSensor();

        } catch (IOException e) {
            Log.e(TAG, "Error configuring sensor", e);
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerSensors();
    }

    private void registerSensors() {
        // Register the BMP280 temperature sensor
        Sensor temperature = mSensorManager
                .getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).get(0);
        mSensorManager.registerListener(this, temperature,
                SensorManager.SENSOR_DELAY_NORMAL);
        // Register the BMP280 pressure sensor
        Sensor pressure = mSensorManager
                .getDynamicSensorList(Sensor.TYPE_PRESSURE).get(0);
        mSensorManager.registerListener(this, pressure,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        showIp();
    }

    private void showIp() {

        String ip = NetUtils.getLocalIpAddress(true);
        textIp.setText(ip);
    }

    private void findViews() {
        textIp = (TextView) findViewById(R.id.text_ip);
        tvTemperature = (TextView) findViewById(R.id.tv_temperature);
        tvPressure = (TextView) findViewById(R.id.tv_preasure);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyHardware();


    }

    private void destroyHardware() {
        Log.i(TAG, "Closing sensor");
        if (bmx280SensorDriver != null) {

            mSensorManager.unregisterListener(this);
            bmx280SensorDriver.unregisterTemperatureSensor();
            bmx280SensorDriver.unregisterPressureSensor();
            try {
                bmx280SensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensor", e);
            } finally {
                bmx280SensorDriver = null;
            }
        }
    }

    private void publishTemperature(Float temperature) {
        tvTemperature.setText(temperature + "ºC");
    }

    private void publishPreasure(Float preasure) {
        tvPressure.setText(preasure + "?");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float value = sensorEvent.values[0];

        if (sensorEvent.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            publishTemperature(value);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            publishPreasure(value);
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "sensor accuracy changed: " + accuracy);
    }
}

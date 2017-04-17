package com.uriolus.barometer.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorManager.DynamicSensorCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;
import com.uriolus.barometer.R;
import com.uriolus.barometer.utils.BoardDefaults;
import com.uriolus.barometer.utils.NetUtils;
import com.uriolus.barometer.view.presenter.DisplayPresenter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textIp;
    private TextView tvTemperature, tvPressure, tvAltitude;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Bmx280SensorDriver bmx280SensorDriver;
    private SensorManager mSensorManager;
    private DisplayPresenter displayPresenter;
    private final int everySeconds=2*1000;
    private long last_time = (long)(System.nanoTime() / 1000000f);
    private float elapsed=0;
    DecimalFormat df = new DecimalFormat("0.0");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayPresenter=new DisplayPresenter(this);
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
        displayPresenter.create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerSensors();
    }

    private void registerSensors() {
        // Register the BMP280 temperature sensor
        mSensorManager.registerDynamicSensorCallback(new DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                super.onDynamicSensorConnected(sensor);
                Log.w(TAG,"New sensor connected:"+sensor.getName());
                registerSensor(sensor);
            }

            @Override
            public void onDynamicSensorDisconnected(Sensor sensor) {
                super.onDynamicSensorDisconnected(sensor);
            }
        });
        List<Sensor> sensorTemperatureList= mSensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensorTemperatureList==null || sensorTemperatureList.size()==0){
            Log.e(TAG,"No temperature sensors");
        }else {
            registerSensor(sensorTemperatureList.get(0));
        }
        List<Sensor> sensorPressureList= mSensorManager.getDynamicSensorList(Sensor.TYPE_PRESSURE);
        if (sensorPressureList==null || sensorPressureList.size()==0){
            Log.e(TAG,"No presure sensors");
        }else {
            // Register the BMP280 pressure sensor
            registerSensor(sensorPressureList.get(0));
        }
    }

    private void registerSensor(Sensor sensor) {
        mSensorManager.registerListener(this, sensor,
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
        tvAltitude = (TextView) findViewById(R.id.tv_altitude);
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
        displayPresenter.destroy();
    }

    private void publishTemperature(Float temperature) {
        tvTemperature.setText(df.format(temperature) + "ºC");
        if (calculateIfHasToDisplay()) {
            new AsyncTaskOLED().doInBackground(df.format(temperature) + "ºC");
        }
       //displayTextInOLED(df.format(temperature) + "ºC");
    }

    private void displayTextInOLED(String temperature) {
        displayPresenter.writeText(temperature);
    }
    private class AsyncTaskOLED extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... strings) {
            //displayTextInOLED(strings[0]) ;

            displayPresenter.drawTextInstantaneously(strings[0]);
            return null;
        }
    }
    private void publishPreasure(Float preasure) {
        tvPressure.setText(preasure + "?");
    }

    private void publishAltitude(float altitude) {
        tvAltitude.setText(altitude+"m");
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float value = sensorEvent.values[0];

        if (sensorEvent.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            publishTemperature(value);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            publishPreasure(value);
            //float altitude = readAltitude(1013.25f, value);
            float altitude = SensorManager.getAltitude(1013.25f, value);
            publishAltitude(altitude);
        }


    }
    private boolean calculateIfHasToDisplay(){
        long time = (long)(System.nanoTime() / 1000000f);
        long delta_time =  (time - last_time );
        last_time = time;
        elapsed+=delta_time;
        if (elapsed>everySeconds){
            elapsed=0;
            Log.d(TAG,"HAS TO UPDATE");
            return true;
        }
        return false;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "sensor accuracy changed: " + accuracy);
    }

}

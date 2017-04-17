package com.uriolus.barometer.utils;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;

import java.util.List;

/**
 * Created by Ofernandez on 12/2/2017.
 */

public class HardwareUtils {
    public static List<String> getListOfPWMPorts(Boolean withLog) {

        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getPwmList();
        if (portList.isEmpty())
        {
            if (withLog) {
                Log.i("LIST_PWM_PORTS", "No PWM port available on this device.");
            }
        } else

        {
            if (withLog) {
                Log.i("LIST_PWM_PORTS", "List of available ports: " + portList);
            }
        }
        return portList;
    }
    public static List<String> getListOfGPIOPorts(Boolean withLog) {

        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i("LIST_PORTS", "No GPIO port available on this device.");
        } else {
            Log.i("LIST_PORTS", "List of available ports: " + portList);
        }
        return portList;
    }
}

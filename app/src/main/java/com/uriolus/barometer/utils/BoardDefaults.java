package com.uriolus.barometer.utils;

import android.os.Build;
import com.google.android.things.pio.PeripheralManagerService;
import java.util.List;

/**
 * Created by oriolfernandez on 6/2/17.
 */

public class BoardDefaults {
  private static final String DEVICE_EDISON_ARDUINO = "edison_arduino";
  private static final String DEVICE_EDISON = "edison";
  private static final String DEVICE_JOULE = "joule";
  private static final String DEVICE_RPI3 = "rpi3";
  private static final String DEVICE_PICO = "imx6ul_pico";
  private static final String DEVICE_VVDN = "imx6ul_iopb";
  private static String sBoardVariant = "";

  /**
   * Return the GPIO pin that the LED is connected on.
   * For example, on Intel Edison Arduino breakout, pin "IO13" is connected to an onboard LED
   * that turns on when the GPIO pin is HIGH, and off when low.
   */
  public static String getGPIOForLED() {
    switch (getBoardVariant()) {
      case DEVICE_EDISON_ARDUINO:
        return "IO13";
      case DEVICE_EDISON:
        return "GP45";
      case DEVICE_RPI3:
        return "BCM6";

      default:
        throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
    }
  }
  /**
   * Return the preferred I2C port for each board.
   */
  public static String getI2CPort() {
    switch (getBoardVariant()) {
      case DEVICE_EDISON_ARDUINO:
        return "I2C6";
      case DEVICE_EDISON:
        return "I2C1";
      case DEVICE_JOULE:
        return "I2C0";
      case DEVICE_RPI3:
        return "I2C1";
      case DEVICE_PICO:
        return "I2C2";
      case DEVICE_VVDN:
        return "I2C4";
      default:
        throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
    }
  }
  /**
   * Return the GPIO pin that the Button is connected on.
   */
  public static String getGPIOForButton() {
    switch (getBoardVariant()) {
      case DEVICE_EDISON_ARDUINO:
        return "IO12";
      case DEVICE_EDISON:
        return "GP44";
      case DEVICE_RPI3:
        return "BCM21";

      default:
        throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
    }
  }

  private static String getBoardVariant() {
    if (!sBoardVariant.isEmpty()) {
      return sBoardVariant;
    }
    sBoardVariant = Build.DEVICE;
    // For the edison check the pin prefix
    // to always return Edison Breakout pin name when applicable.
    if (sBoardVariant.equals(DEVICE_EDISON)) {
      PeripheralManagerService pioService = new PeripheralManagerService();
      List<String> gpioList = pioService.getGpioList();
      if (gpioList.size() != 0) {
        String pin = gpioList.get(0);
        if (pin.startsWith("IO")) {
          sBoardVariant = DEVICE_EDISON_ARDUINO;
        }
      }
    }
    return sBoardVariant;
  }
}
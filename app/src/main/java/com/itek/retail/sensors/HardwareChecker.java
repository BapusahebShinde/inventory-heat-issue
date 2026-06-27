package com.itek.retail.sensors;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.itek.retail.common.SharedPrefManager;

/**
 * Class that tests availability of hardware sensors.
 *
 * @author Bapusaheb Shinde
 */

public class HardwareChecker{
  
  public HardwareChecker(final SensorManager sensorManager){
    if(sensorManager.getSensorList(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR).size() > 0){
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
    }
    else if(sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size() > 0){
      /*if(sensorManager.getSensorList(Sensor.TYPE_GRAVITY).size() > 0 || sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() > 0)
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_MAGNETIC_FIELD);*/
      if(sensorManager.getSensorList(Sensor.TYPE_GRAVITY).size() > 0){
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_MAGNETIC_FIELD * 100 + Sensor.TYPE_GRAVITY);
      }
      else if(sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() > 0){
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_MAGNETIC_FIELD * 100 + Sensor.TYPE_ACCELEROMETER);
      }
    }
    else if(sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).size() > 0)  SharedPrefManager.setSensorTypeValue(Sensor.TYPE_ROTATION_VECTOR);
    else if(sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR).size() > 0)  SharedPrefManager.setSensorTypeValue(Sensor.TYPE_GAME_ROTATION_VECTOR);
    else if(sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE).size() > 0) SharedPrefManager.setSensorTypeValue(Sensor.TYPE_GYROSCOPE);
    
    SharedPrefManager.setIsSensorAvailable(SharedPrefManager.getSensorTypeValue()>0);
  }
}


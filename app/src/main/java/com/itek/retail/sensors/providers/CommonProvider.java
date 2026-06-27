package com.itek.retail.sensors.providers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.reader.direction.OrientationProvider;

/**
 * The orientation provider that delivers the current orientation from the {@link Sensor#TYPE_ACCELEROMETER
 * Accelerometer} and {@link Sensor#TYPE_MAGNETIC_FIELD Compass}.
 *
 * @author Bapusaheb Shinde 17-11-2022
 */
public class CommonProvider extends OrientationProvider{
  
  /**
   * Inclination values
   */
  final float[] inclinationValues = new float[16];
  /**
   * Compass values
   */
  final private float[] magnitudeValues = new float[3];
  /**
   * Accelerometer values
   */
  final private float[] accelerometerValues = new float[3];
  final private float[] gravityValues = new float[3];
  
  final private float[] temporaryQuaternion = new float[4];
  
  /**
   * Initialises a new CommonProvider
   *
   * @param sensorManager The android sensor manager
   */
  public CommonProvider(SensorManager sensorManager){
    super(sensorManager);
    registerSensor();
  }
  
  private void registerSensor(){registerSensor(false);}
  
  /**
   * registers the sensor stored in the Shared Preferences
   *
   * @param isStart
   */
  private void registerSensor(final boolean isStart){
    sensorList.clear();
    try{
      Sensor sensor = sensorManager.getDefaultSensor(SharedPrefManager.getSensorTypeValue());
      if(sensor != null){
        sensorList.add(sensor);
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
          final Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
          if(gravitySensor != null) sensorList.add(gravitySensor);
          final Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
          if(accelerometerSensor != null) sensorList.add(accelerometerSensor);
        }
      }
      else setNextSensor();
    }
    catch(Exception e){}
    
  }
  
  @Override
  public void onSensorChanged(SensorEvent event){
    if(event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
      if(isEmptyArray(event.values)) setNextSensor(true);
      else callRotationVector(event.values);
    }
    if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || event.sensor.getType() == Sensor.TYPE_GRAVITY || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
      if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
        System.arraycopy(event.values, 0, magnitudeValues, 0, magnitudeValues.length);
      }
      else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
        System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
      }
      else if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
        System.arraycopy(event.values, 0, gravityValues, 0, accelerometerValues.length);
      }
      if(!isEmptyArray(magnitudeValues) && (!isEmptyArray(accelerometerValues) || !isEmptyArray(gravityValues))){
        // Fuse accelerometer with compass
        SensorManager.getRotationMatrix(currentOrientationRotationMatrix.matrix, null, !isEmptyArray(gravityValues) ? gravityValues : accelerometerValues, magnitudeValues);
        // Transform rotation matrix to quaternion
        currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix);
        
        //SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);
        //currentOrientationQuaternion.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);
        
        double currentAngle = (float) (2.0f * Math.acos(currentOrientationQuaternion.getW()) * 180.0f / Math.PI);
        if(currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle;
        
        sensorValues.postValue(currentAngle + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ());
      }
      else setNextSensor(true);
    }
    if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
      if(isEmptyArray(event.values)) setNextSensor(true);
      else callRotationVector(event.values);
    }
    if(event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
      if(isEmptyArray(event.values)) setNextSensor(true);
      else callRotationVector(event.values);
    }
    if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
      if(isEmptyArray(event.values)) setNextSensor(true);
    }
  }
  
  private void setNextSensor(){setNextSensor(false);}
  
  /**
   * Sets the Next Sensor if the Current Sensor from Shared Preferences is unavailable or returns null/empty values
   *
   * @param isProcessing
   */
  private void setNextSensor(boolean isProcessing){
    if(isProcessing) stop();
    switch(SharedPrefManager.getSensorTypeValue()){
      case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_MAGNETIC_FIELD);
        registerSensor(isProcessing);
      break;
      case Sensor.TYPE_MAGNETIC_FIELD:
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_ROTATION_VECTOR);
        registerSensor(isProcessing);
      break;
      case Sensor.TYPE_ROTATION_VECTOR:
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_GAME_ROTATION_VECTOR);
        registerSensor(isProcessing);
      break;
      case Sensor.TYPE_GAME_ROTATION_VECTOR:
        SharedPrefManager.setSensorTypeValue(Sensor.TYPE_GYROSCOPE);
        registerSensor(isProcessing);
      break;
      default:
        SharedPrefManager.setSensorTypeValue(0);
        break;
    }
  }
  
  /**
   * handles the event values for all Sensors with Rotation Vector
   *
   * @param eventValues
   */
  private void callRotationVector(float [] eventValues){
    // convert the rotation-vector to a 4x4 matrix. the matrix
    // is interpreted by Open GL as the inverse of the
    // rotation-vector, which is what we want.
    SensorManager.getRotationMatrixFromVector(currentOrientationRotationMatrix.matrix, eventValues);
  
    // Get Quaternion
    // Calculate angle. Starting with API_18, Android will provide this value as event.values[3], but if not, we have to calculate it manually.
    //SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);
    //currentOrientationQuaternion.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);
  
    currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix);
  
    double currentAngle = (float) (2.0f * Math.acos(currentOrientationQuaternion.getW()) * 180.0f / Math.PI);
    if(currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle;
    sensorValues.postValue(currentAngle + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ());
  }
}

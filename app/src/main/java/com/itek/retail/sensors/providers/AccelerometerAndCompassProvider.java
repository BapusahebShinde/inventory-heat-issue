package com.itek.retail.sensors.providers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.itek.retail.reader.direction.OrientationProvider;

/**
 * The orientation provider that delivers the current orientation from the {@link Sensor#TYPE_ACCELEROMETER
 * Accelerometer} and {@link Sensor#TYPE_MAGNETIC_FIELD Compass}.
 *
 * @author Bapusaheb Shinde 17-11-2022
 *
 */
public class AccelerometerAndCompassProvider extends OrientationProvider {

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
    
    
    final private float[] temporaryQuaternion = new float[4];
    /**
     * Initialises a new AccelerometerAndCompassProvider
     *
     * @param sensorManager The android sensor manager
     */
    public AccelerometerAndCompassProvider(SensorManager sensorManager) {
        super(sensorManager);

        //Add the compass and the accelerometer
        sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnitudeValues, 0, magnitudeValues.length);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
        }

        if (magnitudeValues != null && accelerometerValues != null) {
            // Fuse accelerometer with compass
            SensorManager.getRotationMatrix(currentOrientationRotationMatrix.matrix, null, accelerometerValues, magnitudeValues);
            // Transform rotation matrix to quaternion
            currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix);
    
            //SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);
            //currentOrientationQuaternion.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);
            
            double currentAngle = (float) (2.0f * Math.acos(currentOrientationQuaternion.getW()) * 180.0f / Math.PI);
            if(currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle;
            
            sensorValues.postValue(currentAngle + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ());
        }
    }
}

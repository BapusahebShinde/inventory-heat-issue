package com.itek.retail.sensors;

import androidx.lifecycle.MutableLiveData;

import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.direction.OrientationProvider;

public abstract class MainSensorRepository{
  
  /*<p>
  The major novelty in this application is the fusion of virtual
  sensors: <b>Improved Orientation Sensor 1</b> and <b>Improved
    Orientation Sensor 2</b> fuse the Android Rotation Vector with the
  virtual Gyroscope sensor to achieve a pose estimation with a
  previously unknown stability and precision.
</p>

<p>Apart from these two sensors, the following sensors are
  available for comparison:</p>
<ul>
  <li>Improved Orientation Sensor 1 (Sensor fusion of Android
    Rotation Vector and Calibrated Gyroscope - less stable but more
    accurate)</li>
  <li>Improved Orientation Sensor 2 (Sensor fusion of Android
    Rotation Vector and Calibrated Gyroscope - more stable but less
    accurate)</li>
  <li>Android Rotation Vector (Kalman filter fusion of
    Accelerometer + Gyroscope + Compass)</li>
  <li>Calibrated Gyroscope (Separate result of Kalman filter fusion
    of Accelerometer + Gyroscope + Compass)</li>
  <li>Gravity + Compass</li>
  <li>Accelerometer + Compass</li>
</ul>
*/
  protected CommonActivity context;
  protected OrientationProvider currentOrientationProvider;
  /**
   * Instantiates a new Main repository.
   *
   * @param context the context
   */
  public MainSensorRepository(CommonActivity context){
    this.context = context;
  }
  
  /**
   * Get sensor instance object.
   *
   * @return the object
   */
  protected abstract void getSensor();
  
  public void getSensorAndStart(){
  if(currentOrientationProvider==null) getSensor();
  if(currentOrientationProvider!=null) currentOrientationProvider.start();
  }
  
  public void stopSensor(){
   if(currentOrientationProvider!=null) currentOrientationProvider.stop();
  }
  
  public void onResume(){
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    getSensorAndStart();
  }
  
  public void onPause(){
   stopSensor();
  }
  
  public MutableLiveData<String> getSensorData(){
    return currentOrientationProvider != null ? currentOrientationProvider.sensorValues : new MutableLiveData<String>("0$0$0$0");
  }
}

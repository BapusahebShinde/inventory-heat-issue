/*
package com.itek.retail.sensors;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.direction.CubeRenderer;
import com.itek.retail.reader.direction.OrientationProvider;
import com.itek.retail.reader.direction.representation.Quaternion;
import com.itek.retail.sensors.providers.RotationAndGyroMoreStableProvider;

public class RotationAndGyroSensorMoreStableRepository extends MainSensorRepository{
  
  */
/**
   * The class that renders the cube
   *//*

  public CubeRenderer mRenderer;
  */
/**
   * The surface that will be drawn upon
   *//*

  private GLSurfaceView mGLSurfaceView;
  private Quaternion quaternion = new Quaternion();
  */
/**
   * The current orientation provider that delivers device orientation.
   *//*

  private OrientationProvider currentOrientationProvider;
  */
/**
   * Instantiates a new RotationAndGyroSensorMoreStableRepository repository.
   *
   * @param context the context
   *//*

  private Context context;
  
  public RotationAndGyroSensorMoreStableRepository(CommonActivity context){
    super(context);
    this.context = context;
    Log.e("SENSOR", "IN");
    
    // rfidHandler = new SpeedDataRFIDHandler();
    //barcodeHandler = new SpeedDataBarcodeHandler(context, RotationAndGyroSensorLessStableRepository.this, AppCommonMethods.SessionType.OTHER, true);
  }
  
  @Override
  public void getSensorAndStart(){
    //setProgressMessage(false);
    // Check if device has a hardware gyroscope
    
    currentOrientationProvider = new RotationAndGyroMoreStableProvider((SensorManager) context.getSystemService(SENSOR_SERVICE));
    
    if(currentOrientationProvider != null){
      Log.e("SENSOR", "OBJECT CREATED");
      
      currentOrientationProvider.start();
      
      mRenderer = new CubeRenderer();
      mRenderer.setOrientationProvider(currentOrientationProvider);
      mGLSurfaceView = new GLSurfaceView(context);
      mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
      mGLSurfaceView.setRenderer(mRenderer);
      mGLSurfaceView.onResume();
      
      if(currentOrientationProvider != null){
        // All Orientation providers deliver Quaternion as well as rotation matrix.
        // Use your favourite representation:
        
        // Get the rotation from the current orientationProvider as rotation matrix
        //gl.glMultMatrixf(orientationProvider.getRotationMatrix().getMatrix(), 0);
        
        // Get the rotation from the current orientationProvider as quaternion
        currentOrientationProvider.getQuaternion(quaternion);
        
      }
      
    }
  }
  
  @Override
  public MutableLiveData<String> getSensorData(){
    //setProgressMessage(false);
    return currentOrientationProvider != null ? currentOrientationProvider.sensorValues : new MutableLiveData<>("0$0$0$0");
  }
  
  @Override
  public void stopSensor(){
    //setProgressMessage(false);
    if(currentOrientationProvider != null){
      currentOrientationProvider.stop();
    }
  }
  
}
*/

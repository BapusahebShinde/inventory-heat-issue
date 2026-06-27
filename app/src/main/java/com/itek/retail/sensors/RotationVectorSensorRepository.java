package com.itek.retail.sensors;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.SensorManager;

import com.itek.retail.common.CommonActivity;
import com.itek.retail.sensors.providers.RotationVectorProvider;

public class RotationVectorSensorRepository extends MainSensorRepository{
  
  /**
   * Instantiates a new Main repository.
   *
   * @param context the context
   */
  public RotationVectorSensorRepository(CommonActivity context){
    super(context);
  }
  
  @Override
  protected void getSensor(){
    currentOrientationProvider = new RotationVectorProvider((SensorManager) context.getSystemService(SENSOR_SERVICE));
  }
}

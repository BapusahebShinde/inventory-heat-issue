package com.itek.retail.sensors.providers;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.SensorManager;

import com.itek.retail.common.CommonActivity;
import com.itek.retail.sensors.MainSensorRepository;

public class CommonSensorRepository extends MainSensorRepository{
  
  /**
   * Instantiates a new Main repository.
   *
   * @param context the context
   */
  public CommonSensorRepository(CommonActivity context){
    super(context);
  }
  
  @Override
  protected void getSensor(){
    currentOrientationProvider = new CommonProvider((SensorManager) context.getSystemService(SENSOR_SERVICE));
  }
}

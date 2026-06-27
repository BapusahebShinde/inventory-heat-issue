package com.itek.retail.sensors;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.SensorManager;

import com.itek.retail.common.CommonActivity;
import com.itek.retail.sensors.providers.GameRotationVectorProvider;

public class GameRotationSensorRepository extends MainSensorRepository{
  
  /**
   * Instantiates a new Main repository.
   *
   * @param context the context
   */
  public GameRotationSensorRepository(CommonActivity context){
    super(context);
  }
  
  @Override
  protected void getSensor(){
    currentOrientationProvider = new GameRotationVectorProvider((SensorManager) context.getSystemService(SENSOR_SERVICE));
  }
}

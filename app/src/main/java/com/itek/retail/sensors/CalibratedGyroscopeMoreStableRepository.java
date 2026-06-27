package com.itek.retail.sensors;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.SensorManager;

import com.itek.retail.common.CommonActivity;
import com.itek.retail.sensors.providers.CalibratedGyroscopeProvider;

public class CalibratedGyroscopeMoreStableRepository extends MainSensorRepository{
  
  /**
   * Instantiates a new RotationAndGyroSensorMoreStableRepository repository.
   */
  public CalibratedGyroscopeMoreStableRepository(CommonActivity context){
    super(context);
    this.context = context;
  }
  
  @Override
  protected void getSensor(){
    this.currentOrientationProvider = new CalibratedGyroscopeProvider((SensorManager) context.getSystemService(SENSOR_SERVICE));
  }
  
}

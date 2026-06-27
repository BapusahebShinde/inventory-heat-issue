package com.itek.retail.ui.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.itek.retail.R;
import com.itek.retail.common.CommonActivity;

/**
 * The Gauge view.
 */
public class GaugeView extends View{
  
  private static final String GUAGECOLOR = "#962A27";
  private float internalArcStrokeWidth;
  private int colorFirstItem = Color.parseColor(GUAGECOLOR);
  private int colorSecondItem = Color.parseColor(GUAGECOLOR);
  private int colorThirdItem = Color.parseColor(GUAGECOLOR);
  private int colorMainCenterCircle = Color.WHITE;
  private int colorCenterCircle = Color.parseColor(GUAGECOLOR);
  private int colorPointerLine = Color.parseColor(GUAGECOLOR);
  
  private float paddingMain;
  private float paddingInnerCircle;
  
  private float rotateDegree = 0; // for pointer line
  
  private float sweepAngleFirstChart = 0;
  private float sweepAngleSecondChart = 0;
  private float sweepAngleThirdChart = 0;
  private float strokePointerLineWidth = 1.0f;
  
  private float xAxis;
  private float yAxis;
  private float constantMeasure;
  private boolean isWidthBiggerThanHeight;
  
  private double internalArcStrokeWidthScale = 0.0001;
  private double paddingInnerCircleScale = 0.0001;
  private double pointerLineStrokeWidthScale = 0.0001;
  private float mainCircleScale = 1;
  
  /**
   * Instantiates a new Gauge view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public GaugeView(Context context, AttributeSet attrs){
    super(context, attrs);
    setupGaugeColor(context);
  }
  
  private void setupGaugeColor(Context context){
    TypedValue tv = new TypedValue();
    final int colorPrimaryDark = context instanceof CommonActivity ? ((CommonActivity) context).getColorPrimaryDarkFromTheme() : context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, tv, true) ? tv.data : ContextCompat.getColor(context, R.color.colorPrimaryDarkDef);
    colorFirstItem = colorPrimaryDark;//Color.parseColor(GUAGECOLOR);
    colorSecondItem = colorPrimaryDark;//Color.parseColor(GUAGECOLOR);
    colorThirdItem = colorPrimaryDark;//Color.parseColor(GUAGECOLOR);
    //colorMainCenterCircle = Color.WHITE;
    colorCenterCircle = colorPrimaryDark;//Color.parseColor(GUAGECOLOR);
    colorPointerLine = colorPrimaryDark;//Color.parseColor(GUAGECOLOR);
  }
  
  @Override
  protected void onDraw(Canvas canvas){
    super.onDraw(canvas);
    
    xAxis = getWidth();
    yAxis = getHeight();
    
    if(xAxis >= yAxis){
      constantMeasure = yAxis;
      isWidthBiggerThanHeight = true;
    }
    else{
      constantMeasure = xAxis;
      isWidthBiggerThanHeight = false;
    }
    
    internalArcStrokeWidth = (float) (constantMeasure * internalArcStrokeWidthScale);
    
    paddingInnerCircle = (float) (constantMeasure * paddingInnerCircleScale);
    
    strokePointerLineWidth = (float) (constantMeasure * pointerLineStrokeWidthScale);
    int mainCircleStroke = (int) (mainCircleScale * constantMeasure / 60);
    
    // middle arcs START
    Paint paintInnerArc1 = new Paint();
    paintInnerArc1.setStyle(Paint.Style.STROKE);
    paintInnerArc1.setStrokeWidth(internalArcStrokeWidth);
    paintInnerArc1.setColor(colorFirstItem);
    paintInnerArc1.setAntiAlias(true);
    
    Paint paintInnerArc2 = new Paint();
    paintInnerArc2.setStyle(Paint.Style.STROKE);
    paintInnerArc2.setStrokeWidth(internalArcStrokeWidth);
    paintInnerArc2.setColor(colorSecondItem);
    paintInnerArc2.setAntiAlias(true);
    
    Paint paintInnerArc3 = new Paint();
    paintInnerArc3.setStyle(Paint.Style.STROKE);
    paintInnerArc3.setStrokeWidth(internalArcStrokeWidth);
    paintInnerArc3.setColor(colorThirdItem);
    paintInnerArc3.setAntiAlias(true);
    
    RectF rectfInner;
    if(isWidthBiggerThanHeight){
      rectfInner = new RectF((xAxis - constantMeasure) / 2 + paddingInnerCircle, paddingInnerCircle, (xAxis - constantMeasure) / 2 + paddingInnerCircle + constantMeasure - 2 * paddingInnerCircle, constantMeasure - paddingInnerCircle);
    }
    else{
      rectfInner = new RectF(paddingInnerCircle, (yAxis - constantMeasure) / 2 + paddingInnerCircle, constantMeasure - paddingInnerCircle, (yAxis - constantMeasure) / 2 + constantMeasure - paddingInnerCircle);
    }
    
    canvas.drawArc(rectfInner, 135, sweepAngleFirstChart, false, paintInnerArc1); // 135
    canvas.drawArc(rectfInner, 225, sweepAngleSecondChart, false, paintInnerArc2);
    canvas.drawArc(rectfInner, 315, sweepAngleThirdChart, false, paintInnerArc3);
    
    // middle arcs END
    
    // pointer line START
    Paint p = new Paint();
    p.setAntiAlias(true);
    p.setColor(colorPointerLine);
    p.setStrokeWidth(strokePointerLineWidth);
    canvas.rotate(rotateDegree, xAxis / 2, yAxis / 2);
    
    int a = 8;
    if(isWidthBiggerThanHeight){
      float stopX = (xAxis - constantMeasure) / 2 + paddingInnerCircle + constantMeasure - 2 * paddingInnerCircle + mainCircleStroke;
      float stopY = yAxis / 2;
      Path path = new Path();
      path.setFillType(Path.FillType.EVEN_ODD);
      path.moveTo(xAxis / 2 + mainCircleStroke / a, yAxis / 2 - mainCircleStroke);
      path.lineTo(xAxis / 2 + mainCircleStroke / a, yAxis / 2 + mainCircleStroke);
      path.lineTo(stopX, stopY);
      path.close();
      canvas.drawPath(path, p);
    }
    else{
      float stopX = constantMeasure - paddingInnerCircle + mainCircleStroke;
      float stopY = yAxis / 2;
      
      Path path = new Path();
      path.setFillType(Path.FillType.EVEN_ODD);
      path.moveTo(xAxis / 2 + mainCircleStroke / a, yAxis / 2 - mainCircleStroke);
      path.lineTo(xAxis / 2 + mainCircleStroke / a, yAxis / 2 + mainCircleStroke);
      path.lineTo(stopX, stopY);
      path.close();
      canvas.drawPath(path, p);
    }
    
    // center circles START
    Paint paintInnerCircle = new Paint();
    paintInnerCircle.setStyle(Paint.Style.FILL);
    paintInnerCircle.setColor(colorCenterCircle);
    paintInnerCircle.setAntiAlias(true);
    canvas.drawCircle(xAxis / 2, yAxis / 2, mainCircleStroke, paintInnerCircle);
    
    Paint paintCenterCircle = new Paint();
    paintCenterCircle.setStyle(Paint.Style.FILL);
    paintCenterCircle.setColor(colorMainCenterCircle);
    canvas.drawCircle(xAxis / 2, yAxis / 2, mainCircleStroke / 2, paintCenterCircle);
    // center circles END
    
  }
  
  /**
   * Get internal arc stroke width float.
   *
   * @return the float
   */
  public float getInternalArcStrokeWidth(){
    return internalArcStrokeWidth;
  }
  
  /**
   * Set internal arc stroke width.
   *
   * @param internalArcStrokeWidth the internal arc stroke width
   */
  public void setInternalArcStrokeWidth(float internalArcStrokeWidth){
    this.internalArcStrokeWidth = internalArcStrokeWidth;
    invalidate();
  }
  
  /**
   * Get color first item int.
   *
   * @return the int
   */
  public int getColorFirstItem(){
    return colorFirstItem;
  }
  
  /**
   * Set color first item.
   *
   * @param colorFirstItem the color first item
   */
  public void setColorFirstItem(int colorFirstItem){
    this.colorFirstItem = colorFirstItem;
    invalidate();
  }
  
  /**
   * Set needle color
   *
   * @param colorId the color first item
   */
  public void setNeedleColor(int colorId){
    this.colorFirstItem = colorId;
    this.colorSecondItem = colorId;
    this.colorThirdItem = colorId;
    this.colorCenterCircle = colorId;
    this.colorPointerLine = colorId;
    invalidate();
  }
  
  /**
   * Get color second item int.
   *
   * @return the int
   */
  public int getColorSecondItem(){
    return colorSecondItem;
  }
  
  /**
   * Set color second item.
   *
   * @param colorSecondItem the color second item
   */
  public void setColorSecondItem(int colorSecondItem){
    this.colorSecondItem = colorSecondItem;
    invalidate();
  }
  
  /**
   * Get color third item int.
   *
   * @return the int
   */
  public int getColorThirdItem(){
    return colorThirdItem;
  }
  
  /**
   * Set color third item.
   *
   * @param colorThirdItem the color third item
   */
  public void setColorThirdItem(int colorThirdItem){
    this.colorThirdItem = colorThirdItem;
    invalidate();
  }
  
  /**
   * Get color center circle int.
   *
   * @return the int
   */
  public int getColorCenterCircle(){
    return colorCenterCircle;
  }
  
  /**
   * Set color center circle.
   *
   * @param colorCenterCircle the color center circle
   */
  public void setColorCenterCircle(int colorCenterCircle){
    this.colorCenterCircle = colorCenterCircle;
    invalidate();
  }
  
  /**
   * Get color main center circle int.
   *
   * @return the int
   */
  public int getColorMainCenterCircle(){
    return colorMainCenterCircle;
  }
  
  /**
   * Set color main center circle.
   *
   * @param colorMainCenterCircle the color main center circle
   */
  public void setColorMainCenterCircle(int colorMainCenterCircle){
    this.colorMainCenterCircle = colorMainCenterCircle;
    invalidate();
  }
  
  /**
   * Get color pointer line int.
   *
   * @return the int
   */
  public int getColorPointerLine(){
    return colorPointerLine;
  }
  
  /**
   * Set color pointer line.
   *
   * @param colorPointerLine the color pointer line
   */
  public void setColorPointerLine(int colorPointerLine){
    this.colorPointerLine = colorPointerLine;
    invalidate();
  }
  
  /**
   * Get padding main float.
   *
   * @return the float
   */
  public float getPaddingMain(){
    return paddingMain;
  }
  
  /**
   * Set padding main.
   *
   * @param paddingMain the padding main
   */
  public void setPaddingMain(float paddingMain){
    this.paddingMain = paddingMain;
    invalidate();
  }
  
  /**
   * Get padding inner circle float.
   *
   * @return the float
   */
  public float getPaddingInnerCircle(){
    return paddingInnerCircle;
  }
  
  /**
   * Set padding inner circle.
   *
   * @param paddingInnerCircle the padding inner circle
   */
  public void setPaddingInnerCircle(float paddingInnerCircle){
    this.paddingInnerCircle = paddingInnerCircle;
    invalidate();
  }
  
  /**
   * Get rotate degree float.
   *
   * @return the float
   */
  public float getRotateDegree(){
    return rotateDegree;
  }
  
  /**
   * Set rotate degree.
   *
   * @param rotateDegree the rotate degree
   */
  public void setRotateDegree(float rotateDegree){
    this.rotateDegree = rotateDegree;
    invalidate();
  }
  
  /**
   * Get sweep angle first chart float.
   *
   * @return the float
   */
  public float getSweepAngleFirstChart(){
    return sweepAngleFirstChart;
  }
  
  /**
   * Set sweep angle first chart.
   *
   * @param sweepAngleFirstChart the sweep angle first chart
   */
  public void setSweepAngleFirstChart(float sweepAngleFirstChart){
    this.sweepAngleFirstChart = sweepAngleFirstChart;
    invalidate();
  }
  
  /**
   * Get sweep angle second chart float.
   *
   * @return the float
   */
  public float getSweepAngleSecondChart(){
    return sweepAngleSecondChart;
  }
  
  /**
   * Set sweep angle second chart.
   *
   * @param sweepAngleSecondChart the sweep angle second chart
   */
  public void setSweepAngleSecondChart(float sweepAngleSecondChart){
    this.sweepAngleSecondChart = sweepAngleSecondChart;
    invalidate();
  }
  
  /**
   * Get sweep angle third chart float.
   *
   * @return the float
   */
  public float getSweepAngleThirdChart(){
    return sweepAngleThirdChart;
  }
  
  /**
   * Set sweep angle third chart.
   *
   * @param sweepAngleThirdChart the sweep angle third chart
   */
  public void setSweepAngleThirdChart(float sweepAngleThirdChart){
    this.sweepAngleThirdChart = sweepAngleThirdChart;
    invalidate();
  }
  
  /**
   * Get stroke pointer line width float.
   *
   * @return the float
   */
  public float getStrokePointerLineWidth(){
    return strokePointerLineWidth;
  }
  
  /**
   * Set stroke pointer line width.
   *
   * @param strokePointerLineWidth the stroke pointer line width
   */
  public void setStrokePointerLineWidth(float strokePointerLineWidth){
    this.strokePointerLineWidth = strokePointerLineWidth;
    invalidate();
  }
  
  @Override
  public float getX(){
    return xAxis;
  }
  
  @Override
  public void setX(float x){
    this.xAxis = x;
    invalidate();
  }
  
  @Override
  public float getY(){
    return yAxis;
  }
  
  @Override
  public void setY(float y){
    this.yAxis = y;
    invalidate();
  }
  
  /**
   * Get constant measure float.
   *
   * @return the float
   */
  public float getConstantMeasure(){
    return constantMeasure;
  }
  
  /**
   * Set constant measure.
   *
   * @param constantMeasure the constant measure
   */
  public void setConstantMeasure(float constantMeasure){
    this.constantMeasure = constantMeasure;
    invalidate();
  }
  
  /**
   * Is width bigger than height boolean.
   *
   * @return the boolean
   */
  public boolean isWidthBiggerThanHeight(){
    return isWidthBiggerThanHeight;
  }
  
  /**
   * Set width bigger than height.
   *
   * @param isWidthBiggerThanHeight the is width bigger than height
   */
  public void setWidthBiggerThanHeight(boolean isWidthBiggerThanHeight){
    this.isWidthBiggerThanHeight = isWidthBiggerThanHeight;
    invalidate();
  }
  
  /**
   * Get internal arc stroke width scale double.
   *
   * @return the double
   */
  public double getInternalArcStrokeWidthScale(){
    return internalArcStrokeWidthScale;
  }
  
  /**
   * Set internal arc stroke width scale.
   *
   * @param internalArcStrokeWidthScale the internal arc stroke width scale
   */
  public void setInternalArcStrokeWidthScale(double internalArcStrokeWidthScale){
    this.internalArcStrokeWidthScale = internalArcStrokeWidthScale;
    invalidate();
  }
  
  /**
   * Get padding inner circle scale double.
   *
   * @return the double
   */
  public double getPaddingInnerCircleScale(){
    return paddingInnerCircleScale;
  }
  
  /**
   * Set padding inner circle scale.
   *
   * @param paddingInnerCircleScale the padding inner circle scale
   */
  public void setPaddingInnerCircleScale(double paddingInnerCircleScale){
    this.paddingInnerCircleScale = paddingInnerCircleScale;
    invalidate();
  }
  
  /**
   * Get pointer line stroke width scale double.
   *
   * @return the double
   */
  public double getPointerLineStrokeWidthScale(){
    return pointerLineStrokeWidthScale;
  }
  
  /**
   * Set pointer line stroke width scale.
   *
   * @param pointerLineStrokeWidthScale the pointer line stroke width scale
   */
  public void setPointerLineStrokeWidthScale(double pointerLineStrokeWidthScale){
    this.pointerLineStrokeWidthScale = pointerLineStrokeWidthScale;
    invalidate();
  }
}
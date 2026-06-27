package com.itek.retail.ui.customviews.customseekbar;

import static com.itek.retail.ui.customviews.customseekbar.BubbleUtils.dp2px;
import static com.itek.retail.ui.customviews.customseekbar.BubbleUtils.sp2px;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;

/**
 * The Bubble config builder.
 */
public class BubbleConfigBuilder{
  
  float min;
  float max;
  float progress;
  boolean floatType;
  int trackSize;
  int secondTrackSize;
  int thumbRadius;
  int thumbRadiusOnDragging;
  int trackColor;
  int secondTrackColor;
  int thumbColor;
  int sectionCount;
  boolean showSectionMark;
  boolean autoAdjustSectionMark;
  boolean showSectionText;
  int sectionTextSize;
  int sectionTextColor;
  @BubbleSeekBar.TextPosition
  int sectionTextPosition;
  int sectionTextInterval;
  boolean showThumbText;
  int thumbTextSize;
  int thumbTextColor;
  boolean showProgressInFloat;
  long animDuration;
  boolean touchToSeek;
  boolean seekStepSection;
  boolean seekBySection;
  int bubbleColor;
  int bubbleTextSize;
  int bubbleTextColor;
  boolean alwaysShowBubble;
  long alwaysShowBubbleDelay;
  boolean hideBubble;
  boolean rtl;
  
  private BubbleSeekBar mBubbleSeekBar;
  
  /**
   * Instantiates a new Bubble config builder.
   *
   * @param bubbleSeekBar the bubble seek bar
   */
  BubbleConfigBuilder(BubbleSeekBar bubbleSeekBar){
    mBubbleSeekBar = bubbleSeekBar;
  }
  
  /**
   * Build.
   */
  public void build(){
    mBubbleSeekBar.config(this);
  }
  
  /**
   * Min bubble config builder.
   *
   * @param min the min
   * @return the bubble config builder
   */
  public BubbleConfigBuilder min(float min){
    this.min = min;
    this.progress = min;
    return this;
  }
  
  /**
   * Max bubble config builder.
   *
   * @param max the max
   * @return the bubble config builder
   */
  public BubbleConfigBuilder max(float max){
    this.max = max;
    return this;
  }
  
  /**
   * Progress bubble config builder.
   *
   * @param progress the progress
   * @return the bubble config builder
   */
  public BubbleConfigBuilder progress(float progress){
    this.progress = progress;
    return this;
  }
  
  /**
   * Float type bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder floatType(){
    this.floatType = true;
    return this;
  }
  
  /**
   * Track size bubble config builder.
   *
   * @param dp the dp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder trackSize(int dp){
    this.trackSize = dp2px(dp);
    return this;
  }
  
  /**
   * Second track size bubble config builder.
   *
   * @param dp the dp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder secondTrackSize(int dp){
    this.secondTrackSize = dp2px(dp);
    return this;
  }
  
  /**
   * Thumb radius bubble config builder.
   *
   * @param dp the dp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder thumbRadius(int dp){
    this.thumbRadius = dp2px(dp);
    return this;
  }
  
  /**
   * Thumb radius on dragging bubble config builder.
   *
   * @param dp the dp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder thumbRadiusOnDragging(int dp){
    this.thumbRadiusOnDragging = dp2px(dp);
    return this;
  }
  
  /**
   * Track color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder trackColor(@ColorInt int color){
    this.trackColor = color;
    this.sectionTextColor = color;
    return this;
  }
  
  /**
   * Second track color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder secondTrackColor(@ColorInt int color){
    this.secondTrackColor = color;
    this.thumbColor = color;
    this.thumbTextColor = color;
    this.bubbleColor = color;
    return this;
  }
  
  /**
   * Thumb color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder thumbColor(@ColorInt int color){
    this.thumbColor = color;
    return this;
  }
  
  /**
   * Section count bubble config builder.
   *
   * @param count the count
   * @return the bubble config builder
   */
  public BubbleConfigBuilder sectionCount(@IntRange(from = 1) int count){
    this.sectionCount = count;
    return this;
  }
  
  /**
   * Show section mark bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder showSectionMark(){
    this.showSectionMark = true;
    return this;
  }
  
  /**
   * Auto adjust section mark bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder autoAdjustSectionMark(){
    this.autoAdjustSectionMark = true;
    return this;
  }
  
  /**
   * Show section text bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder showSectionText(){
    this.showSectionText = true;
    return this;
  }
  
  /**
   * Section text size bubble config builder.
   *
   * @param sp the sp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder sectionTextSize(int sp){
    this.sectionTextSize = sp2px(sp);
    return this;
  }
  
  /**
   * Section text color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder sectionTextColor(@ColorInt int color){
    this.sectionTextColor = color;
    return this;
  }
  
  /**
   * Section text position bubble config builder.
   *
   * @param position the position
   * @return the bubble config builder
   */
  public BubbleConfigBuilder sectionTextPosition(@BubbleSeekBar.TextPosition int position){
    this.sectionTextPosition = position;
    return this;
  }
  
  /**
   * Section text interval bubble config builder.
   *
   * @param interval the interval
   * @return the bubble config builder
   */
  public BubbleConfigBuilder sectionTextInterval(@IntRange(from = 1) int interval){
    this.sectionTextInterval = interval;
    return this;
  }
  
  /**
   * Show thumb text bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder showThumbText(){
    this.showThumbText = true;
    return this;
  }
  
  /**
   * Thumb text size bubble config builder.
   *
   * @param sp the sp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder thumbTextSize(int sp){
    this.thumbTextSize = sp2px(sp);
    return this;
  }
  
  /**
   * Thumb text color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder thumbTextColor(@ColorInt int color){
    thumbTextColor = color;
    return this;
  }
  
  /**
   * Show progress in float bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder showProgressInFloat(){
    this.showProgressInFloat = true;
    return this;
  }
  
  /**
   * Anim duration bubble config builder.
   *
   * @param duration the duration
   * @return the bubble config builder
   */
  public BubbleConfigBuilder animDuration(long duration){
    animDuration = duration;
    return this;
  }
  
  /**
   * Touch to seek bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder touchToSeek(){
    this.touchToSeek = true;
    return this;
  }
  
  /**
   * Seek step section bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder seekStepSection(){
    this.seekStepSection = true;
    return this;
  }
  
  /**
   * Seek by section bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder seekBySection(){
    this.seekBySection = true;
    return this;
  }
  
  /**
   * Bubble color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder bubbleColor(@ColorInt int color){
    this.bubbleColor = color;
    return this;
  }
  
  /**
   * Bubble text size bubble config builder.
   *
   * @param sp the sp
   * @return the bubble config builder
   */
  public BubbleConfigBuilder bubbleTextSize(int sp){
    this.bubbleTextSize = sp2px(sp);
    return this;
  }
  
  /**
   * Bubble text color bubble config builder.
   *
   * @param color the color
   * @return the bubble config builder
   */
  public BubbleConfigBuilder bubbleTextColor(@ColorInt int color){
    this.bubbleTextColor = color;
    return this;
  }
  
  /**
   * Always show bubble bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder alwaysShowBubble(){
    this.alwaysShowBubble = true;
    return this;
  }
  
  /**
   * Always show bubble delay bubble config builder.
   *
   * @param delay the delay
   * @return the bubble config builder
   */
  public BubbleConfigBuilder alwaysShowBubbleDelay(long delay){
    alwaysShowBubbleDelay = delay;
    return this;
  }
  
  /**
   * Hide bubble bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder hideBubble(){
    this.hideBubble = true;
    return this;
  }
  
  /**
   * Rtl bubble config builder.
   *
   * @param rtl the rtl
   * @return the bubble config builder
   */
  public BubbleConfigBuilder rtl(boolean rtl){
    this.rtl = rtl;
    return this;
  }
  
  /**
   * Get min float.
   *
   * @return the float
   */
  public float getMin(){
    return min;
  }
  
  /**
   * Get max float.
   *
   * @return the float
   */
  public float getMax(){
    return max;
  }
  
  /**
   * Get progress float.
   *
   * @return the float
   */
  public float getProgress(){
    return progress;
  }
  
  /**
   * Is float type boolean.
   *
   * @return the boolean
   */
  public boolean isFloatType(){
    return floatType;
  }
  
  /**
   * Get track size int.
   *
   * @return the int
   */
  public int getTrackSize(){
    return trackSize;
  }
  
  /**
   * Get second track size int.
   *
   * @return the int
   */
  public int getSecondTrackSize(){
    return secondTrackSize;
  }
  
  /**
   * Get thumb radius int.
   *
   * @return the int
   */
  public int getThumbRadius(){
    return thumbRadius;
  }
  
  /**
   * Get thumb radius on dragging int.
   *
   * @return the int
   */
  public int getThumbRadiusOnDragging(){
    return thumbRadiusOnDragging;
  }
  
  /**
   * Get track color int.
   *
   * @return the int
   */
  public int getTrackColor(){
    return trackColor;
  }
  
  /**
   * Get second track color int.
   *
   * @return the int
   */
  public int getSecondTrackColor(){
    return secondTrackColor;
  }
  
  /**
   * Get thumb color int.
   *
   * @return the int
   */
  public int getThumbColor(){
    return thumbColor;
  }
  
  /**
   * Get section count int.
   *
   * @return the int
   */
  public int getSectionCount(){
    return sectionCount;
  }
  
  /**
   * Is show section mark boolean.
   *
   * @return the boolean
   */
  public boolean isShowSectionMark(){
    return showSectionMark;
  }
  
  /**
   * Is auto adjust section mark boolean.
   *
   * @return the boolean
   */
  public boolean isAutoAdjustSectionMark(){
    return autoAdjustSectionMark;
  }
  
  /**
   * Is show section text boolean.
   *
   * @return the boolean
   */
  public boolean isShowSectionText(){
    return showSectionText;
  }
  
  /**
   * Get section text size int.
   *
   * @return the int
   */
  public int getSectionTextSize(){
    return sectionTextSize;
  }
  
  /**
   * Get section text color int.
   *
   * @return the int
   */
  public int getSectionTextColor(){
    return sectionTextColor;
  }
  
  /**
   * Get section text position int.
   *
   * @return the int
   */
  public int getSectionTextPosition(){
    return sectionTextPosition;
  }
  
  /**
   * Get section text interval int.
   *
   * @return the int
   */
  public int getSectionTextInterval(){
    return sectionTextInterval;
  }
  
  /**
   * Is show thumb text boolean.
   *
   * @return the boolean
   */
  public boolean isShowThumbText(){
    return showThumbText;
  }
  
  /**
   * Get thumb text size int.
   *
   * @return the int
   */
  public int getThumbTextSize(){
    return thumbTextSize;
  }
  
  /**
   * Get thumb text color int.
   *
   * @return the int
   */
  public int getThumbTextColor(){
    return thumbTextColor;
  }
  
  /**
   * Is show progress in float boolean.
   *
   * @return the boolean
   */
  public boolean isShowProgressInFloat(){
    return showProgressInFloat;
  }
  
  /**
   * Get anim duration long.
   *
   * @return the long
   */
  public long getAnimDuration(){
    return animDuration;
  }
  
  /**
   * Is touch to seek boolean.
   *
   * @return the boolean
   */
  public boolean isTouchToSeek(){
    return touchToSeek;
  }
  
  /**
   * Is seek step section boolean.
   *
   * @return the boolean
   */
  public boolean isSeekStepSection(){
    return seekStepSection;
  }
  
  /**
   * Is seek by section boolean.
   *
   * @return the boolean
   */
  public boolean isSeekBySection(){
    return seekBySection;
  }
  
  /**
   * Get bubble color int.
   *
   * @return the int
   */
  public int getBubbleColor(){
    return bubbleColor;
  }
  
  /**
   * Get bubble text size int.
   *
   * @return the int
   */
  public int getBubbleTextSize(){
    return bubbleTextSize;
  }
  
  /**
   * Get bubble text color int.
   *
   * @return the int
   */
  public int getBubbleTextColor(){
    return bubbleTextColor;
  }
  
  /**
   * Is always show bubble boolean.
   *
   * @return the boolean
   */
  public boolean isAlwaysShowBubble(){
    return alwaysShowBubble;
  }
  
  /**
   * Get always show bubble delay long.
   *
   * @return the long
   */
  public long getAlwaysShowBubbleDelay(){
    return alwaysShowBubbleDelay;
  }
  
  /**
   * Is hide bubble boolean.
   *
   * @return the boolean
   */
  public boolean isHideBubble(){
    return hideBubble;
  }
  
  /**
   * Is rtl boolean.
   *
   * @return the boolean
   */
  public boolean isRtl(){
    return rtl;
  }
}

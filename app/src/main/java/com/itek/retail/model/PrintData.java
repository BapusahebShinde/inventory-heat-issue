package com.itek.retail.model;

import java.util.List;

public class PrintData{
  String title;
  String barcode;
  List<LabelValues> listData;
  String length;
  String unit;
  
  public PrintData(String title, String barcode, List<LabelValues> listData,String length,String unit){
    this.title = title;
    this.barcode = barcode;
    this.listData = listData;
    this.length=length;
    this.unit=unit;
  }
  
  public PrintData(String title, String barcode, List<LabelValues> listData){
    this.title = title;
    this.barcode = barcode;
    this.listData = listData;
  }
  
  public String getTitle(){
    return title;
  }
  
  public void setTitle(String title){
    this.title = title;
  }
  
  public String getBarcode(){
    return barcode;
  }
  
  public void setBarcode(String barcode){
    this.barcode = barcode;
  }
  
  public List<LabelValues> getListData(){
    return listData;
  }
  
  public void setListData(List<LabelValues> listData){
    this.listData = listData;
  }
  
  public String getLength(){
    return length;
  }
  
  public void setLength(String length){
    this.length = length;
  }
  
  public String getUnit(){
    return unit;
  }
  
  public void setUnit(String unit){
    this.unit = unit;
  }
}

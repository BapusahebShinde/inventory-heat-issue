package com.itek.retail.sgtin;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isIsAllowLeadingZeroForNonStdBarcode;
import static com.itek.retail.common.AppCommonMethods.isIsAllowLeadingZeroForStdBarcode;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.model.Inventory;
import com.itek.retail.nativelib.NativeBridge;

import java.util.Map;

public class EPCEncoderDecoder{
  
  public static String softTagEPCStart = "692d54454b72666964746167";
  public static String hardTagSerialNo = "10";
  public static String softTagSerialNo = "11";
  //TODO serial number based on config
  //public static final int SERIAL_NUMBER_LENGTH = 11;//12
  private static EPCEncoderDecoder instance;
  private EncodeAlgorithmStd encodeAlgorithmStd = null;
  private EncodeAlgorithmNonStd encodeAlgorithmNonStd = null;
  private boolean isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC = false;
  
  
  private EPCEncoderDecoder(final EncodeAlgorithmStd encodeAlgorithmStd, final EncodeAlgorithmNonStd encodeAlgorithmNonStd){
    this.encodeAlgorithmStd = encodeAlgorithmStd;
    this.encodeAlgorithmNonStd = encodeAlgorithmNonStd;
    isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC = encodeAlgorithmStd.value<0;
  }
  
  public static EPCEncoderDecoder getInstance(){
    final String encAlgoStd = SharedPrefManager.getString(ParamConstants.ENCODE_ALGORITHM_STD, "").replaceFirst("retailgtin",EncodeAlgorithmStd.SGTIN.name()).replaceFirst("tatagid",EncodeAlgorithmStd.TATA_GID.name());
    final String encAlgoNonStd = SharedPrefManager.getString(ParamConstants.ENCODE_ALGORITHM_NON_STD, "").replaceFirst("iteknonstandard",EncodeAlgorithmNonStd.ITEK_NONSTD.name());
    EncodeAlgorithmStd encodeAlgorithmStd = null;
    EncodeAlgorithmNonStd encodeAlgorithmNonStd = null;
    try{
      encodeAlgorithmStd = EncodeAlgorithmStd.valueOf(encAlgoStd);
      encodeAlgorithmNonStd = EncodeAlgorithmNonStd.valueOf(encAlgoNonStd);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return getInstance(encodeAlgorithmStd, encodeAlgorithmNonStd);
  }
  
  public static EPCEncoderDecoder getInstance(final EncodeAlgorithmStd encodeAlgorithmStd, final EncodeAlgorithmNonStd encodeAlgorithmNonStd){
    if(instance != null && encodeAlgorithmStd != null /*&& encodeAlgorithmStd.getValue() > 0*/ && encodeAlgorithmNonStd != null && encodeAlgorithmNonStd.getValue() > 0){
      instance.encodeAlgorithmStd = encodeAlgorithmStd;
      instance.encodeAlgorithmNonStd = encodeAlgorithmNonStd;
      instance.isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC = encodeAlgorithmStd.value<0;
    }
    else if(instance == null || instance.encodeAlgorithmStd == null || instance.encodeAlgorithmNonStd == null)//build DB
      instance = (encodeAlgorithmStd != null /*&& encodeAlgorithmStd.getValue() > 0*/ && encodeAlgorithmNonStd != null /*&& encodeAlgorithmNonStd.getValue() > 0*/) ? new EPCEncoderDecoder(encodeAlgorithmStd, encodeAlgorithmNonStd) : null;
    return instance;
  }
  
  public static int getEncodeDecodeLogic(){
    return 2;
  }
  
  public boolean isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC(){
    return isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC;
  }
  
  public void setIgnoreAlgorithmTypeForExtractingBarcodeFromEPC(boolean ignoreAlgorithmTypeForExtractingBarcodeFromEPC){
    isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC = ignoreAlgorithmTypeForExtractingBarcodeFromEPC;
  }
  
  public static boolean isStandardValidBarcode(String barcode){
    return iTEKGID96.isValidGID(barcode) || iTEKGID96.isValidGID(barcode);
  }
  
  public boolean isValidStandardBarcode(String barcode){
    return (isGID() && iTEKGID96.isValidGID(barcode)) || (isSGTIN() && SGTIN96.IsValidGtin(barcode));
  }
  
  public EncodeAlgorithmStd getBarcodeTypeStd(String barcode){
    final boolean isValidGTIN = isSGTIN() && SGTIN96.IsValidGtin(barcode);
    final boolean isValidGID = isGID() && iTEKGID96.isValidGID(barcode);
    if(!isValidGTIN && !isValidGID) return null;
    else if(isValidGTIN && !isValidGID) return EncodeAlgorithmStd.SGTIN;
    else if(!isValidGTIN && isValidGID) return EncodeAlgorithmStd.GID;
    else if(isValidGTIN && isValidGID) return EncodeAlgorithmStd.OTHER;
    else return null;
  }
  
  /**
   * is decoded from epc string.
   *
   * @param epc the epc
   * @return the boolean
   */
  public static boolean isDecoded(final String epc){
    return chkNull(epc, "").startsWith("0");
  }
  
  /**
   * is sold from epc string.
   *
   * @param epc the epc
   * @return the boolean
   */
  public static boolean isSold(final String epc){
    return chkNull(epc, "").length() >= 24 && chkNull(epc, "").startsWith("0");
  }
  
  /**
   * Get tag type int.
   *
   * @param epc the epc
   * @return the int
   */
  public int getTagType(String epc){
    if(epc == null || epc.length() <= 2) return 0;
    try{
      final String header = epc.toUpperCase().substring(0, 2).trim();
      final String barcode = getBarcodeFromEPC(epc);
      if(isNonEmpty(barcode)){
        if(isSGTIN() && header.equalsIgnoreCase("30") && epc.length() >= 24){
          return 1;
        }
        else if(isGID() && header.equalsIgnoreCase("35") && epc.length() >= 24){
          return 1;
        }
        else if(isITEK_NONSTD() && header.equalsIgnoreCase("BB") && epc.length() >= 24){
          return 2;
        }
        else if(isBCAlphaNumeric() && header.equalsIgnoreCase("BC") && epc.length() >= 32){
          return 2;
        }
        else if(isITEK_NONSTD() && header.equalsIgnoreCase("BD") && epc.length() >= 32){
          return 2;
        }
      }
      
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return 3;
  }
  
  private Map<String, Object> getBarcodeAndSerialFromEpc(final String epc){
    try{
      if(chkNull(epc, "").length() < 24) return null;
      final String header = epc.substring(0, 2).toUpperCase();
      switch(header){
        case "30":
          //inventory.tagtype = 1;
          return isSGTIN() ? SGTIN96.getBarcodeAndSerialFromEpc((epc.length()>24?epc.substring(0,24):epc).toUpperCase()) : null;
        //TODO To be added later
        /*
        case "7A":
          return chkNull(getLeftZeroReplacedString(SGTIN128Helper.getBarcodeFromEpc(epc)), AppConstants.UNKNOWN);
        case "7B":break;
          return chkNull(getLeftZeroReplacedString(SGTIN128Helper.getBarcodeFromEpc(epc)), AppConstants.UNKNOWN);
        */
        case "35":
          return isGID() ? iTEKGID96.getBarcodeAndSerialFromEpc((epc.length()>24?epc.substring(0,24):epc).toUpperCase()) : null;
        case "BC":
          //inventory.tagtype = 2;
          return isBCAlphaNumeric()? SGTIN128Helper.getBarcodeAndSerialFromEpc(epc.toUpperCase()): null;
        case "BB":
        case "BD":
          return isITEK_NONSTD()? new NativeBridge().decode(epc.toUpperCase().trim()).toMap() : null;
         // return isITEK_NONSTD()? iTEKNonStd.INSTANCE.decode(epc.toUpperCase().trim()) : null;
        default:
          return null;
      }
    }
    catch(Exception e){
      e.printStackTrace();
      return null;
    }
  }
  
  public Inventory setDataFromEPC(final Inventory inventory){
    final String epc = inventory.epc;
    inventory.tagtype = 3;
    inventory.isHardTag = null;
    //inventory.ean = AppConstants.NON_ENCODED;
    if(isNullOrEmpty(inventory.ean) || inventory.ean.matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")"))
      inventory.ean = AppConstants.NON_ENCODED;
    inventory.tagStatus = AppConstants.NON_ENCODED;
    if(chkNull(epc, "").length() < 24) return inventory;
    final String header = epc.substring(0, 2).toUpperCase();
    if(!header.matches(isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC?"(30|35|BC|BB|BD)":isGID(false) ? "(35|BC|BB|BD)" : "(30|BC|BB|BD)")) return inventory;
    try{
      final Map<String, Object> map = getBarcodeAndSerialFromEpc(epc.toUpperCase());
      if(map != null){
        if((isNullOrEmpty(inventory.ean) || inventory.ean.matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")"))){
          inventory.ean = getLeftZeroReplacedString(chkNull((String) map.get("ean"), AppConstants.NON_ENCODED).trim());
        }
        inventory.isHardTag = isTagTypeHard(epc, ((String) map.get("serial")).trim());
        inventory.tagtype = getTagType(epc);
        inventory.tagStatus = !header.matches("(?i)(BB|BC|BD)") && inventory.isHardTag == null  ? AppConstants.ALIEN : chkNull(inventory.ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? AppConstants.NON_ENCODED : AppConstants.VALID;
        /*switch(header){
          case "30": inventory.tagtype = 1; break;
          case "35": inventory.tagtype = 1; break;
          case "BC": inventory.tagtype = 2; break;
          default: inventory.tagtype = 3; break;
        }*/
        //return inventory;
      }
    }
    catch(Exception e){
      //return inventory;
    }
    return inventory;
  }
  
  /*private String getLeftZeroReplacedString(String barcode){
    return chkNull(barcode, "").trim().replaceFirst("^0+(?!$)", "");
  }*/
  
  private String getLeftZeroReplacedString(String barcode){
    final boolean isStdBarcode = isValidStdBarcode(barcode);
    if((isStdBarcode && SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_STD_BARCODE,isIsAllowLeadingZeroForStdBarcode))||(!isStdBarcode && SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_NON_STD_BARCODE,isIsAllowLeadingZeroForNonStdBarcode)))
      return chkNull(barcode, "");
    return chkNull(barcode, "").replaceFirst("^0+(?!$)", "");
  }
  
  /*private boolean isSGTIN(){
    return encodeAlgorithmStd != null && encodeAlgorithmStd == EncodeAlgorithmStd.SGTIN;
  }
  
  public boolean isGID(){
    return encodeAlgorithmStd != null && (encodeAlgorithmStd == EncodeAlgorithmStd.TATA_GID || encodeAlgorithmStd == EncodeAlgorithmStd.GID);
  }*/
  
  private boolean isSGTIN(){
    return isSGTIN(isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC);
    //return encodeAlgorithmStd != null && (isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC || encodeAlgorithmStd == EncodeAlgorithmStd.SGTIN);
  }
  
  public boolean isGID(){
    return isGID(isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC);
    //return encodeAlgorithmStd != null && (isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC || encodeAlgorithmStd == EncodeAlgorithmStd.TATA_GID || encodeAlgorithmStd == EncodeAlgorithmStd.GID);
  }
  
  private boolean isSGTIN(final boolean isIgnoreAlgorithmType){
    return encodeAlgorithmStd != null && (isIgnoreAlgorithmType || encodeAlgorithmStd == EncodeAlgorithmStd.SGTIN);
  }
  
  private boolean isGID(final boolean isIgnoreAlgorithmType){
    return encodeAlgorithmStd != null && (isIgnoreAlgorithmType || encodeAlgorithmStd == EncodeAlgorithmStd.TATA_GID || encodeAlgorithmStd == EncodeAlgorithmStd.GID);
  }
  
  public boolean isBCAlphaNumeric(){
    return isBCAlphaNumeric(isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC);
  }
  
  public boolean isITEK_NONSTD(){
    return isITEK_NONSTD(isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC);
  }
  
  private boolean isBCAlphaNumeric(final boolean isIgnoreAlgorithmType){
    return encodeAlgorithmNonStd!=null && (isIgnoreAlgorithmType || encodeAlgorithmNonStd == EncodeAlgorithmNonStd.BC_ALPHA_NUM);
  }
  
  private boolean isITEK_NONSTD(final boolean isIgnoreAlgorithmType){
    return encodeAlgorithmNonStd!=null && (isIgnoreAlgorithmType || encodeAlgorithmNonStd == EncodeAlgorithmNonStd.ITEK_NONSTD);
  }
  
  public String getEpcFromBarcode(final String barcode){ return getEpcFromBarcode(barcode, false); }//"10000");}
  
  public String getEpcFromBarcode(final String barcode, final boolean isGenerateSerial){
    return getEpcFromBarcode(barcode, isGenerateSerial,(EncodeAlgorithmStd) null);
  }
  
  public String getEpcFromBarcode(final String barcode, final boolean isGenerateSerial, EncodeAlgorithmStd std){
    return getEpcFromBarcode(barcode, !isGenerateSerial, !isGenerateSerial ? "10000" : "10" + String.valueOf(System.currentTimeMillis()).substring(2),std);
  }
  //public String getEpcFromBarcode(final String barcode){ return getEpcFromBarcode(barcode,"10000");}
  
  public boolean isValidStdBarcode(final String barcode){
    return (isGID() && iTEKGID96.isValidGID(barcode)) || (isSGTIN() && SGTIN96.IsValidGtin(barcode));
  }
  
  public String getEpcFromBarcode(final String barcode, final boolean isFixedSerial, String serial){
    //final boolean isFixedSerial=serial.length()<7;
    if(isGID(false) && iTEKGID96.isValidGID(barcode)){
      String enc = iTEKGID96.GetEpc(barcode, Long.parseLong(serial.length() > 11 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 9) : serial));//10000l);
      if(enc != null){
        String s = isFixedSerial && enc.length() > 15 ? enc.substring(0, 15) : enc;
        return s;
      }
      else{
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
      }
    }
    else if(isSGTIN(false) && SGTIN96.IsValidGtin(barcode)){
      String enc = SGTIN96.convertToSGTIN96(barcode, 7, Long.parseLong(serial.length() > 12 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 10) : serial), 1);
      if(enc != null) return isFixedSerial && enc.length() > 14 ? enc.substring(0, 14) : enc;
      else
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
    }
    else{
      return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial).toUpperCase();
    }
  }
  
  /*public String getEpcFromBarcode(final String barcode, final boolean isFixedSerial, String serial){
    return getEpcFromBarcode(barcode,isFixedSerial,serial,null);
  }*/
  
  public String getEpcFromBarcode(final String barcode, final boolean isFixedSerial, String serial, EncodeAlgorithmStd std){
    //final boolean isFixedSerial=serial.length()<7;
    if(isIgnoreAlgorithmTypeForExtractingBarcodeFromEPC && std==null){
      final EncodeAlgorithmStd algorithmStd = getBarcodeTypeStd(barcode);
      /*if(algorithmStd==null) return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial).toUpperCase();
      else */if(algorithmStd!=null && algorithmStd== EncodeAlgorithmStd.OTHER) return null;
      else std=algorithmStd;
      /*final boolean isValidGTIN = SGTIN96.IsValidGtin(barcode);
      final boolean isValidGID = iTEKGID96.isValidGID(barcode);
      if(!isValidGTIN && !isValidGID) return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial).toUpperCase();
      else if(!isValidGTIN && isValidGID){
        String enc = iTEKGID96.GetEpc(barcode, Long.parseLong(serial.length() > 11 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 9) : serial));//10000l);
        if(enc != null){
          String s = isFixedSerial && enc.length() > 15 ? enc.substring(0, 15) : enc;
          return s;
        }
        else
          return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
      }
      else if(isValidGTIN && !isValidGID){
        String enc = SGTIN96.convertToSGTIN96(barcode, 7, Long.parseLong(serial.length() > 12 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 10) : serial), 1);
        if(enc != null) return isFixedSerial && enc.length() > 14 ? enc.substring(0, 14) : enc;
        else
          return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
      }
      else if(isValidGTIN && isValidGID) return null;*/
    }
    
    if((isGID(false) || (std!=null && (std== EncodeAlgorithmStd.GID || std== EncodeAlgorithmStd.TATA_GID))) && iTEKGID96.isValidGID(barcode)){
      String enc = iTEKGID96.GetEpc(barcode, Long.parseLong(serial.length() > 11 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 9) : serial));//10000l);
      if(enc != null){
        String s = isFixedSerial && enc.length() > 15 ? enc.substring(0, 15) : enc;
        return s;
      }
      else{
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
      }
    }
    else if((isSGTIN(false) || std!=null && std== EncodeAlgorithmStd.SGTIN) && SGTIN96.IsValidGtin(barcode)){
      String enc = SGTIN96.convertToSGTIN96(barcode, 7, Long.parseLong(serial.length() > 12 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 10) : serial), 1);
      if(enc != null) return isFixedSerial && enc.length() > 14 ? enc.substring(0, 14) : enc;
      else
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
    }
    else if(isITEK_NONSTD()){
     // String enc = iTEKNonStd.INSTANCE.encode(barcode,serial.length() > 8 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 6) : serial);
      String enc = new NativeBridge().encode(barcode,serial.length() > 8 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 6) : serial,"iteknonstandard").getEpc();
      if(enc != null) return !isFixedSerial?enc: enc.length() > 26 ? enc.substring(0, 26) : enc.length()> 18? enc.substring(0, 18) : enc;
      else
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial.length() > 7 ? "10" + serial.replaceFirst("10", "").substring(serial.length() - 5) : serial).toUpperCase();
    }
    else{ return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, serial).toUpperCase(); }
  }
  
  public String getSgtinFromBarcode(final String barcode, final String tid){
    //final boolean isFixedSerial=serial.length()<7;
    String serial = "33554431";
    
    if(isGID() && iTEKGID96.isValidGID(barcode)){
      serial = "68719476735";
      long serialNumber = OfflineSgtin.generateSerialNumber(SharedPrefManager.getIMEI(), barcode, tid, serial);
      String enc = iTEKGID96.GetEpc(barcode, serialNumber);//10000l);
      if(enc != null){
        String s = enc;
        return s;
      }
      else{
        serial = "33554431";
        long serialNumber2 = OfflineSgtin.generateSerialNumber(SharedPrefManager.getIMEI(), barcode, tid, serial);
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, String.valueOf(serialNumber2)).toUpperCase();
      }
    }
    else if(SGTIN96.IsValidGtin(barcode)){
      serial = "274877906943";
      long serialNumber3 = OfflineSgtin.generateSerialNumber(SharedPrefManager.getIMEI(), barcode, tid, serial);
      String enc = SGTIN96.convertToSGTIN96(barcode, 7, serialNumber3, 1);
      if(enc != null) return enc;
      else{
        serial = "33554431";
        long serialNumber4 = OfflineSgtin.generateSerialNumber(SharedPrefManager.getIMEI(), barcode, tid, serial);
        return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, String.valueOf(serialNumber4)).toUpperCase();
      }
      
    }
    else{
      serial = "33554431";
      long serialNumber5 = OfflineSgtin.generateSerialNumber(SharedPrefManager.getIMEI(), barcode, tid, serial);
      return SGTIN128Helper.getSgtinFromBarcodeAndSrno(barcode, String.valueOf(serialNumber5)).toUpperCase();
    }
  }
  
  public String getBarcodeFromEPC(final String epc){ return getBarcodeFromEPC(epc, false); }
  
  public String getEpcFromDecodedEpc(final String decodedEpc){
    if(chkNull(decodedEpc, "").length() < 24) return "";
    final String epcUpper = decodedEpc.toUpperCase().trim();
    final String header = epcUpper.substring(0, 2).trim();
    if(!header.startsWith("0")) return decodedEpc;
    switch(header){
      case "0B":
        return epcUpper.replaceFirst(header, "BB");
      case "0D":
        return epcUpper.replaceFirst(header, "BD");
      case "0C":
        return epcUpper.replaceFirst(header, "BC");
      case "05":
        return epcUpper.replaceFirst(header, "35");
      case "00":
        return epcUpper.length() >= 32 ? isITEK_NONSTD()? epcUpper.replaceFirst(header, "BD") : epcUpper.replaceFirst(header, "BC") : epcUpper.replaceFirst(header, (isITEK_NONSTD()? "BB" : isGID() ? "35" : "30"));
      default:
        return decodedEpc;
    }
  }
  
  public String getBarcodeFromEPC(final String epc, final boolean isCheckDecoded){
    //TODO check here if decode should be ignored
    if(chkNull(epc, "").length() < 24) return "";
    final String epcUpper = epc.toUpperCase().trim();
    final String header = epcUpper.substring(0, 2).trim();//.toUpperCase().trim();
    showLog("header", header);
    try{
      switch(header){
        case "0B":
          return isCheckDecoded ? getBarcodeFromEPC(epcUpper.replaceFirst(header, "BB")) : AppConstants.NON_ENCODED;
        case "0D":
          return isCheckDecoded ? getBarcodeFromEPC(epcUpper.replaceFirst(header, "BD")) : AppConstants.NON_ENCODED;
        case "0C":
          return isCheckDecoded ? getBarcodeFromEPC(epcUpper.replaceFirst(header, "BC")) : AppConstants.NON_ENCODED;
        case "05":
          return isCheckDecoded && isGID() ? getBarcodeFromEPC(epcUpper.replaceFirst(header, "35")) : AppConstants.NON_ENCODED;
        case "00":
          return isCheckDecoded ? getBarcodeFromEPC(epcUpper.length() >= 32 ? isITEK_NONSTD()?epcUpper.replaceFirst(header, "BD") :epcUpper.replaceFirst(header, "BC") : epcUpper.replaceFirst(header, (isITEK_NONSTD()?"BB":isSGTIN()?"30": isGID() ? "35" : "30"))) : AppConstants.NON_ENCODED;
        //case "30":
        //return isSGTIN() ? getLeftZeroReplacedString((String) SGTIN96.getBarcodeAndSerialFromEpc(epcUpper).get("ean")) : AppConstants.NON_ENCODED;
        //case "35":
        //return isGID() ? getLeftZeroReplacedString((String) iTEKGID96.getBarcodeAndSerialFromEpc(epcUpper).get("ean")) : AppConstants.NON_ENCODED;
        //TODO To be added later
        /*
        case "7A":
          return chkNull(getLeftZeroReplacedString(SGTIN128Helper.getBarcodeFromEpc(epc)), AppConstants.UNKNOWN);
        case "7B":break;
          return chkNull(getLeftZeroReplacedString(SGTIN128Helper.getBarcodeFromEpc(epc)), AppConstants.UNKNOWN);
        */
        //case "BC":
        //return chkNull(getLeftZeroReplacedString((String) SGTIN128Helper.getBarcodeAndSerialFromEpc(epcUpper, isBCAlphaNumeric()).get("ean")), AppConstants.NON_ENCODED);
        
        case "30":
        case "35":
        case "BC":
        case "BB":
        case "BD":
          final Map m = getBarcodeAndSerialFromEpc(epcUpper);
          showLog("m", m != null ? m.toString() : "null");
          return (m != null) ? getLeftZeroReplacedString(((String) m.get("ean")).trim()) : AppConstants.NON_ENCODED;
        default:
          return AppConstants.NON_ENCODED;
      }
    }
    catch(Exception e){ return AppConstants.NON_ENCODED; }
  }
  
  public Boolean isValidItekTag(final String epc){
    if(!SharedPrefManager.getBoolean(ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_EPC)) return true;
    if(!isValidAlgorithmHeader(epc)) return false;
    final String serial = getSerialFromEpc(epc,true);
    return isValidSerialNumber(epc,serial) && serial.startsWith(hardTagSerialNo);
  }
  
  private String getSerialFromEpc(final String epc, final boolean isCheckDecoded){
    if(chkNull(epc, "").length() < 24) return "";
    final String epcUpper = epc.toUpperCase().trim();
    final String header = epcUpper.substring(0, 2).trim();//.toUpperCase().trim();
    try{
      switch(header){
        case "0B":
          return isCheckDecoded ? getSerialFromEpc(epcUpper.replaceFirst(header, "BB"), false) : AppConstants.NON_ENCODED;
        case "0D":
          return isCheckDecoded ? getSerialFromEpc(epcUpper.replaceFirst(header, "BD"), false) : AppConstants.NON_ENCODED;
        case "0C":
          return isCheckDecoded ? getSerialFromEpc(epcUpper.replaceFirst(header, "BC"), false) : AppConstants.NON_ENCODED;
        case "05":
          return isCheckDecoded && isGID() ? getSerialFromEpc(epcUpper.replaceFirst(header, "35"), false) : AppConstants.NON_ENCODED;
        case "00":
          return isCheckDecoded ? getSerialFromEpc(epcUpper.length() >= 32 ? isITEK_NONSTD() ? epcUpper.replaceFirst(header, "BD") : epcUpper.replaceFirst(header, "BC") : epcUpper.replaceFirst(header, (isITEK_NONSTD() ? "BB" : isSGTIN() ? "30" : isGID() ? "35" : "30")),false) : AppConstants.NON_ENCODED;
      }
      final Map<String, Object> m = getBarcodeAndSerialFromEpc(epc);
      if(m != null) return ((String) m.get("serial")).trim();
    }
    catch(Exception e){ }
    return null;
  }
  
  public boolean isValidHeader(final String epc){
    return chkNull(epc, "").length() >= 24;
  }
  
  public boolean isValidAlgorithmHeader(final String epc){
     if(chkNull(epc, "").length()<4) return false;
     final String header = epc.substring(0,2).toUpperCase().trim();
     switch(header){
       case "00":
       case "30":
         return isSGTIN(false);
       case "05":
       case "35":
         return isGID(false);
       case "0C":
       case "BC":
         return isBCAlphaNumeric(false);
       case "BB":
       case "BD":
       case "0B":
       case "0D":
         return isITEK_NONSTD(false);
       default: return false;
     }
  }
  
  public String replaceHeader(final String epc){
    if(chkNull(epc,"").length()<24) return "";
    if(epc.startsWith("0C") && epc.length()>24) return "BC"+epc.substring(2);
    if(epc.startsWith("0D") && epc.length()>24) return "BD"+epc.substring(2);
    if(epc.startsWith("0B") && epc.length()==24) return "BB"+epc.substring(2);
    if(isGID() || epc.startsWith("05")) return "35"+epc.substring(2);
    if(epc.startsWith("00")) return (isSGTIN()?"30":isGID()?"35":"30")+epc.substring(2);
    return "";
  }
  
  public Inventory setPCDataBeforeEncoding(Inventory pickedTag){
    if(pickedTag.epc.length()==pickedTag.newEpc.length()) return pickedTag;
    if(pickedTag.pcdata.length() < 1) return pickedTag;
    pickedTag.pcdata= (pickedTag.newEpc.length() / 8)+pickedTag.pcdata.substring(1);
    pickedTag.newEpc = pickedTag.pcdata + pickedTag.newEpc;
    
    //final String sgtin = pickedTag.newEpc;
    //old code commented
    /*final String header = pickedTag.newEpc.substring(0, 2);
    boolean access_to_write = true;
    AppCommonMethods.showLog("pcData1", pickedTag.pcdata);
    switch(pickedTag.pcdata){
      case "3000":
      case "3400":
        if(header.equalsIgnoreCase("BC")){
          pickedTag.pcdata = pickedTag.pcdata.replaceFirst("3", "4");
          pickedTag.newEpc = pickedTag.pcdata + pickedTag.newEpc;
        }
        break;
      case "4000":
      case "4400":
        if(header.matches("(30|35)")){
          pickedTag.pcdata = pickedTag.pcdata.replaceFirst("4", "3");
          pickedTag.newEpc = pickedTag.pcdata + pickedTag.newEpc;
        }
        break;
      default:
        if(header.equalsIgnoreCase("BC")){
          pickedTag.pcdata = "4000";
          pickedTag.newEpc = "4000" + pickedTag.newEpc;
        }
        else if(header.matches("(30|35)")){
          pickedTag.pcdata = "3000";
          pickedTag.newEpc = "3000" + pickedTag.newEpc;
        }
        else access_to_write = false;
        break;
    }*/
    return pickedTag;
  }
  
  public Boolean isTagTypeHard(final String epc){
    return isTagTypeHard(epc, getSerialFromEpc(epc,true));
  }
  
  private Boolean isValidSerialNumberLength(String epc, String serial){
    if(serial != null){
      if(isSGTIN() && epc.matches("(?i)(^(00|30).*$)") && serial.length() == 12) return true;
      if(isGID() && epc.matches("(?i)(^(05|35).*$)") && serial.length() == 11) return true;
      if(isITEK_NONSTD() && epc.matches("(?i)(^(0B|BB|0D|BD).*$)") && serial.length() == 8) return true;
      if(isBCAlphaNumeric() && epc.matches("(?i)(^(0C|BC).*$)") && (serial.length() == 7 || serial.length() == 8)) return true;
    }
    return false;
  }
  
  private Boolean isValidSerialNumber(String epc, String serial){
    try{
      if(epc.length() >= 24 && epc.substring(0, 24).matches(softTagEPCStart)) return true;
      return serial != null && isValidSerialNumberLength(epc,serial) && (serial.substring(0, 2).equalsIgnoreCase(softTagSerialNo) || serial.substring(0, 2).equalsIgnoreCase(hardTagSerialNo));
    }catch(Exception e){e.printStackTrace();}
    return false;
  }
  
  private Boolean isTagTypeHard(String epc, String serial){
    try{
      if(epc.length() >= 24 && epc.substring(0, 24).matches(softTagEPCStart)) return false;
      //String barcode = getSkuFromEpc(epc);
      if(isSGTIN() && epc.startsWith("30")){
        if(serial != null && serial.length() == 12){
          if(serial.substring(0, 2).equalsIgnoreCase(softTagSerialNo)) return false;
          else if(serial.substring(0, 2).equalsIgnoreCase(hardTagSerialNo)) return true;
        }
      }
      else if(isGID() && epc.startsWith("35")){
        if(serial != null && serial.length() == 11){
          if(serial.substring(0, 2).equalsIgnoreCase(softTagSerialNo)) return false;
          else if(serial.substring(0, 2).equalsIgnoreCase(hardTagSerialNo)) return true;
        }
      }
      if(isITEK_NONSTD() && epc.matches("(?i)(^(BB|BD).*$)")){
        if(serial != null && serial.length() == 8){
          if(serial.substring(0, 2).equalsIgnoreCase(softTagSerialNo)) return false;
          else if(serial.substring(0, 2).equalsIgnoreCase(hardTagSerialNo)) return true;
        }
      }
      else if(isBCAlphaNumeric() && (epc.startsWith("BC") || epc.startsWith("bc"))){
        if(serial != null && (serial.length() == 7 || serial.length() == 8)){
          if(serial.substring(0, 2).equalsIgnoreCase(softTagSerialNo)) return false;
          else if(serial.substring(0, 2).equalsIgnoreCase(hardTagSerialNo)) return true;
        }
      }
    }
    catch(Exception e){
      e.printStackTrace();
      return null;
    }
    return null;
  }
  
  //public String getNewEpc(String epc, String ean, String serial){return getNewEpc(epc,ean,serial);}
  public String getNewEpc(String epc, String ean, String serial, EncodeAlgorithmStd std){
    if(epc.startsWith("0") && getLeftZeroReplacedString(getBarcodeFromEPC(epc,true)).trim().equalsIgnoreCase(ean))
      return replaceHeader(epc);
    else
      return getEpcFromBarcode(ean,false,serial,std);
  }
  
  public enum EncodeAlgorithmStd{
    OTHER(0), SGTIN(1), GID(2), TATA_GID(3);
    
    private final int value;
    
    /**
     * Instantiates a new Device type.
     *
     * @param newValue the new value
     */
    EncodeAlgorithmStd(final int newValue){ value = newValue; }
    
    /**
     * Get device type.
     *
     * @param value the value
     * @return the device type
     */
    public static EncodeAlgorithmStd get(int value){
      return EncodeAlgorithmStd.values()[value];
    }
    
    public static int getLimit(){
      return EncodeAlgorithmStd.values().length;
    }
    
    /**
     * Get value int.
     *
     * @return the int
     */
    public int getValue(){ return value; }
  }
  
  public enum EncodeAlgorithmNonStd{
    OTHER(0), BC_NUM(1), BC_ALPHA_NUM(2), ITEK_NONSTD(3);
    
    private final int value;
    
    /**
     * Instantiates a new Device type.
     *
     * @param newValue the new value
     */
    EncodeAlgorithmNonStd(final int newValue){ value = newValue; }
    
    /**
     * Get device type.
     *
     * @param value the value
     * @return the device type
     */
    public static EncodeAlgorithmNonStd get(int value){
      return EncodeAlgorithmNonStd.values()[value];
    }
    
    /**
     * Get value int.
     *
     * @return the int
     */
    public int getValue(){ return value; }
  }
}

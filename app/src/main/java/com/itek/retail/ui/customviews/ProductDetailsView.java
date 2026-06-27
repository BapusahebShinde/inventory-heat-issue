package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isShowCheckAvailabilityBtnForProductDetails;
import static com.itek.retail.common.AppCommonMethods.isValidUrl;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.itek.retail.R;
import com.itek.retail.adapter.ProdDisplayDataListAdapter;
import com.itek.retail.adapter.ProductDetailsListAdapter;
import com.itek.retail.adapter.ProductImagesListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.DialogProductDetailsBinding;
import com.itek.retail.model.LabelValues;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.FileViewerFragment;
import com.itek.retail.ui.actionmenu.ActionMenuCompareFragment;
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Product details view.
 */
public class ProductDetailsView extends LinearLayout{
  
  Context context;
  TypedArray typedArray;
  LinearLayout llRoot, llImgData, llTxtData;
  RecyclerView listTxtData;
  ViewPager2 pagerProdImages;
  ImageView imgSold;
  ImageView imgStatus;
  ConstraintLayout clImgPager;
  TextView txtImgCount, txtDetails, txtVideo;
  Button btnSimilarProducts;
  boolean isVerticalView = false, isShowDataAsList = true;
  float imgWeight = 2.0f, lblWeight = 2.0f;
  ProductModel productModel = null;
  ProductModel compareProductModel = null;
  
  AlertDialog productDetailsAlert;
  
  /**
   * Instantiates a new Product details view.
   *
   * @param context the context
   */
  public ProductDetailsView(Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Product details view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public ProductDetailsView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Product details view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public ProductDetailsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Product details view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   * @param defStyleRes  the def style res
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ProductDetailsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
  }
  
  /**
   * Init.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  void init(Context context, @Nullable AttributeSet attrs){
    ProductDetailsView.this.context = context;
    final View root = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_product_data, this, true);
    llRoot = root.findViewById(R.id.llProductData);
    llImgData = root.findViewById(R.id.llProductImgData);
    clImgPager = root.findViewById(R.id.clImgPager);
    pagerProdImages = root.findViewById(R.id.pagerProdImages);
    
    imgSold = root.findViewById(R.id.imgSold);
    imgStatus = root.findViewById(R.id.imgStatus);
    txtImgCount = root.findViewById(R.id.txtProductImgCount);
    txtVideo = root.findViewById(R.id.txtProductVideo);
    txtDetails = root.findViewById(R.id.txtProductDetails);
    //txtVideo.setMovementMethod(LinkMovementMethod.getInstance());
    btnSimilarProducts = root.findViewById(R.id.btnSimilarProducts);
    
    llTxtData = root.findViewById(R.id.llProductTxtData);
    listTxtData = root.findViewById(R.id.listTxtData);
    if(context instanceof CommonActivity){
      txtDetails.setTextColor(((CommonActivity) context).getColorPrimaryDarkFromTheme());
      txtVideo.setTextColor(((CommonActivity) context).getColorPrimaryDarkFromTheme());
    }
    else{
      TypedValue tv = new TypedValue();
      txtDetails.setTextColor(context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, tv, true) ? tv.data : ContextCompat.getColor(context, R.color.colorPrimaryDarkDef));
      txtVideo.setTextColor(context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, tv, true) ? tv.data : ContextCompat.getColor(context, R.color.colorPrimaryDarkDef));
    }
    
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProductDetailsView, 0, 0);
    if(typedArray != null){
      isVerticalView = typedArray.getBoolean(R.styleable.ProductDetailsView_isVerticalView, false);
      imgWeight = typedArray.getFloat(R.styleable.ProductDetailsView_img_weight, isVerticalView ? 1.0f : 2.0f);
      lblWeight = typedArray.getFloat(R.styleable.ProductDetailsView_lbl_weight, 2.0f);
      isShowDataAsList = typedArray.getBoolean(R.styleable.ProductDetailsView_is_data_list, true);
    }
    setLayout();
  }
  
  /**
   * Set layout.
   */
  private void setLayout(){
    llRoot.setOrientation(isVerticalView ? VERTICAL : HORIZONTAL);
    llRoot.getLayoutParams().height = isVerticalView ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
    ((LayoutParams) llImgData.getLayoutParams()).weight = imgWeight;
    llImgData.setBackgroundResource(isVerticalView ? R.color.transparent : R.drawable.border_right);
    
  }
  
  /**
   * Is vertical view boolean.
   *
   * @return the boolean
   */
  public boolean isVerticalView(){
    return isVerticalView;
  }
  
  /**
   * Set vertical view.
   *
   * @param verticalView the vertical view
   */
  public void setVerticalView(boolean verticalView){
    isVerticalView = verticalView;
    setLayout();
  }
  
  /**
   * Is show data as list boolean.
   *
   * @return the boolean
   */
  public boolean isShowDataAsList(){
    return isShowDataAsList;
  }
  
  /**
   * Set show data as list.
   *
   * @param showDataAsList the show data as list
   */
  public void setShowDataAsList(boolean showDataAsList){
    isShowDataAsList = showDataAsList;
  }
  
  /**
   * Get product model product model.
   *
   * @return the product model
   */
  public ProductModel getProductModel(){
    return productModel;
  }
  
  /**
   * Set product model.
   *
   * @param productModel the product model
   */
  public void setProductModel(ProductModel productModel){
    setProductModel(productModel, "");
  }
  
  /**
   * Set product model.
   *
   * @param productModel      the product model
   * @param replenishmentType the replenishment type
   */
  public void setProductModel(ProductModel productModel, String replenishmentType){
    setProductModel(productModel, replenishmentType, true, null);
  }
  
  /**
   * Set product model.
   *
   * @param productModel the product model
   * @param isShowZone   the is show zone
   */
  public void setProductModel(ProductModel productModel, boolean isShowZone){
    setProductModel(productModel, "", isShowZone, null);
  }
  
  /**
   * Set product model.
   */
  public void setProductModel(ProductModel productModel, VideoView videoView){
    setProductModel(productModel, "", false, videoView);
  }
  
  /**
   * Set product model.
   *
   * @param productModel      the product model
   * @param replenishmentType the replenishment type
   */
  public void setProductModel(ProductModel productModel, String replenishmentType, boolean isShowZone, VideoView videoView){
    this.productModel = productModel;
    if(productModel == null) setFragmentButton(null, "");
    updateProductData(videoView, replenishmentType, isShowZone);
  }
  
  /**
   * Get compare product model product model.
   *
   * @return the product model
   */
  public ProductModel getCompareProductModel(){
    return compareProductModel;
  }
  
  /**
   * Set compare product model.
   *
   * @param compareProductModel the compare product model
   */
  public void setCompareProductModel(ProductModel compareProductModel){
    this.compareProductModel = compareProductModel;
  }
  
  @Override
  public void setEnabled(boolean isEnabled){
    llImgData.setEnabled(isEnabled);
    llTxtData.setEnabled(isEnabled);
    pagerProdImages.setEnabled(isEnabled);
    if(txtDetails != null) txtDetails.setEnabled(isEnabled);
    if(txtVideo != null) txtVideo.setEnabled(isEnabled);
    if(btnSimilarProducts != null) btnSimilarProducts.setEnabled(isEnabled);
  }
  
  /**
   * Update product data.
   *
   * @param replenishmentType the replenishment type
   */
  private void updateProductData(final String replenishmentType, final boolean isShowZone){
    updateProductData(null, replenishmentType, isShowZone);
  }
  
  /**
   * Update product data.
   *
   * @param replenishmentType the replenishment type
   * @param isShowZone        the is show zone
   */
  private void updateProductData(final VideoView videoView, final String replenishmentType, final boolean isShowZone){
    txtImgCount.setText("");
    txtImgCount.setVisibility(GONE);
    imgSold.setVisibility(GONE);
    imgStatus.setVisibility(GONE);
    listTxtData.setVisibility(GONE);
    llTxtData.setVisibility(GONE);
    JSONArray jsonDataArray = productModel != null ? productModel.getDisplayData(context, compareProductModel, replenishmentType, isShowZone) : null;
    if(productModel != null){
      //setup Images
      imgSold.setVisibility(productModel.getSold() ? VISIBLE : GONE);
      imgStatus.setVisibility(productModel.getClosed() || productModel.getIsCompleted() || productModel.getIsMismatched() ? VISIBLE : GONE);
      imgStatus.setImageResource(productModel.getClosed() ? R.drawable.ic_disable : productModel.getIsCompleted() ? R.drawable.ic_success : R.drawable.ic_error);
      final int totalCount = productModel.getItemImgUrl().split(",").length;
      txtImgCount.setVisibility(totalCount > 1 ? VISIBLE : GONE);
      final String prodDetails = getProductModel().getDisplayDataDetails();
      final String videoUrl = getProductModel().getItemVideoUrl();
      //txtDetails.setVisibility(isNonEmpty(prodDetails) ? VISIBLE : GONE);
      txtVideo.setVisibility(isNonEmpty(videoUrl) ? VISIBLE : GONE);
      //btnSimilarProducts.setVisibility(isNonEmpty(videoUrl) ? VISIBLE : GONE);
      if(isNonEmpty(prodDetails)){
        try{
          JSONArray prodDtls = new JSONArray(prodDetails);
          if(isNonEmpty(prodDtls)){
            final List<String> listLabels = new ArrayList<>(0);
            final List<List<LabelValues>> listLblVals = new ArrayList<>(0);
            for(int i = 0; i < prodDtls.length(); i++){
              listLblVals.add(i, new ArrayList<>());
              final Object obj = prodDtls.get(i);
              final JSONObject jObj = obj != null && obj instanceof JSONObject ? (JSONObject) obj : null;
              final JSONArray jarrayFields = jObj != null ? extractJSONArray(jObj, "Fields") : null;
              if(isNonEmpty(jarrayFields)){
                for(int j = 0; j < jarrayFields.length(); j++){
                  try{
                    final LabelValues lblValues = AppCommonMethods.getGSON().fromJson(jarrayFields.getJSONObject(j).toString(), LabelValues.class);
                    if(lblValues != null && isNonEmpty(lblValues.getLabel()) /*&& isNonEmpty(lblValues.getValue())*/){
                      if(i == 0 && !listLabels.contains(lblValues.getLabel()))
                        listLabels.add(lblValues.getLabel());
                      listLblVals.get(i).add(lblValues);
                    }
                  }
                  catch(JSONException e){
                    e.printStackTrace();
                  }
                }
              }
              
            }
            
            txtDetails.setVisibility(isNonEmpty(listLabels) ? VISIBLE : GONE);
            if(isNonEmpty(listLabels)){
              showLog("listLabels", listLabels.toString());
              txtDetails.setText(R.string.icon_info);
              txtDetails.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View view){
                  if(view != null && view.getVisibility() == View.VISIBLE && context instanceof CommonActivity){
                    showProductDetailsAlert(listLabels, listLblVals);
                  }
                }
              });
            }
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
        
      }
      if(isNonEmpty(videoUrl)){
        final String vidUrl = isNonEmpty(videoUrl) ? isValidUrl(videoUrl) ? videoUrl : SharedPrefManager.getServerUrl().replaceFirst(AppCommonMethods.SERVER_URL_APPEND_API, videoUrl.startsWith("/") ? "" : AppCommonMethods.SERVER_URL_APPEND_IMG) + videoUrl.replaceAll("(\"|\\[|\\]|,null|null,)", "").trim().split(",")[0] : "";
        showLog("vidUrl", vidUrl);
        //1. Redirect to Browser
        //txtVideo.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_prod_video), vidUrl), HtmlCompat.FROM_HTML_MODE_LEGACY));
        //txtVideo.setText(HtmlCompat.fromHtml(context.getString(R.string.lbl_prod_video), HtmlCompat.FROM_HTML_MODE_LEGACY));
        //txtVideo.setText(R.string.icon_video);
        //}
        if(videoView != null){
          MediaController mediaController = new MediaController(context);
          mediaController.setAnchorView(videoView);
          videoView.setMediaController(mediaController);
        }
        txtVideo.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View view){
            //2. Open In Pre-Installed Players
            //Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(vidUrl));
            //intent.setDataAndType(Uri.parse(vidUrl), "video/*");
            //context.startActivity(Intent.createChooser(intent,"Select"));
            //3. Open VideoView
            //3.1 VideoView on Same Fragment
            if(videoView != null){
              videoView.setVideoPath(vidUrl);
              //videoView.setVideoURI(Uri.parse(vidUrl));
              videoView.setVisibility(VISIBLE);
              videoView.start();
            }
            //3.2 Open Separate Fragment for VideoView
            if(allowBtnClick && context instanceof CommonActivity){
              allowBtnClick = false;
              Bundle args = new Bundle();
              args.putString(AppConstants.FILE_PATH, vidUrl);
              ((CommonActivity) context).loadFragment(new FileViewerFragment(), args);
            }
          }
        });
      }
      pagerProdImages.setAdapter(new ProductImagesListAdapter((MainActivity) context, pagerProdImages, productModel.getItemImgUrl().split(",")));
      pagerProdImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
          super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
        
        @Override
        public void onPageSelected(int position){
          super.onPageSelected(position);
          txtImgCount.setText((position + 1) + "/" + totalCount);
        }
      });
    }
    setProductData(jsonDataArray);
  }
  
  /**
   * Set product data.
   *
   * @param jsonDataArray the json data array
   */
  private void setProductData(JSONArray jsonDataArray){
    if(isNonEmpty(jsonDataArray)){
      listTxtData.setVisibility(isShowDataAsList ? VISIBLE : GONE);
      llTxtData.setVisibility(!isShowDataAsList ? VISIBLE : GONE);
      llTxtData.removeAllViews();
      List<LabelValues> listData = new ArrayList<>(0);
      for(int i = 0; i < jsonDataArray.length(); i++){
        try{
          JSONObject jsonObject = jsonDataArray.getJSONObject(i);
          if(!jsonObject.has(ParamConstants.LABEL) || !jsonObject.has(ParamConstants.VALUE)){
            //TODO
            String label = jsonObject.keys().toString();
            String value = jsonObject.getString(label);
          }
          final LabelValues lblValues = AppCommonMethods.getGSON().fromJson(jsonDataArray.getJSONObject(i).toString(), LabelValues.class);
          if(lblValues != null && isNonEmpty(lblValues.getLabel()) && isNonEmpty(lblValues.getValue())){
            if(isShowDataAsList) listData.add(lblValues);
            else{
              final int margin = getResources().getDimensionPixelSize(R.dimen.dp_5);
              final View root = ((CommonActivity) context).getLayoutInflater().inflate(R.layout.view_label_value_pair, null);
              root.setBackgroundResource(i % 2 == 0 ? R.color.white : R.color.bgListAlternet);
              /*if(isVerticalView)*/
              root.setPadding(0, margin, 0, margin);
              final TextView txtLabel = root.findViewById(R.id.txt_row_label);
              final TextView txtValue = root.findViewById(R.id.txt_row_value);
              txtLabel.setSelected(true);
              txtValue.setSelected(true);
              txtLabel.setText(HtmlCompat.fromHtml(lblValues.getLabel(), HtmlCompat.FROM_HTML_MODE_LEGACY));
              final boolean isHexColorCode = lblValues.getValue().matches(AppConstants.REGEX_HEX_COLOR_CODE);
              ((CommonActivity) context).setTextAppearance(txtValue, isHexColorCode ? R.style.TextStyleSubSubHeaderAwesome : R.style.TextStyleSubSubHeader);
              txtValue.setText(HtmlCompat.fromHtml(isHexColorCode ? String.format(context.getString(R.string.txt_color_code), lblValues.getValue()) : lblValues.getValue(), HtmlCompat.FROM_HTML_MODE_LEGACY));
              root.setBackgroundResource(i % 2 == 0 ? R.color.white : R.color.bgListAlternet);
              llTxtData.addView(root);
            }
          }
        }
        catch(JSONException e){
          e.printStackTrace();
        }
      }
      if(isShowDataAsList){
        listTxtData.setAdapter(new ProdDisplayDataListAdapter((MainActivity) context, listData, isVerticalView, productModel.isMismatched, lblWeight == 1.0f));
        listTxtData.setLayoutManager(new LinearLayoutManager(context));
      }
    }
  }
  
  public void setFragmentButton(CommonFragment fragment, int btnTextRes){
    setFragmentButton(fragment, context.getString(btnTextRes));
  }
  
  public void setFragmentButton(CommonFragment fragment, String btnText){
    if(fragment != null && isNonEmpty(btnText) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_CHECK_AVAILABILITY_BTN, isShowCheckAvailabilityBtnForProductDetails)){
      if(isVerticalView){
        btnSimilarProducts.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
        int margin = getResources().getDimensionPixelSize(R.dimen.dp_10);
        btnSimilarProducts.setPadding(margin, 0, margin, 0);
      }
      btnSimilarProducts.setText(HtmlCompat.fromHtml(btnText, HtmlCompat.FROM_HTML_MODE_LEGACY));
      btnSimilarProducts.setVisibility(VISIBLE);
      btnSimilarProducts.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v){
          
          if(v.getVisibility() == View.VISIBLE && fragment != null){
            final boolean isShowFIFOChart = SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_CHART);
            if(fragment instanceof ActionMenuCompareFragment){
              ((ActionMenuCompareFragment) fragment).callSimilarStyles(productModel);
            }
            else if(fragment instanceof ActionMenuSearchFragment){
              ((ActionMenuSearchFragment) fragment).callStyleChartAPI(isShowFIFOChart);
            }
            else if(fragment instanceof ProductDetailsFragment){
              ((ProductDetailsFragment) fragment).callStyleChartAPI(isShowFIFOChart);
            }
            else if(fragment instanceof ProductSearchDetailsFragment){
              ((ProductSearchDetailsFragment) fragment).callStyleChartAPI(isShowFIFOChart);
            }
            else if(fragment instanceof SearchFIFOStartFragment){
              ((SearchFIFOStartFragment) fragment).checkDecodeQtyForCallingStyleChart(isShowFIFOChart);
            }
          }
        }
      });
    }
    else btnSimilarProducts.setVisibility(GONE);
  }
  
  public void showProductDetailsAlert(final List<String> listLabels, final List<List<LabelValues>> listDetails){
    productDetailsAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    ((CommonActivity) context).setAlertDialogCustomTitle(productDetailsAlert, String.format(context.getString(R.string.title__details), "Product"));
    final DialogProductDetailsBinding dialogBinding = DialogProductDetailsBinding.inflate(LayoutInflater.from(context));
    final LinearLayout llLbls = dialogBinding.llProductDetailsLabels;
    final RecyclerView lvProdDetailsVals = dialogBinding.listProductDetailsValues;
    setupLayout(llLbls, listLabels);
    lvProdDetailsVals.setAdapter(new ProductDetailsListAdapter(context, listDetails));
    lvProdDetailsVals.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    productDetailsAlert.setView(dialogBinding.getRoot());
    productDetailsAlert.setCancelable(true);
    productDetailsAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        productDetailsAlert.dismiss();
      }
    });
    productDetailsAlert.show();
  }
  
  public void dismissAlerts(){
    if(isShowingDetailAlert()){
      productDetailsAlert.dismiss();
      productDetailsAlert = null;
    }
  }
  
  public boolean isShowingDetailAlert(){
    return productDetailsAlert != null && productDetailsAlert.isShowing();
  }
  
  private void setupLayout(final LinearLayout ll, final List<String> list){
    ll.removeAllViews();
    int margin = context.getResources().getDimensionPixelSize(R.dimen.dp_5);
    LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //llParams.setMargins(margin,margin,margin,margin);
    for(String lv : list){
      TextView txtVal = new TextView(context);
      txtVal.setTextAppearance(context, R.style.TextStyleSubHeader);
      txtVal.setBackgroundResource(R.drawable.border_bottom);
      txtVal.setPadding(margin, margin, margin, margin);
      txtVal.setText(lv);
      txtVal.setLayoutParams(llParams);
      txtVal.setMaxLines(1);
      txtVal.setSingleLine(true);
      txtVal.setEllipsize(TextUtils.TruncateAt.MARQUEE);
      txtVal.setMarqueeRepeatLimit(-1);
      txtVal.setSelected(true);
      ll.addView(txtVal);
    }
  }
  
}

package com.itek.retail.ui.search.productsearch;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDemoApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.FIFOChartListAdapter;
import com.itek.retail.adapter.ProductColorsListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.InsertDBFIFOs;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.DialogSimilarStylesBinding;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentProductDetailsBinding;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * The Product details fragment.
 */
public class ProductDetailsFragment extends CommonFragment{
  
  private ProductSearchDetailsViewModel mViewModel;
  private FragmentProductDetailsBinding binding;
  private ProductModel model;
  private AlertDialog styleChartAlert;
  private ProductDao productDao;
  
  /**
   * Instantiates a new Product details fragment.
   */
  public ProductDetailsFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ProductSearchDetailsViewModel.class);
    binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
    final Object obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    if(model == null){ popBackStack(); }
    binding.pdvProdSearch.setProductModel(model, chkNull(model.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
    binding.pdvProdSearch.setFragmentButton(this, R.string.tab_check_availability);
    
    binding.btnSizeChart.setOnClickListener(view -> {
      callStyleChartAPI(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_CHART));
    });
    
    return binding.getRoot();
  }
  
  public void callStyleChartAPI(final boolean isShowFIFOChart){
    if(styleChartAlert == null || !styleChartAlert.isShowing()){
      if(isShowFIFOChart){
        context.showCustomAlertSelectionDialog("Choose Option", "", getString(R.string.tab_size_chart), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            callStyleChartAPI(false);
          }
        }, getString(R.string.tab_fifo_products), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            try{
              JSONObject jsonRequest = new JSONObject();
              jsonRequest.put(ParamConstants.EAN, model.getEan());
              callWebService(URLConstants.GET_FIFO_SEARCH_LIST, jsonRequest, getString(R.string.progress_msg_getting_data));
              
            }
            catch(Exception e){ e.printStackTrace(); }
            //showFIFOChartAlert();
          }
        });
      }
      else{
        try{
          JSONObject jsonRequest = new JSONObject();
          jsonRequest.put(ParamConstants.EAN, model.getEan());
          callWebService(URLConstants.GET_SIZE_CHART, jsonRequest, getString(R.string.progress_msg_getting_data));
          
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
  }
  
  /**
   * Set product.
   *
   * @param args the args
   */
  public void setProduct(Bundle args){
    final Object obj = extractSerializable(args, ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    if(model == null){ popBackStack(); }
    productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
    binding.pdvProdSearch.setProductModel(model);
  }
  
  /**
   * Get model product model.
   *
   * @return the product model
   */
  public ProductModel getModel(){
    return model;
  }
  
  /**
   * Show style chart alert.
   */
  public void showStyleChartAlert(){
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, R.string.tab_size_chart);
    final DialogSizeChartBinding dialogBinding = DialogSizeChartBinding.inflate(LayoutInflater.from(context));
    final RecyclerView lvProdColors = dialogBinding.listDialogSizeChartColors;
    final RecyclerView lvProdSizes = dialogBinding.listDialogSizeChart;
    final List<String> listColors = productDao.getColorList();
    if(isNonEmpty(listColors) && listColors.contains(model.getColor()) && listColors.indexOf(model.getColor()) >= 0){
      listColors.remove(model.getColor());
      listColors.add(0, model.getColor());
    }
    lvProdColors.setAdapter(new ProductColorsListAdapter((MainActivity) context, ProductDetailsFragment.this, styleChartAlert, lvProdSizes, listColors, model));
    lvProdColors.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    styleChartAlert.setView(dialogBinding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  /**
   * Show fifo style chart alert.
   */
  public void showFIFOChartAlert(){
    showFIFOChartAlert(AppDatabase.getFIFODao(context).getDatewiseList(model.ean));
  }
  
  public void showFIFOChartAlert(final List<FIFOModel> listFIFO){
    showLog(ProductDetailsFragment.class.getSimpleName() + "_listFIFO", "" + listFIFO.size());
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, R.string.tab_fifo_products);
    final DialogSimilarStylesBinding binding = DialogSimilarStylesBinding.inflate(LayoutInflater.from(context));
    binding.listDialogSimilarStyles.setAdapter(new FIFOChartListAdapter((MainActivity) context, ProductDetailsFragment.this, styleChartAlert, listFIFO, model));
    binding.listDialogSimilarStyles.setLayoutManager(new LinearLayoutManager(context));
    binding.listDialogSimilarStyles.setVisibility(View.VISIBLE);
    styleChartAlert.setView(binding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  @Override
  public void onBackPressed(){
    productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
    super.onBackPressed();
  }
  
  @Override
  public void onDestroy(){
    if(productDao != null) productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
    super.onDestroy();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_SIZE_CHART:
          productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
          if(isDemoApp && isStaticDebug(context)) isSuccess = false;
          if(isSuccess && jsonResponse != null){
            final JSONArray jsonArraySizeChartProducts = extractJSONArray(jsonResponse, ParamConstants.SIZE_CHART_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(jsonArraySizeChartProducts != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.SEARCH.getValue(), jsonRequest, jsonResponse, args, jsonArraySizeChartProducts);
            /*else if(isNonEmpty(jsonArraySizeChartProducts)){
              for(int i = 0; i < jsonArraySizeChartProducts.length(); i++){
                final JSONObject product = jsonArraySizeChartProducts.getJSONObject(i);
                final ProductModel productModel = getGSON().fromJson(product.toString(), ProductModel.class);
                if(productModel != null){
                  productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
                  productModel.setItemImgUrl(extractString(product, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(product, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, "");
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, 0);
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        productDao.insert(productModel);
                      }
                    }
                  }
                  else productDao.insert(productModel);
                }
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
          }
          //else context.showCustomErrDialog(R.string.err_no_data);
          if(chkNull(productDao.getAllTotal(), 0) > 0) showStyleChartAlert();
          break;
        case URLConstants.GET_FIFO_SEARCH_LIST:
          if(isSuccess){
            try{
              final String ean = extractString(jsonRequest, ParamConstants.EAN, "");
              final FIFODao fifoDao = AppDatabase.getFIFODao(context);
              JSONArray jsonFIFOProducts = extractJSONArray(jsonResponse, ParamConstants.PRODUCTS);
              if(jsonFIFOProducts != null){
                new InsertDBFIFOs((MainActivity) context, this, AppCommonMethods.SessionType.SEARCH_FIFO.getValue(), url, jsonRequest, jsonResponse, args).execute(jsonFIFOProducts);
              }
              /*else if(isNonEmpty(jsonFIFOProducts)){
                ArrayList<FIFOModel> list = new ArrayList<FIFOModel>(0);
                //AppDatabase.getRIFDSessionDao(context).deleteAll(AppCommonMethods.SessionType.SEARCH_FIFO.getValue());
                fifoDao.deleteAll();
                for(int i = 0; i < jsonFIFOProducts.length(); i++){
                  showLog("FIFOProducts[" + i + "]", jsonFIFOProducts.getJSONObject(i).toString());
                  final FIFOModel fifoModel = getGSON().fromJson(jsonFIFOProducts.getJSONObject(i).toString(), FIFOModel.class);
                  if(fifoModel != null && isNonEmpty(fifoModel.ean) && isNonEmpty(fifoModel.epc) && isNonEmpty(fifoModel.fifoDate) && Integer.parseInt(chkZero(fifoModel.zoneId, "0")) > 0){
                    fifoModel.setFifoDate(fifoModel.getFifoDate());
                    list.add(fifoModel);
                  }
                }
                if(list.size() > 0){
                  fifoDao.insertAll(list);
                  showFIFOChartAlert(fifoDao.getDatewiseList(ean));
                }
              }*/
            }
            catch(Exception e){ e.printStackTrace(); }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}
package com.itek.retail.common

import android.os.Bundle
import com.google.gson.Gson
import com.itek.retail.R
import com.itek.retail.apis.ParamConstants
import com.itek.retail.apis.URLConstants
import com.itek.retail.common.AppCommonMethods.SessionType
import com.itek.retail.common.AppCommonMethods.allowBtnClick
import com.itek.retail.common.AppCommonMethods.chkNull
import com.itek.retail.common.AppCommonMethods.chkZero
import com.itek.retail.common.AppCommonMethods.extractBoolean
import com.itek.retail.common.AppCommonMethods.extractJSONArray
import com.itek.retail.common.AppCommonMethods.extractString
import com.itek.retail.common.AppCommonMethods.isNonEmpty
import com.itek.retail.common.AppCommonMethods.isNullOrEmpty
import com.itek.retail.common.AppCommonMethods.isStaticDebug
import com.itek.retail.common.AppCommonMethods.showLog
import com.itek.retail.common.AppCommonMethods.toUnderScoreCase
import com.itek.retail.database.AppDatabase
import com.itek.retail.database.BrandDao
import com.itek.retail.database.BrandEanDao
import com.itek.retail.database.BrandWiseZoneInventoryDao
import com.itek.retail.database.CategoryDao
import com.itek.retail.database.FIFODao
import com.itek.retail.database.HUDetailsDao
import com.itek.retail.database.HUStatusDao
import com.itek.retail.database.InventoryDao
import com.itek.retail.database.OutwardToteEansDao
import com.itek.retail.database.ProductDao
import com.itek.retail.database.ProductInvFilterDao
import com.itek.retail.database.ReplenishBatchDetailsDao
import com.itek.retail.database.SerialDetailsDao
import com.itek.retail.database.TripInventoryDao
import com.itek.retail.database.TripStatusDao
import com.itek.retail.database.UploadInventoryDao
import com.itek.retail.database.ZoneDao
import com.itek.retail.model.Brand
import com.itek.retail.model.BrandEans
import com.itek.retail.model.BrandWiseZoneInventory
import com.itek.retail.model.Category
import com.itek.retail.model.FIFOModel
import com.itek.retail.model.HUDetails
import com.itek.retail.model.HUStatus
import com.itek.retail.model.Inventory
import com.itek.retail.model.OutwardToteEans
import com.itek.retail.model.ProductInvFilterModel
import com.itek.retail.model.ProductModel
import com.itek.retail.model.ReplenishBatchDetails
import com.itek.retail.model.SerialDetails
import com.itek.retail.model.TripInventory
import com.itek.retail.model.TripStatus
import com.itek.retail.model.UploadInventory
import com.itek.retail.model.Zone
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment
import com.itek.retail.ui.inventory.InventoryBrandFragment
import com.itek.retail.ui.inventory.InventoryFilterFragment
import com.itek.retail.ui.inventory.InventoryMainFragment
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionMainFragment
import com.itek.retail.ui.inward.InwardMainFragment
import com.itek.retail.ui.inward1.HuProcessStartFragment
import com.itek.retail.ui.inward1.TripHUListFragment
import com.itek.retail.ui.inward1.TripListFragment
import com.itek.retail.ui.movement.replenishment.ReplenishmentListFragment
import com.itek.retail.ui.outward.huverification.OutwardHuDataFragment
import com.itek.retail.ui.outward.offrange.OffRangeMainFragment
import com.itek.retail.ui.outward.tote.OutwardToteMainFragment
import com.itek.retail.ui.replenishondemand.ReplenishmentArticleListFragment
import com.itek.retail.ui.search.fifo.SearchFIFOFragment
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment
import com.itek.retail.ui.search.listnewsearch.SearchListExcelFragment
import com.itek.retail.ui.search.listsearch.SearchListFragment
import com.itek.retail.ui.search.listsearch.SearchListStartFragment
import com.itek.retail.ui.search.omnichannel.OmniChannelListFragment
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class InsertDBask(context: CommonActivity) : CoroutineScope by MainScope() {
  var context: CommonActivity? = null
  var fragment: CommonFragment? = null
  var url = ""
  var errMsg = ""
  var activeUsers: Int = -2
  var args: Bundle? = null

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment? = null,
    url: String,
    args: Bundle? = null,
    jsonResponse: JSONObject? = null,
    activeUsers: Int = -2,
    errMsg: String = "",
  ) :
    this(context) {
    this.context = context;
    this.fragment = fragment;
    this.url = url
    this.activeUsers = if (jsonResponse != null) AppCommonMethods.extractInt(
      jsonResponse,
      ParamConstants.ACTIVE_USERS,
      -2
    ) else activeUsers;
    this.errMsg = if (jsonResponse != null) extractString(
      jsonResponse,
      ParamConstants.ERR_MSG,
      extractString(
        jsonResponse,
        ParamConstants.ERROR,
        ""
      )
    ) else errMsg;
    this.args = args
  }

  final fun executeMultiListThread(jsonArrayOriginal: JSONArray): Boolean {
    if (isNonEmpty(jsonArrayOriginal)) {
      var multiLists: ArrayList<ArrayList<Any>> = getMultiLists(jsonArrayOriginal);
      val es1: ExecutorService = Executors.newFixedThreadPool(multiLists.size)
      for (i in 0..(multiLists.size - 1)) {
        es1.execute(java.lang.Runnable {
          doTask(multiLists.get(i), i)
        })
      }
      es1.shutdown();
      return es1.awaitTermination(3, TimeUnit.MINUTES)
    } else return false;
  }

  final fun getMultiLists(jsonArrayOriginal: JSONArray): ArrayList<ArrayList<Any>> {
    val size: Int = 8;
    val lists: ArrayList<ArrayList<Any>> = ArrayList(0);

    for (index in 0..(jsonArrayOriginal.length() - 1)) {
      if (index < size && lists.size < (index + 1))
        lists.add(index, ArrayList<Any>(0))
      lists.get(index % size).add(jsonArrayOriginal.get(index))
    }
    showLog("lists", lists.toString());
    return lists;
  }

  abstract fun onPostExecute(result: Boolean)

  abstract fun execute(jsonArray: JSONArray)

  abstract suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean

  abstract fun doTask(list: MutableList<Any>, i: Int)
}

class InsertDBBrandsCategories : InsertDBask {
  var type = ""
  var sessionValidTill: Int = -1
  var target: Int = -1
  var categoryDao: CategoryDao? = null
  var brandDao: BrandDao? = null

  internal constructor(
    context: CommonActivity,
    url: String,
    type: String,
    activeUsers: Int,
    sessionValidTill: Int,
    target: Int,
    errMsg: String,
    args: Bundle?
  ) : super(context, null, url, args, null, activeUsers, errMsg) {
    this.type = type
    this.sessionValidTill = sessionValidTill
    this.target = target
  }

  override fun execute(jsonArray: JSONArray) {
    brandDao = if (url.equals(
        URLConstants.GET_BRANDS,
        ignoreCase = true
      )
    ) AppDatabase.getBrandDao(context) else null

    categoryDao =
      if (url.equals(URLConstants.GET_CATEGORIES, ignoreCase = true)) AppDatabase.getCategoryDao(
        context
      ) else null

    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItems: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      executeMultiListThread(jsonArrayItems)
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (AppCommonMethods.isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      try {
        for (i in 0 until list.size) {
          try {
            val json = list.get(i)
            if (json != null) {
              var brand: Brand? = null
              var category: Category? = null
              if (json is JSONObject) {
                if (url.equals(URLConstants.GET_BRANDS, ignoreCase = true))
                  brand =
                    AppCommonMethods.getGSON().fromJson<Brand>(json.toString(), Brand::class.java)
                else if (url.equals(URLConstants.GET_CATEGORIES, ignoreCase = true))
                  category = AppCommonMethods.getGSON()
                    .fromJson<Category>(json.toString(), Category::class.java)
              } else if (json is String) {
                val name = json.toString().trim { it <= ' ' }
                if (AppCommonMethods.isNonEmpty(name) && url.equals(
                    URLConstants.GET_BRANDS,
                    ignoreCase = true
                  )
                )
                  brand = Brand("" + (i + 1), name.trim { it <= ' ' })
                if (AppCommonMethods.isNonEmpty(name) && url.equals(
                    URLConstants.GET_CATEGORIES,
                    ignoreCase = true
                  )
                )
                  category = Category("" + (i + 1), name.trim { it <= ' ' })
              }
              if (brand != null && brand is Brand && brandDao != null) brandDao!!.insert(brand)
              if (category != null && category is Category && categoryDao != null) categoryDao!!.insert(
                category
              )
            }
          } catch (e: java.lang.Exception) {
            e.printStackTrace()
          }
        }
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) context?.loadSessionFragment(
      type,
      chkNull(activeUsers, -2),
      chkNull(sessionValidTill, -1),
      chkNull(target, -1),
      args
    ) else if (AppCommonMethods.isNonEmpty(errMsg))
      context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBZones : InsertDBask {
  var zoneDao: ZoneDao? = null

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment?,
    jsonResponse: JSONObject?,
    url: String,
    errMsg: String,
    args: Bundle?
  ) : super(context, fragment, url, args, jsonResponse, -2, errMsg) {

  }

  override fun execute(jsonArray: JSONArray) {
    zoneDao = if (url.equals(
        URLConstants.GET_ZONES,
        ignoreCase = true
      )
    ) AppDatabase.getZoneDao(context) else null
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItems: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      executeMultiListThread(jsonArrayItems)
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (AppCommonMethods.isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      try {
        for (i in 0 until list.size) {
          try {
            val json = list.get(i)
            if (json != null) {
              var zone: Zone? = null
              if (json is JSONObject) {
                if (url.equals(URLConstants.GET_ZONES, ignoreCase = true))
                  zone =
                    AppCommonMethods.getGSON().fromJson<Zone>(json.toString(), Zone::class.java)
              }
              if (zone != null && zone is Zone && zoneDao != null) zoneDao!!.insert(zone)
            }
          } catch (e: java.lang.Exception) {
            e.printStackTrace()
          }
        }
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result)
      context?.handlePostRedirection(url, fragment, args)
    else if (AppCommonMethods.isNonEmpty(errMsg))
      context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBProducts : InsertDBask {
  var sessionType = 0
  var sessionValidTill: Int = -1
  var target: Int = -1
  var productDao: ProductDao? = null
  var jsonRequest: JSONObject? = null
  var jsonResponse: JSONObject? = null
  var hasPickData: Boolean = false

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment?,
    url: String,
    sessionType: Int,
    jsonRequest: JSONObject?,
    jsonResponse: JSONObject,
    args: Bundle?,
  ) : super(context, fragment, url, args, jsonResponse) {
    this.sessionType = sessionType
    this.jsonRequest = jsonRequest
    this.jsonResponse = jsonResponse
  }

  internal constructor(
    context: CommonActivity,
    url: String,
    sessionType: Int,
    jsonResponse: JSONObject,
    args: Bundle?
  ) :
    super(context, null, url, args, jsonResponse) {
    this.sessionType = sessionType
    this.jsonResponse = jsonResponse
  }

  override fun execute(jsonArray: JSONArray) {
    showLog("Start execute:", "START");
    productDao = AppDatabase.getProductDao(context);
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      showLog("Start Loop:", "START");
      if (sessionType == 27) productDao!!.deleteAll() else productDao!!.deleteAllExcept()
      if (sessionType > 0) {
        if (sessionType == AppCommonMethods.SessionType.SEARCH_LIST.value) AppDatabase.getInventoryDao(
          context
        ).deleteAllExcept() else AppDatabase.getInventoryDao(context)
          .deleteInventory(sessionType)
      }

      val finished = executeMultiListThread(jsonArrayItemsOriginal)
      if (finished) {
        showLog("End Loop:", "end1");
        if (url.equals(
            URLConstants.GET_PICK_LIST,
            ignoreCase = true
          ) || url.equals(URLConstants.GET_OMNICHANNEL_LIST, ignoreCase = true) || url.equals(
            URLConstants.GET_OMNICHANNEL_LIST_DETAILS,
            ignoreCase = true
          )
        ) {
          for (ean in productDao!!.getDistinctEans(sessionType)) {
            productDao!!.updateTotalQty(ean, sessionType);
          }
        }
      }
      finished

    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      showLog("Thread Start", "THREAD STARTED : " + ix.toString() + "_" + list.size)
      var insertCount = 0
      var copyOflistOne = ArrayList<ProductModel>(0);
      if (AppCommonMethods.isNonEmpty(list)) {
        try {
          for (i in 0 until list.size) {
            val json = list.get(i)
            if (json != null && json is JSONObject) {
              var shortageProduct: JSONObject = json;
              val productModel: ProductModel? =
                if (shortageProduct != null) AppCommonMethods.getGSON().fromJson<ProductModel>(
                  shortageProduct.toString(),
                  ProductModel::class.java
                ) else null
              if (productModel != null) {
                productModel.setSessionType(sessionType)
                productModel.setItemImgUrl(
                  AppCommonMethods.extractString(
                    shortageProduct,
                    ParamConstants.IMG_URL,
                    ""
                  ).replace(AppConstants.IMAGE_URL_REPLACE_REGEX.toRegex(), "").trim { it <= ' ' })
                /*if(chkNull(productModel.qty,0)>0 && chkNull(productModel.totalQty,0)<=0)
                 productModel.totalQty=productModel.qty;*/
                val jsonZones =
                  AppCommonMethods.extractJSONArray(shortageProduct, ParamConstants.ZONES)
                var totalQty = 0
                if (AppCommonMethods.isNonEmpty(jsonZones)) {
                  for (j in 0 until jsonZones.length()) {
                    val zone = jsonZones.getJSONObject(j)
                    val zoneId = AppCommonMethods.extractString(zone, ParamConstants.ZONE_ID, "0");
                    val zoneName = AppCommonMethods.extractString(
                      zone,
                      ParamConstants.ZONE_NAME,
                      AppCommonMethods.extractString(
                        zone,
                        ParamConstants.ZONE,
                        if (!chkZero(zoneId, "0").equals("0")) AppDatabase.getZoneDao(context)
                          .getZoneNameById(zoneId) else ""
                      )
                    );
                    val eanQty = AppCommonMethods.extractInt(
                      zone,
                      ParamConstants.EAN_QTY,
                      AppCommonMethods.extractInt(zone, ParamConstants.QTY, 0)
                    );
                    if (AppCommonMethods.isNonEmpty(zoneName) && AppCommonMethods.chkNull(
                        eanQty,
                        0
                      ) > 0
                    ) {
                      val pm: ProductModel = ProductModel(productModel);
                      if (j > 0) pm.setQty(0);
                      pm.setZoneId(zoneId)
                      pm.setZone(zoneName)
                      pm.setEanQty(eanQty)
                      val jsonTags =
                        AppCommonMethods.extractJSONArray(
                          zone,
                          ParamConstants.PICKED_EPCS,
                          AppCommonMethods.extractJSONArray(
                            zone,
                            ParamConstants.EPCS
                          )
                        )
                      if (jsonTags != null && jsonTags.length() > 0) {
                        var epc = ""
                        var foundQty = 0
                        var decodedQty = 0
                        for (k in 0 until jsonTags.length()) {
                          val obj = jsonTags.get(k);
                          if (obj != null) {
                            val jsonStr = (if (obj is String) jsonTags.getString(k) else "").trim();
                            val jsonTag =
                              if (obj is JSONObject) jsonTags.getJSONObject(k) else null;
                            if (isNonEmpty(jsonStr)) {
                              foundQty += 1
                              epc += (if (AppCommonMethods.isNonEmpty(
                                  epc
                                )
                              ) "," else "") + jsonStr
                            } else if (isNonEmpty(jsonTag)) {
                              val isDecoded =
                                AppCommonMethods.extractBoolean(
                                  jsonTag,
                                  ParamConstants.IS_DECODED,
                                  false
                                )
                              decodedQty += if (isDecoded) 1 else 0
                              foundQty += 1
                              epc += (if (AppCommonMethods.isNonEmpty(
                                  epc
                                )
                              ) "," else "") + AppCommonMethods.extractString(
                                jsonTag,
                                ParamConstants.EPC
                              )
                            }
                          }
                        }
                        pm.setFoundQty(foundQty)
                        pm.found = foundQty > 0
                        pm.setDecodedQty(decodedQty)
                        pm.decoded = decodedQty > 0
                        pm.setEpc(epc)
                        if (!hasPickData) hasPickData = true;
                      }
                      //productDao!!.insert(productModel)
                      copyOflistOne.add(pm);
                      totalQty += AppCommonMethods.chkNull(eanQty, 0)
                      insertCount++
                    }
                  }
                  /*==if (url.equals(
                      URLConstants.GET_PICK_LIST,
                      ignoreCase = true
                    ) && insertCount > 0 && totalQty > 0
                  ) productDao!!.updateTotalQty(
                    productModel.ean,
                    totalQty,
                    productModel.getSessionType()
                  )*/
                  /*if (url.equals(
                      URLConstants.GET_OMNICHANNEL_LIST,
                      ignoreCase = true
                    ) && insertCount > 0 && totalQty > 0
                  ) productDao!!.updateTotalQty(
                    productModel.ean,
                    totalQty,
                    productModel.getSessionType()
                  )*/
                } else {
                  val jsonEPCs = AppCommonMethods.extractJSONArray(
                    shortageProduct,
                    ParamConstants.EPCS,
                    if (chkNull(productModel.epc, "").contains(",")) {
                      JSONArray(
                        "[\"" + productModel.epc.replace(
                          "(\"|\\[|\\]|,null|null,)",
                          ""
                        )
                          .replace(",", "\",\"") + "\"]"
                      )
                    } else null
                  );
                  if (AppCommonMethods.isNonEmpty(jsonEPCs)) {
                    for (j in 0 until jsonEPCs.length()) {
                      val epc = jsonEPCs.getString(j)
                      val pm: ProductModel = ProductModel(productModel)
                      pm.setEpc(epc)
                      copyOflistOne.add(pm)
                      //productDao!!.insert(productModel)
                      insertCount++
                    }
                  } else if (url.equals(URLConstants.GET_PICK_LIST, ignoreCase = true)) {
                    if (!AppCommonMethods.isUseNewUIForLBS) {
                      productModel.eanQty = productModel.qty
                      productModel.totalQty = productModel.qty
                      copyOflistOne.add(productModel)
                      //productDao!!.insert(productModel)
                      insertCount++
                    } else {
                      productModel.totalQty = productModel.eanQty;
                      copyOflistOne.add(productModel);
                      //productDao!!.insert(productModel)
                      insertCount++
                    }
                  } else {
                    copyOflistOne.add(productModel);
                    //productDao!!.insert(productModel)
                    insertCount++
                  }
                }
              }
            }
          }
          if (isNonEmpty(copyOflistOne)) productDao!!.insertAll(copyOflistOne.toList());
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      showLog("Thread END", "THREAD ENDED : " + ix.toString())
    }
  }

  override fun onPostExecute(result: Boolean) {
    showLog(this.javaClass.simpleName + "_" + url + "_Result", "" + result)
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      if (args == null) args = Bundle()
      args!!.putInt(AppConstants.ACTIVE_USERS, chkNull(activeUsers, 0))
      if (url.equals(
          URLConstants.GET_STOCK_CORRECTION_DASHBOARD,
          ignoreCase = true
        )
      ) context?.loadFragment(StockCorrectionMainFragment(), args)
      else if (url.equals(URLConstants.GET_EXCEL_SEARCH_LIST, ignoreCase = true)) {
        SharedPrefManager.setInt(
          ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW,
          AppCommonMethods.extractInt(
            jsonResponse,
            ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW,
            AppCommonMethods.markFoundPercentNewLBS
          )
        )
        val bundle = if (args != null) args!! else Bundle()
        bundle.putString(
          AppConstants.SEARCH_LIST_ID,
          extractString(
            jsonRequest,
            ParamConstants.SEARCH_LIST_ID,
            extractString(
              jsonResponse,
              ParamConstants.SEARCH_LIST_ID,
              extractString(
                jsonResponse,
                ParamConstants.SEARCH_LIST_NAME,
                extractString(
                  jsonResponse,
                  ParamConstants.NAME,
                  extractString(
                    jsonResponse,
                    ParamConstants.CODE,
                    extractString(jsonResponse, ParamConstants.ORDER_NO, "")
                  )
                )
              )
            )
          )
        )
        bundle.putString(
          AppConstants.SEARCH_LIST_TYPE,
          extractString(
            jsonRequest,
            ParamConstants.SEARCH_LIST_TYPE,
            extractString(
              jsonResponse,
              ParamConstants.SEARCH_LIST_TYPE,
              extractString(jsonResponse, ParamConstants.TYPE, "")
            )
          )
        )
        context?.loadFragment(SearchListExcelFragment(), bundle)
      } else if (url.equals(URLConstants.GET_PICK_LIST, ignoreCase = true)) {
        SharedPrefManager.setInt(
          ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED,
          AppCommonMethods.extractInt(
            jsonResponse,
            ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED,
            AppCommonMethods.markFoundPercentLBS
          )
        )
        val bundle = if (args != null) args!! else Bundle()
        bundle.putString(
          AppConstants.SEARCH_LIST_ID,
          extractString(
            jsonRequest,
            ParamConstants.SEARCH_LIST_ID,
            extractString(
              jsonResponse,
              ParamConstants.SEARCH_LIST_ID,
              extractString(
                jsonResponse,
                ParamConstants.SEARCH_LIST_NAME,
                extractString(
                  jsonResponse,
                  ParamConstants.NAME,
                  extractString(
                    jsonResponse,
                    ParamConstants.CODE,
                    extractString(jsonResponse, ParamConstants.ORDER_NO, "")
                  )
                )
              )
            )
          )
        )
        bundle.putString(
          AppConstants.SEARCH_LIST_TYPE,
          extractString(
            jsonRequest,
            ParamConstants.SEARCH_LIST_TYPE,
            extractString(
              jsonResponse,
              ParamConstants.SEARCH_LIST_TYPE,
              extractString(jsonResponse, ParamConstants.TYPE, "")
            )
          )
        )
        context?.loadFragment(SearchListFragment(), bundle)
      } else if (url.equals(URLConstants.GET_OFF_RANGE_PRODUCTS, ignoreCase = true)) {
        context?.loadFragment(OffRangeMainFragment(), args);
      } else if (url.equals(URLConstants.GET_REPLENISHMENT_LIST, ignoreCase = true)) {
        if (fragment != null && fragment is ReplenishmentListFragment && jsonRequest != null) (fragment as ReplenishmentListFragment).updateSelectedValuesOnSuccess(
          jsonRequest
        )
      } else if (url.equals(URLConstants.GET_OMNICHANNEL_LIST, ignoreCase = true)) {
        if (fragment != null && fragment is OmniChannelListFragment && jsonRequest != null) (fragment as OmniChannelListFragment).updateSelectedValuesOnSuccess(
          jsonRequest
        )
      } else if (url.equals(URLConstants.GET_OMNICHANNEL_LIST_DETAILS, ignoreCase = true)) {
        if (fragment != null && fragment is OmniChannelListFragment) (fragment as OmniChannelListFragment).handleOmniDetailsRedirection(
          args,
          hasPickData
        )
      } else if (url.equals(URLConstants.GET_SIZE_CHART, ignoreCase = true)) {
        if (fragment != null && fragment is ActionMenuSearchFragment) (fragment as ActionMenuSearchFragment).showStyleChartAlert()
        else if (fragment != null && fragment is ProductSearchDetailsFragment) (fragment as ProductSearchDetailsFragment).showStyleChartAlert()
        else if (fragment != null && fragment is ProductDetailsFragment) (fragment as ProductDetailsFragment).showStyleChartAlert()
        else if (fragment != null && fragment is SearchListStartFragment) (fragment as SearchListStartFragment).showStyleChartAlert()
        else if (fragment != null && fragment is SearchFIFOStartFragment) (fragment as SearchFIFOStartFragment).showStyleChartAlert()
      }
    } else if (AppCommonMethods.isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBProductInvFilter : InsertDBask {
  var sessionType = 0
  var zone: String = ""
  var zoneId: String =""
  var productInvFilterDao: ProductInvFilterDao? = null
  var jsonRequest: JSONObject? = null
  var jsonResponse: JSONObject? = null
  var keys: Iterator<String>?=null

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment?,
    url: String,
    jsonRequest: JSONObject?,
    jsonResponse: JSONObject,
    args: Bundle?,
    sessionType: Int,
    zone: String,
    zoneId: String,
  ) : super(context, fragment, url, args, jsonResponse) {
    this.sessionType = sessionType
    this.zone = zone
    this.zoneId = zoneId
    this.jsonRequest = jsonRequest
    this.jsonResponse = jsonResponse
  }

  internal constructor(
    context: CommonActivity,
    url: String,
    sessionType: Int,
    jsonResponse: JSONObject,
    args: Bundle?
  ) :
    super(context, null, url, args, jsonResponse) {
    this.sessionType = sessionType
    this.jsonResponse = jsonResponse
  }

  override fun execute(jsonArray: JSONArray) {
    showLog("Start execute:", "START");
    if(isNullOrEmpty(jsonArray) || jsonArray.get(0)==null || !(jsonArray.get(0) is JSONObject)) return;
    keys=jsonArray.getJSONObject(0).keys()
    productInvFilterDao = AppDatabase.getProductInvFilterDao(context);
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      showLog("Start Loop:", "START");
      productInvFilterDao!!.deleteAll()
      val finished = executeMultiListThread(jsonArrayItemsOriginal)
      finished
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      showLog("Thread Start", "THREAD STARTED : " + ix.toString() + "_" + list.size)
      var insertCount = 0
      var copyOflistOne = ArrayList<ProductInvFilterModel>(0);
      try {
        for (i in 0 until list.size) {
          val json = list.get(i)
          if (json != null && json is JSONObject) {
            var filterProduct: JSONObject = json;
            val productInvFilterModel: ProductInvFilterModel? =
              if (filterProduct != null) AppCommonMethods.getGSON().fromJson<ProductInvFilterModel>(
                filterProduct.toString(),
                ProductInvFilterModel::class.java
              ) else null
            if (productInvFilterModel != null) {
              productInvFilterModel.setZone(zone)
              productInvFilterModel.setZoneId(zoneId)
              productInvFilterModel.setSessionType(sessionType)
              copyOflistOne.add(productInvFilterModel);
              insertCount++
            }
          }
        }
        if (isNonEmpty(copyOflistOne)) productInvFilterDao!!.insertAll(copyOflistOne.toList());
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    showLog("Thread END", "THREAD ENDED : " + ix.toString())
  }

  override fun onPostExecute(result: Boolean) {
    showLog(this.javaClass.simpleName + "_" + url + "_Result", "" + result)
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      try{
        val jsResult  = JSONObject()
        jsResult.put(ParamConstants.IS_MULTI_SELECT, extractBoolean(jsonResponse,ParamConstants.IS_MULTI_SELECT,false))
        jsResult.put(ParamConstants.IS_CASCADE, extractBoolean(jsonResponse,ParamConstants.IS_CASCADE,false))
        val jFilters  = JSONObject()
        while (keys!!.hasNext()) {
          val key = keys!!.next()
          if(isNonEmpty(key) && !key.matches(Regex("(?i)(ean|qty)")))
            jFilters.put(key,JSONArray(productInvFilterDao?.getList(zone, toUnderScoreCase(key),null)))
        }
        jsResult.put(ParamConstants.FILTERS,jFilters)
        var fragment = this.fragment as InventoryFilterFragment;
        fragment?.onDBFilterSaved(jsResult)
      }
      catch (ex:Exception){ex.printStackTrace()}

    } else if (AppCommonMethods.isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBTrips : InsertDBask {
  var type = ""
  var tripInventoryDao: TripInventoryDao? = null
  var tripStatusDao: TripStatusDao? = null

  internal constructor(
    context: CommonActivity,
    url: String,
    type: String,
    jsonResponse: JSONObject?,
    args: Bundle?
  ) : super(context, null, url, args, jsonResponse) {
    this.type = type
  }

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment,
    url: String,
    type: String,
    jsonResponse: JSONObject?,
    args: Bundle?
  ) : super(context, fragment, url, args, jsonResponse) {
    this.type = type
  }

  override fun execute(jsonArray: JSONArray) {
    tripStatusDao = AppDatabase.getTripStatusDao(context)
    tripInventoryDao = AppDatabase.getTripInventoryDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean {
    return async(Dispatchers.IO) {

      tripStatusDao!!.deleteAllTripStatus(type)
      tripInventoryDao!!.deleteAllTripInventory()

      val finished = executeMultiListThread(jsonArrayItemsOriginal);

      if (finished) {
        showLog("End Loop:", "end1");
      }
      finished
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      var tripStatusCount = 0
      var tripInvCount = 0
      var copyOftripStatus = ArrayList<TripStatus>(0);
      var copyOftripInv = ArrayList<TripInventory>(0);
      for (i in 0 until list.size) {
        try {
          val trip = list.get(i)
          if (trip != null && trip is JSONObject) {
            val tripStatus = Gson().fromJson(
              trip.toString(),
              TripStatus::class.java
            )
            if (tripStatus != null) {
              if (isNullOrEmpty(tripStatus.tripNumber) && isNonEmpty(tripStatus.refTripNumber)) {
                tripStatus.setTripNumber(tripStatus.refTripNumber);
              } else if (isNullOrEmpty(tripStatus.refTripNumber) && isNonEmpty(tripStatus.tripNumber)) {
                tripStatus.setRefTripNumber("");
              }
              if (isNullOrEmpty(tripStatus.type)) tripStatus.setType(type)
              if (isNullOrEmpty(tripStatus.tripType)) {
                tripStatus.tripType = "";
                val srcLocType = chkNull(
                  tripStatus.srcLocType,
                  if (type.equals(
                      AppConstants.OUTWARD,
                      false
                    )
                  ) SharedPrefManager.getStoreType() else ""
                );
                val destLocType = chkNull(
                  tripStatus.destLocType,
                  if (type.equals(
                      AppConstants.INWARD,
                      false
                    )
                  ) SharedPrefManager.getStoreType() else ""
                );
                if (isNonEmpty(srcLocType) && isNonEmpty(destLocType))
                  tripStatus.tripType = (srcLocType + " To " + destLocType).trim();
              }
              if (tripStatus.getCompletedHu() > 0) tripStatus.status =
                AppConstants.TRIP_STATUS_IN_PROGRESS
              val tripDtlsArray =
                AppCommonMethods.extractJSONArray(
                  trip,
                  ParamConstants.K_HU_DATA
                )
              if (isNonEmpty(tripDtlsArray)) {
                for (j in 0 until tripDtlsArray.length()) {
                  val tripInventory = Gson().fromJson(
                    tripDtlsArray.getJSONObject(j).toString(),
                    TripInventory::class.java
                  )
                  if (tripInventory != null) {
                    //tripInventoryDao!!.insertTripInventoryData(tripInventory)
                    copyOftripInv.add(tripInventory);
                    tripInvCount++
                  }
                }
              }
              //tripStatusDao!!.insertTripStatusData(tripStatus)
              copyOftripStatus.add(tripStatus);
              tripStatusCount++
            }
          }
        } catch (e: java.lang.Exception) {
          e.printStackTrace()
        }
      }
      if (isNonEmpty(copyOftripInv)) tripInventoryDao!!.insertAll(copyOftripInv);
      if (isNonEmpty(copyOftripStatus)) tripStatusDao!!.insertAll(copyOftripStatus);
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      if (url.equals(URLConstants.GET_INWARD_TRIP_DATA, ignoreCase = true)) context?.loadFragment(
        InwardMainFragment()
      )
      else if (url.equals(
          URLConstants.GET_OUTWARD_TRIP_DATA,
          ignoreCase = true
        )
      ) context?.loadFragment(OutwardHuDataFragment())
      else if (url.equals(URLConstants.GET_TRIPS_DATA, ignoreCase = true)) {
        if (fragment != null && fragment is TripListFragment) (fragment as TripListFragment).initUI();
        else context?.loadFragment(TripListFragment(), args)
      }
    } else if (AppCommonMethods.isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBHUs : InsertDBask {
  var type = ""
  var tripNum = ""
  var huDetailsDao: HUDetailsDao? = null
  var huStatusDao: HUStatusDao? = null
  var tripInventoryDao: TripInventoryDao? = null
  var isShowDialog = false

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment,
    url: String,
    type: String,
    tripNum: String,
    jsonResponse: JSONObject?,
    args: Bundle?
  ) : super(context, fragment, url, args, jsonResponse) {
    this.type = type
    this.tripNum = tripNum
    this.isShowDialog = extractBoolean(args, ParamConstants.IS_SHOW_HU_INFO_DIALOG, false);
  }

  override fun execute(jsonArray: JSONArray) {
    huDetailsDao = AppDatabase.getHUDetailsDao(context)
    huStatusDao = AppDatabase.getHUStatusDao(context)
    tripInventoryDao = AppDatabase.getTripInventoryDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      huStatusDao!!.deleteAllTripHus(type, tripNum)
      huDetailsDao!!.deleteAllTripHus(type, tripNum)
      tripInventoryDao!!.deleteTripInventory(tripNum)
      val finished = executeMultiListThread(jsonArrayItemsOriginal);
      if (finished) {
        showLog("End Loop:", "end1");
      }
      finished
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      var huStatusCount = 0
      var huDetailsCount = 0
      var copyOfhuStatus = ArrayList<HUStatus>(0);
      var copyOfHUDetails = ArrayList<HUDetails>(0);
      for (i in 0 until list.size) {
        try {
          val tripHu = list.get(i)
          if (tripHu != null && tripHu is JSONObject) {
            val huStatus = Gson().fromJson(
              tripHu.toString(),
              HUStatus::class.java
            )
            if (huStatus != null) {
              if (isNullOrEmpty(huStatus.tripNumber)) huStatus.setTripNumber(tripNum)
              if (isNullOrEmpty(huStatus.type)) huStatus.setType(type)
              val huDtlsArray =
                AppCommonMethods.extractJSONArray(
                  tripHu,
                  ParamConstants.K_TRIP_HU_DETAILS
                )
              var expHuQty = 0;
              if (isNonEmpty(huDtlsArray)) {
                for (j in 0 until huDtlsArray.length()) {
                  val huDtls = huDtlsArray.getJSONObject(j);
                  val huDetails = Gson().fromJson(
                    huDtls.toString(),
                    HUDetails::class.java
                  )
                  if (huDetails != null) {
                    val skus = extractString(
                      huDtls,
                      ParamConstants.SKU_IDS,
                      extractString(
                        huDtls,
                        ParamConstants.SKUS,
                        extractString(huDtls, ParamConstants.EANS)
                      )
                    ).replace("(\"|\\[|\\]|,null|null,)".toRegex(), "").trim();
                    /* val rfids = extractString(
                       huDtls,
                       ParamConstants.RFIDS,
                       extractString(huDtls, ParamConstants.EPCS)
                     ).replace("(\"|\\[|\\]|,null|null,)".toRegex(), "").trim();
                     expHuQty += huDetails.expQty;
                     huDetails.tripNumber = huStatus.tripNumber;
                     huDetails.huNumber = huStatus.huNumber;
                     if (isNonEmpty(skus)) huDetails.ean = skus;
                     if (isNonEmpty(rfids)) huDetails.rfid = rfids;
                     //tripInventoryDao!!.insertTripInventoryData(tripInventory)
                     copyOfHUDetails.add(huDetails);
                     huDetailsCount++ */
                  }
                }
              }
              //huStatusDao!!.insertHUStatusData(huStatus)
              if (huStatus.getExpQty() <= 0) huStatus.setExpQty(expHuQty);
              copyOfhuStatus.add(huStatus);
              huStatusCount++
            }
          }
        } catch (e: java.lang.Exception) {
          e.printStackTrace()
        }
      }
      if (isNonEmpty(copyOfhuStatus)) huStatusDao!!.insertAll(copyOfhuStatus);
      if (isNonEmpty(copyOfHUDetails)) huDetailsDao!!.insertAll(copyOfHUDetails);
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      if (url.equals(URLConstants.GET_INWARD_TRIP_DATA, ignoreCase = true)) context?.loadFragment(
        InwardMainFragment()
      )
      else if (url.equals(
          URLConstants.GET_OUTWARD_TRIP_DATA,
          ignoreCase = true
        )
      ) context?.loadFragment(OutwardHuDataFragment())
      else if (url.equals(URLConstants.GET_HU_DATA, ignoreCase = true)) {
        if (fragment != null && fragment is TripHUListFragment) (fragment as TripHUListFragment).onPostData(
          args
        );
        else
          context?.loadFragment(TripHUListFragment(), args)
      }
    } else if (AppCommonMethods.isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBHUDetails : InsertDBask {
  var type = ""
  var tripNum = ""
  var huNum = ""
  var isShowDialog = false
  var huStatusDao: HUStatusDao? = null
  var huDetailsDao: HUDetailsDao? = null
  var tripInventoryDao: TripInventoryDao? = null
  var serialDetailsDao: SerialDetailsDao? = null
  var huStatusHasExpQty = false;

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment,
    url: String,
    type: String,
    tripNum: String,
    huNum: String,
    jsonResponse: JSONObject?,
    args: Bundle?
  ) : super(context, fragment, url, args, jsonResponse) {
    this.type = type
    this.tripNum = tripNum
    this.huNum = huNum
    this.isShowDialog = extractBoolean(args, ParamConstants.IS_SHOW_HU_INFO_DIALOG, false);
  }

  override fun execute(jsonArray: JSONArray) {
    huStatusDao = AppDatabase.getHUStatusDao(context)
    huDetailsDao = AppDatabase.getHUDetailsDao(context)
    tripInventoryDao = AppDatabase.getTripInventoryDao(context)
    serialDetailsDao = AppDatabase.getSerialDetailsDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    huStatusHasExpQty = huStatusDao!!.getHUData(type, tripNum, huNum).getExpQty() > 0;
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      huDetailsDao!!.deleteAllTripHus(type, tripNum, huNum)
      tripInventoryDao!!.deleteInventory(tripNum, huNum)
      //Check possible in future or new architecture:- clear 3rd table
      serialDetailsDao!!.deleteAllSerialDetails()
      val finished = executeMultiListThread(jsonArrayItemsOriginal);
      if (finished) {
        showLog("End Loop:", "end1");
      }
      finished
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      var huDetailsCount = 0
      var tripInvCount = 0
      var serialDetailsCount = 0
      var totalExpQty = 0
      var totalScanQty = 0
      var copyOfhuDetails = ArrayList<HUDetails>(0);
      var copyOftripInv = ArrayList<TripInventory>(0);
      var copyOfSerialDetails = ArrayList<SerialDetails>(0);
      for (i in 0 until list.size) {
        try {
          val tripHu = list.get(i)
          if (tripHu != null && tripHu is JSONObject) {
            val huDetails = Gson().fromJson(
              tripHu.toString(),
              HUDetails::class.java
            )
            val skus = extractString(
              tripHu,
              ParamConstants.SKU_IDS,
              extractString(
                tripHu,
                ParamConstants.SKUS,
                extractString(tripHu, ParamConstants.EANS, "")
              )
            ).replace("(\"|\\[|\\]|,null|null,)".toRegex(), "").trim();
            var rfids = extractString(
              tripHu,
              ParamConstants.RFIDS,
              extractString(tripHu, ParamConstants.EPCS, "")
            ).replace("(\"|\\[|\\]|,null|null,)".toRegex(), "").trim();
            //Check possible in future or new architecture:- If the serial is coming from response then set it to trip inventory 3rd table
            val rfids1 = extractJSONArray(tripHu, ParamConstants.RFIDS1)
            if (isNonEmpty(rfids1)) {
              val hasEpcs = isNonEmpty(rfids);
              for (i in 0 until rfids1.length()) {
                val epcSerial = rfids1.get(i)
                if (epcSerial != null && epcSerial is JSONObject) {
                  val epc = extractString(epcSerial, ParamConstants.EPC, "");
                  val serial = extractString(epcSerial, ParamConstants.SERIAL_NUMBER, "");
                  if (!hasEpcs) rfids += epc + ","
                  //Check possible in future or new architecture:- insert epc + ean + serial + trip + hu in 3rd table
                  val serialDetails = SerialDetails();
                  if (serialDetails != null) {
                    //Check possible in future or new architecture:- Get EAN from EPC
                    serialDetails.epc = epc;
                    serialDetails.ean = context!!.epcEncoderDecoder.getBarcodeFromEPC(epc);
                    serialDetails.serialNo = serial;
                    copyOfSerialDetails.add(serialDetails);
                    serialDetailsCount++
                  }
                }
              }
              if (rfids.endsWith(",")) rfids.substring(rfids.length - 1);
            }
            if (huDetails != null) {
              if (isNullOrEmpty(huDetails.type)) huDetails.setType(type)
              if (isNullOrEmpty(huDetails.tripNumber)) huDetails.setTripNumber(tripNum)
              if (isNullOrEmpty(huDetails.huNumber)) huDetails.setHuNumber(huNum)
              if (isNonEmpty(skus)) huDetails.setEan(skus)
              if (isNonEmpty(rfids)) huDetails.setRfid(rfids)
              copyOfhuDetails.add(huDetails);
              huDetailsCount++
              if (!huStatusHasExpQty) {
                huStatusDao!!.updateHUStatusQuantities(
                  huDetails.type,
                  huDetails.tripNumber,
                  huDetails.huNumber,
                  huDetails.expQty,
                  huDetails.scanQty
                )
              }
            }
            val tripInventory = if (isShowDialog) null else TripInventory();
            if (tripInventory != null) {
              tripInventory.tripNo = huDetails.tripNumber;
              tripInventory.huNo = huDetails.huNumber;
              tripInventory.articleCode = huDetails.articleCode;
              tripInventory.ean = huDetails.ean;
              tripInventory.eanQty = huDetails.expQty;
              tripInventory.rfid = huDetails.rfid;
              copyOftripInv.add(tripInventory);
              tripInvCount++
            }
          }
        } catch (e: java.lang.Exception) {
          e.printStackTrace()
        }
      }
      if (isNonEmpty(copyOfhuDetails)) huDetailsDao!!.insertAll(copyOfhuDetails);
      if (isNonEmpty(copyOftripInv)) tripInventoryDao!!.insertAll(copyOftripInv);
      //Check possible in future or new architecture:- insert list to 3rd table
      if (isNonEmpty(copyOfSerialDetails)) serialDetailsDao!!.insertAll(copyOfSerialDetails);
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    allowBtnClick = true
    if (result) {
      if (url.equals(URLConstants.GET_HU_DETAILS, ignoreCase = true)) {
        if (fragment != null && fragment is TripHUListFragment) (fragment as TripHUListFragment).onPostData(huNum, args);
        else context?.loadFragment(HuProcessStartFragment(), args)
      }
    } else if (isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBBrandZones : InsertDBask {
  //insert bulk data parallely on parallel threads.
  var brandWiseZoneInventoryDao: BrandWiseZoneInventoryDao? = null
  var jsonRequest: JSONObject? = null
  var jsonResponse: JSONObject? = null

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment,
    url: String,
    jsonRequest: JSONObject,
    jsonResponse: JSONObject,
    args: Bundle?
  ) : super(context, fragment, url, args, jsonResponse) {
    this.jsonRequest = jsonRequest
    this.jsonResponse = jsonResponse
  }

  override fun execute(jsonArray: JSONArray) {
    brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItems: JSONArray): Boolean {
    return async(Dispatchers.IO) {

      brandWiseZoneInventoryDao!!.deleteAll()

      val finished = executeMultiListThread(jsonArrayItems)
      if (finished) {
        showLog("End Loop:", "end1");
        //productDao!!.insertAll(copyOflist.toList());

        //2023-07-12 19:17:25.186 4506-4712/com.itek.retail E/THREAD1: 1
        //CHAINWAY C72e Android 8.1.0, API 27
      }
      finished
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      var insertCount = 0;
      var copyOflist = ArrayList<BrandWiseZoneInventory>(0);
      for (i in 0 until list.size) {
        try {
          val jobj = list.get(i)
          if (jobj != null && jobj is JSONObject) {
            val brandName = extractString(
              jobj,
              ParamConstants.BRAND_NAME,
              extractString(jobj, ParamConstants.BRAND, "")
            ).trim { it <= ' ' }
            val categoryName = extractString(
              jobj,
              ParamConstants.CATEGORY_NAME,
              extractString(jobj, ParamConstants.CATEGORY, "")
            ).trim { it <= ' ' }
            val zonewise =
              AppCommonMethods.extractJSONArray(jobj, ParamConstants.ZONES, JSONArray())
            val brands = AppCommonMethods.extractJSONArray(jobj, ParamConstants.BRANDS, JSONArray())
            val categories =
              AppCommonMethods.extractJSONArray(jobj, ParamConstants.CATEGORIES, JSONArray())
            if (isNonEmpty(zonewise)) {
              //showLog("zonewise", "" + zonewise.length())
              for (j in 0 until zonewise.length()) {
                val brandWiseZoneInventory: BrandWiseZoneInventory =
                  AppCommonMethods.getGSON()
                    .fromJson<BrandWiseZoneInventory>(
                      zonewise.getJSONObject(j).toString(),
                      BrandWiseZoneInventory::class.java
                    )
                if (brandWiseZoneInventory != null) {
                  brandWiseZoneInventory.brandName = brandName
                  copyOflist.add(brandWiseZoneInventory)
                  insertCount++
                }
              }
            } else if (isNonEmpty(brandName) && isNonEmpty(categories)) {
              //showLog("zonewise", "" + zonewise.length())
              for (j in 0 until categories.length()) {
                val json: JSONObject = categories.getJSONObject(j);
                val categoryName = extractString(
                  json,
                  ParamConstants.CATEGORY_NAME,
                  extractString(json, ParamConstants.CATEGORY, "")
                ).trim { it <= ' ' }
                val zonewise =
                  AppCommonMethods.extractJSONArray(json, ParamConstants.ZONES, JSONArray())
                if (isNonEmpty(zonewise)) {
                  //showLog("zonewise", "" + zonewise.length())
                  for (k in 0 until zonewise.length()) {
                    val brandWiseZoneInventory: BrandWiseZoneInventory =
                      AppCommonMethods.getGSON()
                        .fromJson<BrandWiseZoneInventory>(
                          zonewise.getJSONObject(k).toString(),
                          BrandWiseZoneInventory::class.java
                        )
                    if (brandWiseZoneInventory != null) {
                      brandWiseZoneInventory.brandName = brandName
                      brandWiseZoneInventory.categoryName = categoryName
                      copyOflist.add(brandWiseZoneInventory)
                      insertCount++
                    }
                  }
                } else {
                  val brandWiseZoneInventory: BrandWiseZoneInventory =
                    AppCommonMethods.getGSON()
                      .fromJson<BrandWiseZoneInventory>(
                        zonewise.getJSONObject(j).toString(),
                        BrandWiseZoneInventory::class.java
                      )
                  if (brandWiseZoneInventory != null) {
                    brandWiseZoneInventory.brandName = brandName
                    copyOflist.add(brandWiseZoneInventory)
                    insertCount++
                  }
                }
              }
            } else if (isNonEmpty(categoryName) && isNonEmpty(brands)) {
              for (j in 0 until brands.length()) {
                val json: JSONObject = brands.getJSONObject(j);
                val brandName = extractString(
                  json,
                  ParamConstants.BRAND_NAME,
                  extractString(json, ParamConstants.BRAND, "")
                ).trim { it <= ' ' }
                val zonewise =
                  AppCommonMethods.extractJSONArray(json, ParamConstants.ZONES, JSONArray())
                if (isNonEmpty(zonewise)) {
                  //showLog("zonewise", "" + zonewise.length())
                  for (k in 0 until zonewise.length()) {
                    val brandWiseZoneInventory: BrandWiseZoneInventory =
                      AppCommonMethods.getGSON()
                        .fromJson<BrandWiseZoneInventory>(
                          zonewise.getJSONObject(k).toString(),
                          BrandWiseZoneInventory::class.java
                        )
                    if (brandWiseZoneInventory != null) {
                      brandWiseZoneInventory.brandName = brandName
                      brandWiseZoneInventory.categoryName = categoryName
                      copyOflist.add(brandWiseZoneInventory)
                      insertCount++
                    }
                  }
                } else {
                  val brandWiseZoneInventory: BrandWiseZoneInventory =
                    AppCommonMethods.getGSON()
                      .fromJson<BrandWiseZoneInventory>(
                        json.toString(),
                        BrandWiseZoneInventory::class.java
                      )
                  if (brandWiseZoneInventory != null) {
                    brandWiseZoneInventory.brandName = brandName
                    brandWiseZoneInventory.categoryName = categoryName
                    copyOflist.add(brandWiseZoneInventory)
                    insertCount++
                  }
                }
              }
            } else {
              val brandWiseZoneInventory: BrandWiseZoneInventory =
                AppCommonMethods.getGSON()
                  .fromJson<BrandWiseZoneInventory>(
                    jobj.toString(),
                    BrandWiseZoneInventory::class.java
                  );
              copyOflist.add(brandWiseZoneInventory)
              insertCount++
            }
          }
        } catch (e: java.lang.Exception) {
        }
      }
      if (isNonEmpty(copyOflist)) brandWiseZoneInventoryDao!!.insertAll(copyOflist.toList());
      showLog("insertCount", "" + insertCount);
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      if (url.equals(URLConstants.GET_INVENTORY_DASHBOARD, ignoreCase = true) && fragment != null) {
        if (fragment != null && fragment is InventoryBrandFragment) (fragment as InventoryBrandFragment).updateInvCount()
        else if (fragment != null && fragment is InventoryMainFragment) (fragment as InventoryMainFragment).updateInvCount()
      }
    } else if (AppCommonMethods.isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }
}

class InsertDBEANs : InsertDBask {
  var action = ""
  var sessionId = ""
  var sessionTime = ""
  var selZone = ""
  var selZones: Set<String>? = null
  var inventoryCount: Long? = null
  var brandWiseZoneInventoryDao: BrandWiseZoneInventoryDao? = null
  var brandEanDao: BrandEanDao? = null

  internal constructor(
    context: CommonActivity,
    fragment: InventoryFilterFragment,
    url: String,
    action: String,
    sessionId: String,
    sessionTime: String,
    inventoryCount: Long,
  ) : super(context, fragment, url) {
    this.action = action
    this.sessionId = sessionId
    this.sessionTime = sessionTime
    this.inventoryCount = inventoryCount;
  }

  internal constructor(context: CommonActivity, fragment: InventoryFilterFragment, url: String) :
    super(context, fragment, url) {
    action = ""
    sessionId = ""
    sessionTime = ""
  }

  override fun execute(jsonArray: JSONArray) {
    var fragment = this.fragment as InventoryFilterFragment;
    selZone = fragment?.binding?.spinInventoryStartLocation?.getSelectedItem().toString()
    selZones = fragment?.binding?.spinInventoryStartLocation?.getSelectedVals() as Set<String>
    brandEanDao = AppDatabase.getBrandEansDao(context)
    brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonBrandEansOrignal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      showLog("jsonBrandEansOrignal doit", "" + jsonBrandEansOrignal);

      val finished = executeMultiListThread(jsonBrandEansOrignal)

      if (finished) {
        showLog("End Loop:", "end1");
        //productDao!!.insertAll(copyOflist.toList());
      }
      finished

    }.await() == true;
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    showLog("Thread Start", "THREAD STARTED : " + ix.toString())
    if (AppCommonMethods.isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      try {
        var copyList = ArrayList<BrandEans>(0);
        for (i in 0 until list.size) {
          val jsonBrandEan = list.get(i)
          if (jsonBrandEan != null && (jsonBrandEan is JSONObject || jsonBrandEan is String)) {
            val brandEan = (if (jsonBrandEan is JSONObject) extractString(
              jsonBrandEan,
              ParamConstants.EANS,
              ""
            ) else jsonBrandEan.toString())
              .replace("(\"|\\[|\\]|,null|null,)".toRegex(), "")
              .replace("\\s*,\\s*".toRegex(), ",")
              .trim { it <= ' ' }
            //final JSONArray brandEans = extractJSONArray(jsonBrandEan, ParamConstants.EANS, null);
            val invCount: Int = if (jsonBrandEan is JSONObject) AppCommonMethods.extractInt(
              jsonBrandEan,
              ParamConstants.TOTAL_QTY,
              if (inventoryCount != null && chkZero(
                  inventoryCount?.toInt(),
                  0
                ) >= 0
              ) inventoryCount?.toInt() else chkZero(
                brandWiseZoneInventoryDao?.getInvCount(
                  selZone,
                  selZones,
                  null
                )?.toInt(), 0
              )
            ) else 0
            if (isNonEmpty(brandEan)) {
              val brandWiseEans = BrandEans(
                "-",
                ",$brandEan,".replace(",,".toRegex(), ",").trim { it <= ' ' },
                invCount
              )
              if (brandWiseEans != null) {
                //brandEanDao?.insert(brandWiseEans)
                copyList.add(brandWiseEans);
              }
            }
            showLog("Thread END", "THREAD ENDED : " + i.toString())
          }
        }
        brandEanDao?.insertAll(copyList.toList());
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    var fragment = this.fragment as InventoryFilterFragment;
    fragment?.onPostExecute(url, action, sessionId, sessionTime, result)
  }
}

class InsertDBBrandEANs : InsertDBask {
  var action = ""
  var sessionId = ""
  var sessionTime = ""
  var selZone = ""
  var selBrand = ""
  var selZones: Set<String>? = null
  var selBrands: Set<String?>? = null
  var isSingleBrand = false
  var inventoryCount: Long? = null
  var brandWiseZoneInventoryDao: BrandWiseZoneInventoryDao? = null
  var brandEanDao: BrandEanDao? = null

  internal constructor(
    context: CommonActivity,
    fragment: InventoryBrandFragment,
    url: String,
    action: String,
    sessionId: String,
    sessionTime: String,
    inventoryCount: Long,
  ) : super(context, fragment, url) {
    this.action = action
    this.sessionId = sessionId
    this.sessionTime = sessionTime
    this.inventoryCount = inventoryCount;
  }

  internal constructor(context: CommonActivity, fragment: InventoryBrandFragment, url: String) :
    super(context, fragment, url) {
    action = ""
    sessionId = ""
    sessionTime = ""
  }

  override fun execute(jsonArray: JSONArray) {
    var fragment = this.fragment as InventoryBrandFragment;
    selZone = fragment?.binding?.spinInventoryStartLocation?.getSelectedItem().toString()
    selBrand = fragment?.binding?.spinInventoryStartBrand?.getSelectedItem().toString()
    selZones = fragment?.binding?.spinInventoryStartLocation?.getSelectedVals() as Set<String>
    selBrands = fragment?.binding?.spinInventoryStartBrand?.getSelectedVals() as Set<String>
    isSingleBrand = (selBrands as Set<String>).size == 1
    brandEanDao = AppDatabase.getBrandEansDao(context)
    brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonBrandEansOrignal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      showLog("jsonBrandEansOrignal doit", "" + jsonBrandEansOrignal);

      val finished = executeMultiListThread(jsonBrandEansOrignal)

      if (finished) {
        showLog("End Loop:", "end1");
        //productDao!!.insertAll(copyOflist.toList());
      }
      finished

    }.await() == true;
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    showLog("Thread Start", "THREAD STARTED : " + ix.toString())
    if (AppCommonMethods.isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      try {
        var copyList = ArrayList<BrandEans>(0);
        for (i in 0 until list.size) {
          val jsonBrandEan = list.get(i)
          if (jsonBrandEan != null && jsonBrandEan is JSONObject) {
            val brandName = extractString(
              jsonBrandEan,
              ParamConstants.BRAND_NAME,
              extractString(jsonBrandEan, ParamConstants.BRAND)
            ).trim { it <= ' ' }
            val brandEan = extractString(jsonBrandEan, ParamConstants.EANS, "")
              .replace("(\"|\\[|\\]|,null|null,)".toRegex(), "")
              .replace("\\s*,\\s*".toRegex(), ",")
              .trim { it <= ' ' }
            //final JSONArray brandEans = extractJSONArray(jsonBrandEan, ParamConstants.EANS, null);
            val invCount: Int = AppCommonMethods.extractInt(
              jsonBrandEan,
              ParamConstants.TOTAL_QTY,
              if (isSingleBrand && inventoryCount != null && chkZero(
                  inventoryCount?.toInt(),
                  0
                ) >= 0
              ) inventoryCount?.toInt() else chkZero(
                brandWiseZoneInventoryDao?.getInvCount(
                  selZone,
                  selZones,
                  brandName
                )?.toInt(), 0
              )
            )
            if (AppCommonMethods.isNonEmpty(brandEan) && (AppCommonMethods.isNullOrEmpty(
                selBrands
              ) || AppCommonMethods.isNullOrEmpty(
                brandName
              ) || selBrands?.contains(brandName) == true)
            ) {
              showLog(
                "brandEan",
                brandName + ": " + brandEan.split(",".toRegex()).toTypedArray().size
              )
              if (AppCommonMethods.isNonEmpty(brandEan)) {
                val brandWiseEans = BrandEans(
                  brandName,
                  ",$brandEan,".replace(",,".toRegex(), ",").trim { it <= ' ' },
                  invCount
                )
                if (brandWiseEans != null) {
                  //brandEanDao?.insert(brandWiseEans)
                  copyList.add(brandWiseEans);
                }
              }
            }
            showLog("Thread END", "THREAD ENDED : " + i.toString())
          }
        }
        brandEanDao?.insertAll(copyList.toList());
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    var fragment = this.fragment as InventoryBrandFragment;
    fragment?.onPostExecute(url, action, sessionId, sessionTime, result)
  }
}

class InsertDBOutwardToteTypeEANs : InsertDBask {
  var outwardToteEansDao: OutwardToteEansDao? = null
  var label: String? = "";

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment,
    url: String,
    label: String
  ) :
    super(context, fragment, url) {
    this.label = label;
  }

  override fun execute(jsonArray: JSONArray) {
    var fragment = this.fragment;
    outwardToteEansDao = AppDatabase.getOutwardToteEansDao(context)
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonBrandEansOrignal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      showLog("jsonBrandEansOrignal doit", "" + jsonBrandEansOrignal);
      val finished = executeMultiListThread(jsonBrandEansOrignal)
      if (finished) {
        showLog("End Loop:", "end1");
        //productDao!!.insertAll(copyOflist.toList());
      }
      finished
    }.await() == true;
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    showLog("Thread Start", "THREAD STARTED : " + ix.toString())
    if (isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      try {
        var copyList = ArrayList<OutwardToteEans>(0);
        for (i in 0 until list.size) {
          val jsonBrandEan = list.get(i)
          if (jsonBrandEan != null && jsonBrandEan is JSONObject) {
            val toteType = extractString(
              jsonBrandEan,
              ParamConstants.OUTWARD_TOTE_TYPE,
              extractString(jsonBrandEan, ParamConstants.TYPE)
            ).trim { it <= ' ' }
//            val brandEan = extractString(jsonBrandEan,ParamConstants.OUTWARD_TOTE_EANS,extractString(jsonBrandEan, ParamConstants.EANS, ""))
//              .replace("(\"|\\[|\\]|,null|null,)".toRegex(), "")
//              .replace("\\s*,\\s*".toRegex(), ",")
//              .trim { it <= ' ' }
            val brandEans: JSONArray = extractJSONArray(jsonBrandEan, ParamConstants.EANS, null);
//            val invCount: Int = AppCommonMethods.extractInt(
//              jsonBrandEan,
//              ParamConstants.TOTAL_QTY, 0
//            )

            if (AppCommonMethods.isNonEmpty(brandEans)) {
              for (i in 0 until brandEans.length()) {
                val jsonEan = brandEans.get(i)
                if (jsonEan != null && jsonEan is String) {
                  val toteEans = OutwardToteEans(toteType, jsonEan.trim());
                  if (toteEans != null) copyList.add(toteEans);
                } else if (jsonEan != null && jsonEan is JSONObject) {
                  val ean = extractString(
                    jsonEan,
                    ParamConstants.OUTWARD_TOTE_EAN,
                    extractString(jsonEan, ParamConstants.EAN)
                  );
                  val invCount: Int = AppCommonMethods.extractInt(
                    jsonBrandEan,
                    ParamConstants.TOTAL_QTY,
                    AppCommonMethods.extractInt(jsonBrandEan, ParamConstants.EAN_QTY, 0)
                  )
                  val toteEans = OutwardToteEans(toteType, ean.trim(), invCount);
                  if (toteEans != null) copyList.add(toteEans);
                }
//                if (toteEans != null) {
//                  //brandEanDao?.insert(brandWiseEans)
//                  copyList.add(toteEans);
//                }
              }
            }
            showLog("Thread END", "THREAD ENDED : " + i.toString())
          }
        }
        outwardToteEansDao?.insertAll(copyList.toList());
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    var fragment = this.fragment as OutwardToteMainFragment;
    allowBtnClick = true;
    fragment?.updateSpinnerAdapter(label)
  }
}

class InsertDBFIFOs : InsertDBask {
  var fifoDao: FIFODao? = null
  var jsonRequest: JSONObject? = null
  var jsonResponse: JSONObject? = null
  var sessionType: Int

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment?,
    sessionType: Int,
    url: String,
    jsonRequest: JSONObject,
    jsonResponse: JSONObject,
    args: Bundle?
  ) :
    super(context, fragment, url, args, jsonResponse) {
    this.jsonRequest = jsonRequest
    this.jsonResponse = jsonResponse
    this.sessionType = sessionType;
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      if (fragment != null && fragment is SearchFIFOFragment) {
        val ean = extractString(jsonRequest, ParamConstants.EAN, "")
        val isAllowDecode = AppCommonMethods.extractBoolean(
          jsonResponse,
          ParamConstants.IS_ALLOW_DECODE,
          false
        )
        val isAllowDecodeOnPick = isAllowDecode && AppCommonMethods.extractBoolean(
          jsonResponse,
          ParamConstants.IS_ALLOW_DECODE_ON_PICK,
          AppCommonMethods.isAllowDecodeOnPick
        )
        (fragment as SearchFIFOFragment).redirectToSearch(
          args,
          ean,
          isAllowDecode,
          isAllowDecodeOnPick,
          fifoDao!!.getOldestDateObj(ean)
        )
      } else if (fragment != null && fragment is ProductSearchDetailsFragment) (fragment as ProductSearchDetailsFragment).showFIFOChartAlert()
      else if (fragment != null && fragment is ProductDetailsFragment) (fragment as ProductDetailsFragment).showFIFOChartAlert()
      else if (fragment != null && fragment is SearchFIFOStartFragment) (fragment as SearchFIFOStartFragment).showFIFOChartAlert()
      else if (fragment != null && fragment is ActionMenuSearchFragment) (fragment as ActionMenuSearchFragment).showFIFOChartAlert()
    } else if (AppCommonMethods.isNonEmpty(errMsg)) context?.showCustomErrDialog(errMsg)
  }

  override fun execute(jsonArray: JSONArray) {
    fifoDao = AppDatabase.getFIFODao(context);
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItemsOriginal: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      showLog("Start Loop:", "START");
      fifoDao!!.deleteAll()
      if (sessionType > 0) AppDatabase.getInventoryDao(context).deleteInventory(sessionType)
      val finished = executeMultiListThread(jsonArrayItemsOriginal)
      if (finished) {
        showLog("End Loop:", "end1");
      }
      finished

    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (AppCommonMethods.isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      var copyOfList = ArrayList<FIFOModel>(0);
      try {
        val ean = extractString(jsonRequest, ParamConstants.EAN, "")
        for (i in 0 until list.size) {
          try {
            val fifo = list.get(i)
            if (fifo != null && fifo is JSONObject) {
              val fifoModel = Gson().fromJson(
                fifo.toString(),
                FIFOModel::class.java
              )
              if (fifoModel != null && isNonEmpty(fifoModel.ean) && isNonEmpty(fifoModel.epc) && isNonEmpty(
                  fifoModel.fifoDate
                ) && !fifoModel.fifoDate.trim { it <= ' ' }
                  .startsWith("0") && chkZero(fifoModel.zoneId, "0").toInt() > 0
              ) {
                if (isStaticDebug() && !fifoModel.ean.equals(ean)) fifoModel.ean = ean;
                fifoModel.setFifoDate(fifoModel.getFifoDate())
                copyOfList.add(fifoModel)
              }
            }
          } catch (e: java.lang.Exception) {
            e.printStackTrace()
          }
        }
        if (isNonEmpty(copyOfList)) fifoDao!!.insertAll(copyOfList.toList())
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }
}

class InsertDBReplenishBatchDetails : InsertDBask {
  var replenishBatchDetailsDao: ReplenishBatchDetailsDao? = null
  var batchId = ""

  internal constructor(
    context: CommonActivity,
    fragment: CommonFragment?,
    jsonResponse: JSONObject?,
    url: String,
    batchId: String,
    errMsg: String,
    args: Bundle?
  ) : super(context, fragment, url, args, jsonResponse, -2, errMsg) {
    this.batchId = batchId
  }

  override fun execute(jsonArray: JSONArray) {
    replenishBatchDetailsDao = if (url.equals(
        URLConstants.GET_REPLENISHMENT_BATCH_DETAILS,
        ignoreCase = true
      )
    ) AppDatabase.getReplenishBatchDetailsDao(context) else null
    context?.showProgressDialog(context?.getString(R.string.progress_msg_check_data))
    AppCommonMethods.allowBtnClick = false
    launch { onPostExecute(doInBackground(jsonArray)) }
  }

  override suspend fun doInBackground(jsonArrayItems: JSONArray): Boolean {
    return async(Dispatchers.IO) {
      replenishBatchDetailsDao!!.deleteAll()
      val finished = executeMultiListThread(jsonArrayItems)
      if (finished) {
        showLog("End Loop:", "end1");
      }
      finished
    }.await()
  }

  override fun doTask(list: MutableList<Any>, ix: Int) {
    if (AppCommonMethods.isNonEmpty(list)) {
      showLog("doTask", ix.toString() + "_" + list.size)
      var insertCount = 0
      var copyOflistOne = ArrayList<ReplenishBatchDetails>(0);
      if (AppCommonMethods.isNonEmpty(list)) {
        try {
          for (i in 0 until list.size) {
            val json = list.get(i)
            if (json != null && json is JSONObject) {
              var batchDetails: JSONObject = json;
              val replenishBatchDetails: ReplenishBatchDetails? =
                if (batchDetails != null) AppCommonMethods.getGSON()
                  .fromJson<ReplenishBatchDetails>(
                    batchDetails.toString(),
                    ReplenishBatchDetails::class.java
                  ) else null
              if (replenishBatchDetails != null) {
                if (isNullOrEmpty(replenishBatchDetails.getBatchId())) replenishBatchDetails.setBatchId(
                  batchId
                )
                val jsonArrayEanQty =
                  AppCommonMethods.extractJSONArray(batchDetails, ParamConstants.EANS)
                if (AppCommonMethods.isNonEmpty(jsonArrayEanQty)) {
                  for (j in 0 until jsonArrayEanQty.length()) {
                    val eanQty = jsonArrayEanQty.getJSONObject(j)
                    if (eanQty != null) {
                      val ean = extractString(eanQty, ParamConstants.EAN, "")
                      val eanPickQty = AppCommonMethods.extractInt(
                        eanQty,
                        ParamConstants.PICK_QTY,
                        AppCommonMethods.extractInt(
                          eanQty,
                          ParamConstants.PICKED_QTY,
                          AppCommonMethods.extractInt(eanQty, ParamConstants.QTY, 0)
                        )
                      )
                      if (isNonEmpty(ean)) {
                        val rbd =
                          ReplenishBatchDetails(replenishBatchDetails) //AppCommonMethods.deepClone(replenishBatchDetails);
                        rbd.setEan(ean)
                        rbd.setEanPickQty(eanPickQty)
                        copyOflistOne.add(rbd)
                      }
                    }
                  }
                } else {
                  copyOflistOne.add(replenishBatchDetails);
                  insertCount++
                }
              }
            }
          }
          if (isNonEmpty(copyOflistOne)) replenishBatchDetailsDao!!.insertAll(copyOflistOne.toList());
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      showLog("Thread END", "THREAD ENDED : " + ix.toString())
    }
  }

  override fun onPostExecute(result: Boolean) {
    context?.hideProgressDialog()
    AppCommonMethods.allowBtnClick = true
    if (result) {
      if (url.equals(
          URLConstants.GET_REPLENISHMENT_BATCH_DETAILS,
          ignoreCase = true
        )
      ) {
        if (fragment is ReplenishmentArticleListFragment) (fragment as ReplenishmentArticleListFragment)?.postAPIResult()
        else context?.handlePostRedirection(url, fragment, args)
      }
    } else if (AppCommonMethods.isNonEmpty(errMsg))
      context?.showCustomErrDialog(errMsg)
  }
}

class UpdateWrittenFoundTagsTask(context: CommonActivity) : CoroutineScope by MainScope() {
  var context: CommonActivity? = null
  var sessionType: AppCommonMethods.SessionType = SessionType.OTHER
  var sessionId: String = ""
  var epc: String = ""
  var tid: String = ""
  var fifoDate: String? = null
  var extras: String? = null
  var inventoryDao: InventoryDao? = null
  var uploadInventoryDao: UploadInventoryDao? = null

  internal constructor(
    context: CommonActivity,
    sessionType: AppCommonMethods.SessionType,
    sessionId: String,
    epc: String,
    tid: String,
    fifoDate: String?,
    extras: String?
  ) : this(context) {
    this.context = context
    this.sessionType = sessionType
    this.sessionId = sessionId
    this.epc = epc
    this.tid = tid
    this.fifoDate = fifoDate
    this.extras = extras
    if (context != null && !context.isFinishing) {
      inventoryDao = AppDatabase.getInventoryDao(context)
      uploadInventoryDao = AppDatabase.getUploadInventoryDao(context)
    }
  }


  suspend fun doInBackground(): Boolean {
    if (inventoryDao == null || uploadInventoryDao == null) return false;
    if (!(sessionType == SessionType.ENCODING || sessionType == SessionType.ENCODING_THAN || sessionType == SessionType.DECODING || sessionType == SessionType.OMNICHANNEL || sessionType == SessionType.SEARCH_LIST || sessionType == SessionType.SEARCH_FIFO)) return false;
    val tagInfo: Inventory? =
      inventoryDao!!.getInventoryByEpcAndTid(sessionType.getValue(), epc, tid)
    if (tagInfo == null || tagInfo.isUploaded) return false
    //Update in DB for decode if the scanned Tag is already encoded/decoded with expected epc & tid
    var result: Boolean = false;
    try {
      tagInfo.insertTime =
        SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(Date(System.currentTimeMillis()))
      tagInfo.writeFailReason = null
      tagInfo.isUploaded = false
      inventoryDao!!.updateInventoryData(tagInfo)
      try {
        uploadInventoryDao!!.insertUploadInventoryData(UploadInventory(tagInfo, extras))
        result = true
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return false
      }
      if (!(sessionType == SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)) return result;
      try {
        val productDao = AppDatabase.getProductDao(context)
        val fifoDao = AppDatabase.getFIFODao(context)
        if (productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)) productDao.updateDecodedEPC(
          tagInfo.epc,
          tagInfo.ean,
          tagInfo.zone
        )
        else if (fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) fifoDao.updateDecoded(
          tagInfo.ean,
          tagInfo.epc,
          fifoDate
        )
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
      return result;
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      return false
    }
  }

  fun onPostExecute(result: Boolean) {
    if (result) showLog(
      "UpdatedWrittenFoundTag",
      "sessionType: " + sessionType + ",tid: " + tid + ",epc: " + epc
    )
  }

  fun execute() {
    launch { onPostExecute(doInBackground()) }
  }
}

package com.example.androidwidgetapp.googleBilling

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.android.billingclient.api.queryProductDetails
import com.example.androidwidgetapp.databinding.ActivityGoogleBillingBinding
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleBillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleBillingBinding

    private val skuTypeMonthly = "shadhin_monthly_2022"
    private val skuTypeAnnual = "shadhin_annual"

    private lateinit var billingClient: BillingClient

    private lateinit var productDetails: ProductDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            Log.e("TEST", "billingResult : $billingResult,   purchases : $purchases")
        }


        billingClient = BillingClient
            .newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i("TEST", "Billing client successfully set up")
                    queryOneTimeProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.i("TEST", "Billing service disconnected")
            }
        })

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(skuTypeMonthly)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()))
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->

            productDetails = productDetailsList.first()
            // check billingResult
            // process returned productDetailsList
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken("")
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()



        binding.googleBtn.setOnClickListener {
            // Launch the billing flow
             billingClient.launchBillingFlow(this, billingFlowParams)
        }
    }

    private fun queryOneTimeProducts() {
        val skuListToQuery = ArrayList<String>()

        skuListToQuery.add(skuTypeMonthly)
        // ‘coins_5’ is the product ID that was set in the Play Console.
        // Here is where we can add more product IDs to query for based on
        //   what was set up in the Play Console.

        val params = SkuDetailsParams.newBuilder()
        params
            .setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.INAPP)
        // SkuType.INAPP refers to 'managed products' or one time purchases.
        // To query for subscription products, you would use SkuType.SUBS.

        billingClient.querySkuDetailsAsync(params.build())
        { result, skuDetails ->
            Log.i("TEST", "onSkuDetailsResponse ${result.responseCode}")
            if (skuDetails != null) {
                for (skuDetail in skuDetails) {
                    Log.i("TEST", skuDetail.toString())
                }
            } else {
                Log.i("TEST", "No skus found from query")
            }
        }
    }

    suspend fun processPurchases() {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("product_id_example")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        // Process the result.
    }

//    private fun initializeBillingConnection() {
//
//        billingClient?.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    // The BillingClient is ready. You can query purchases here.
//                    productInfo()
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }

//    private fun productInfo() {
//
//        val products = ArrayList<QueryProductDetailsParams.Product>()
//        products.add(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(skuTypeMonthly)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build(),
//        )
//        products.add(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(skuTypeAnnual)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build(),
//        )
//
//        val queryProductDetailsParams= QueryProductDetailsParams.newBuilder()
//                .setProductList(products)
//                .build()
//
//        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
//            // check billingResult
//            // process returned productDetailsList
//        }
//    }

//    private suspend fun productInfoExtensionFun() {
//        val products = ArrayList<QueryProductDetailsParams.Product>()
//        products.add(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(skuTypeMonthly)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build(),
//        )
//        products.add(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(skuTypeAnnual)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build(),
//        )
//
//        val queryProductDetailsParams= QueryProductDetailsParams.newBuilder()
//            .setProductList(products)
//            .build()
//
//        val productDetailsResult = withContext(Dispatchers.IO) {
//            billingClient?.queryProductDetails(queryProductDetailsParams)
//        }
//    }

//    private fun purchaseFlow(activity: Activity, product: ProductDetails, selectedOfferToken: String) {
//        val productDetailsParamsList = listOf(
//            BillingFlowParams.ProductDetailsParams.newBuilder()
//                .setProductDetails(product)
//                .setOfferToken(selectedOfferToken)
//                .build()
//        )
//
//        val billingFlowParams = BillingFlowParams.newBuilder()
//            .setProductDetailsParamsList(productDetailsParamsList)
//            .build()
//
//        // Launch the billing flow
//        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
//    }


}

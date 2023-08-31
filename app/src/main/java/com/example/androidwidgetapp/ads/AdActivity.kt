package com.example.androidwidgetapp.ads

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.androidwidgetapp.databinding.ActivityAdBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdBinding
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "AdActivity"
    lateinit var adRequest : AdRequest
    lateinit var adLoader: AdLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdBinding.inflate(layoutInflater)
        setContentView(binding.root)




        MobileAds.initialize(this) {}
        //addTestDeviceId()

        initClick()
    }

    private fun addTestDeviceId() {
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(
            listOf(
                "6DC9062A205771E36F91A03DEDC3D1D6",
                "16BA09A6038CD5475886D3F43E381347",
                "11AD95B86DAA27616E55E22BE16BF0F1"
            )
        ).build()
        MobileAds.setRequestConfiguration(configuration)
    }

    private fun initInterstitialAd() {
        binding.progressBar.isVisible = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad loaded")
                binding.progressBar.isVisible = false
                mInterstitialAd = interstitialAd
                mInterstitialAd?.show(this@AdActivity)
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }

    private fun initClick() {
        binding.bannerBtn.setOnClickListener {
            binding.progressBar.isVisible = true
            adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    binding.progressBar.isVisible = false
                }
            }
        }

        binding.interstitialBtn.setOnClickListener {
            initInterstitialAd()
        }

        binding.nativeBtn.setOnClickListener {
            initNativeAd()
        }
    }

    private fun initNativeAd() {
        binding.progressBar.isVisible = true
        adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd {
                binding.progressBar.isVisible = false
                binding.nativeAd.setNativeAd(it)
                Log.e("","")
            }.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    public override fun onPause() {
        super.onPause()
        binding.adView.pause()
    }
    public override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }
    public override fun onDestroy() {
        super.onDestroy()
        binding.adView.destroy()
    }
}
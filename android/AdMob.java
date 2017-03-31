
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;

import com.google.firebase.FirebaseApp;

import com.godot.game.BuildConfig;
import com.godot.game.R;

import org.godotengine.godot.Godot;
import org.godotengine.godot.Utils;

import org.json.JSONObject;
import org.json.JSONException;

public class AdMob {

	public static AdMob getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new AdMob(p_activity);
		}

		return mInstance;
	}

	public AdMob(Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;

		JSONObject config = FireBase.getConfig().optJSONObject("Ads");

		if (config.optBoolean("BannerAd", false)) { createBanner(config); }
		if (config.optBoolean("InterstitialAd", false)) { createInterstitial(config); }
	}

	public void createBanner(JSONObject config) {
		FrameLayout layout = ((Godot)activity).layout; // Getting Godots framelayout
		FrameLayout.LayoutParams AdParams = new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.MATCH_PARENT,
							FrameLayout.LayoutParams.WRAP_CONTENT);

		if(mAdView != null) { layout.removeView(mAdView); }

		if (config.optString("BannetGravity", "BOTTOM").equals("BOTTOM")) {
			AdParams.gravity = Gravity.BOTTOM;
		} else { AdParams.gravity = Gravity.TOP; }

		AdRequest.Builder adRequestB = new AdRequest.Builder();
		adRequestB.tagForChildDirectedTreatment(true);

		if (BuildConfig.DEBUG) {
			adRequestB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRequestB.addTestDevice(Utils.getDeviceId(activity));
		}

		AdRequest adRequest = adRequestB.build();

		String ad_unit_id = config.optString("BannerAdId", "");

		if (ad_unit_id.length() <= 0) {
			Log.d(TAG, "AdMob:Banner:UnitId:NotProvided");
			ad_unit_id = activity.getString(R.string.banner_ad_unit_id);
		}

		mAdView	= new AdView(activity);
		mAdView.setBackgroundColor(Color.TRANSPARENT);
		mAdView.setAdUnitId(ad_unit_id);
		mAdView.setAdSize(AdSize.SMART_BANNER);

		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Log.d(TAG, "AdMob:Banner:OnAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				Log.w(TAG, "AdMob:Banner:onAdFailedToLoad:" + errorCode);
			}
		});

		mAdView.setVisibility(View.INVISIBLE);
		mAdView.loadAd(adRequest);

		layout.addView(mAdView, AdParams);
	}

	public void createInterstitial(JSONObject config) {
		String ad_unit_id = config.optString("InterstitialAdId", "");

		if (ad_unit_id.length() <= 0) {
			Log.d(TAG, "AdMob:Interstitial:UnitId:NotProvided");
			ad_unit_id = activity.getString(R.string.interstitial_ad_unit_id);
		}

		mInterstitialAd = new InterstitialAd(activity);
		mInterstitialAd.setAdUnitId(ad_unit_id);
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Log.d(TAG, "AdMob:Interstitial:OnAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				Log.w(TAG, "AdMob:Interstitial:onAdFailedToLoad:" + errorCode);
			}

			@Override
			public void onAdClosed() {
				Log.w(TAG, "AdMob:Interstitial:onAdClosed");
				requestNewInterstitial();
			}
		});

		requestNewInterstitial();
	}

	public void show_banner_ad(final boolean show) {
		if (!isInitialized()) { return; }

		// Show Ad Banner here

		if (show) {
			if (mAdView.isEnabled()) { mAdView.setEnabled(true); }
			if (mAdView.getVisibility() == View.INVISIBLE) {
				Log.d(TAG, "AdMob:Visiblity:On");
				mAdView.setVisibility(View.VISIBLE);
			}
		} else {
			if (mAdView.isEnabled()) { mAdView.setEnabled(false); }
			if (mAdView.getVisibility() != View.INVISIBLE) {
				Log.d(TAG, "AdMob:Visiblity:Off");
				mAdView.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void show_interstitial_ad() {
		if (!isInitialized()) { return; }

		// Show interstitial ad

		if (mInterstitialAd.isLoaded()) { mInterstitialAd.show(); }
		else { Log.d(TAG, "AdMob:Interstitial:NotLoaded"); }
	}

	private void requestNewInterstitial() {
		AdRequest adRequest = new AdRequest.Builder()
		.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		.addTestDevice(Utils.getDeviceId(activity))

		.build();

		mInterstitialAd.loadAd(adRequest);
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Log.d(TAG, "AdMob is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	public void onStart() {

	}

	public void onPause() {
		if (mAdView != null) { mAdView.pause(); }
	}

	public void onResume() {
		if (mAdView != null) { mAdView.resume(); }

	}

	public void onStop() {
		if (mAdView != null) { mAdView.destroy(); }
	}

	private static Activity activity = null;
	private static AdMob mInstance = null;

	private AdView mAdView = null;
	private InterstitialAd mInterstitialAd = null;

	private FirebaseApp mFirebaseApp = null;

	private static final String TAG = "FireBase";
}

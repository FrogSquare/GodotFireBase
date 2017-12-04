/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.reward.RewardItem;

import com.google.firebase.FirebaseApp;

import com.godot.game.BuildConfig;
import com.godot.game.R;

import org.godotengine.godot.Godot;
import org.godotengine.godot.Utils;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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

		AdMobConfig = FireBase.getConfig().optJSONObject("Ads");
		MobileAds.initialize(activity, AdMobConfig.optString("AppId"));

		if (AdMobConfig.optBoolean("BannerAd", false)) { createBanner(); }
		if (AdMobConfig.optBoolean("InterstitialAd", false)) { createInterstitial(); }
		if (AdMobConfig.optBoolean("RewardedVideoAd", false)) {
			reward_ads = new HashMap<String, RewardedVideoAd>();

			String ad_unit_id = AdMobConfig.optString("RewardedVideoAdId", "");
			List<String> ad_units = new ArrayList<String>();

			if (ad_unit_id.length() <= 0) {
				Utils.d("AdMob:RewardedVideo:UnitId:NotProvided");
				ad_units.add(activity.getString(R.string.rewarded_video_ad_unit_id));
			} else {
				ad_units = Arrays.asList(ad_unit_id.split(","));
				Utils.d("AdMob:RewardedVideo:"+ String.valueOf(ad_units.size()) +":UnitIdS:Found");
			}

			for (String id : ad_units) {
				RewardedVideoAd mrv = createRewardedVideo(id);
				requestNewRewardedVideo(mrv, id);

				reward_ads.put(id, mrv);
			}
		}
	}

	public void setBannerUnitId(final String id) {
		createBanner(id);
	}

	public void createBanner() {
		if (AdMobConfig == null) { return; }

		String ad_unit_id = AdMobConfig.optString("BannerAdId", "");

		if (ad_unit_id.length() <= 0) {
			Utils.d("AdMob:Banner:UnitId:NotProvided");
			ad_unit_id = activity.getString(R.string.banner_ad_unit_id);
		}

		createBanner(ad_unit_id);
	}

	public void createBanner(final String ad_unit_id) {
		FrameLayout layout = ((Godot)activity).layout; // Getting Godots framelayout
		FrameLayout.LayoutParams AdParams = new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.MATCH_PARENT,
							FrameLayout.LayoutParams.WRAP_CONTENT);

		if(mAdView != null) { layout.removeView(mAdView); }

		if (AdMobConfig.optString("BannetGravity", "BOTTOM").equals("BOTTOM")) {
			AdParams.gravity = Gravity.BOTTOM;
		} else { AdParams.gravity = Gravity.TOP; }

		AdRequest.Builder adRequestB = new AdRequest.Builder();
		adRequestB.tagForChildDirectedTreatment(true);

		if (BuildConfig.DEBUG) {
			adRequestB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRequestB.addTestDevice(Utils.getDeviceId(activity));
		}

		AdRequest adRequest = adRequestB.build();

		mAdView	= new AdView(activity);
		mAdView.setBackgroundColor(Color.TRANSPARENT);
		mAdView.setAdUnitId(ad_unit_id);
		mAdView.setAdSize(AdSize.SMART_BANNER);

		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Utils.d("AdMob:Banner:OnAdLoaded");	
				Utils.callScriptFunc("AdMob", "AdMob_Banner", "loaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				Utils.w("AdMob:Banner:onAdFailedToLoad:ErrorCode:" + errorCode);
				Utils.callScriptFunc("AdMob", "AdMob_Banner", "load_failed");
			}
		});

		mAdView.setVisibility(View.INVISIBLE);
		mAdView.loadAd(adRequest);

		layout.addView(mAdView, AdParams);
	}

	public void createInterstitial() {
		if (AdMobConfig == null) { return; }

		String ad_unit_id = AdMobConfig.optString("InterstitialAdId", "");

		if (ad_unit_id.length() <= 0) {
			Utils.d("AdMob:Interstitial:UnitId:NotProvided");
			ad_unit_id = activity.getString(R.string.interstitial_ad_unit_id);
		}

		mInterstitialAd = new InterstitialAd(activity);
		mInterstitialAd.setAdUnitId(ad_unit_id);
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Utils.d("AdMob:Interstitial:OnAdLoaded");
				Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "loaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				Utils.w("AdMob:Interstitial:onAdFailedToLoad:" + errorCode);
				Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "load_failed");
			}

			@Override
			public void onAdClosed() {
				Utils.w("AdMob:Interstitial:onAdClosed");
				Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "closed");
				requestNewInterstitial();
			}
		});

		requestNewInterstitial();
	}

	public void emitRewardedVideoStatus(final String unitid) {
		RewardedVideoAd mrv = reward_ads.get(unitid);
		Utils.callScriptFunc("AdMob", "AdMob_Video",
		buildStatus(unitid, mrv.isLoaded() ? "loaded" : "not_loaded"));
	}

	public Dictionary buildStatus(String unitid, String status) {
		Dictionary dict = new Dictionary();
		dict.put("unit_id", unitid);
		dict.put("status", status);

		return dict;
	}

	public void emitRewardedVideoStatus() {
		RewardedVideoAd mrv = (RewardedVideoAd) reward_ads.values().toArray()[0];
		String id = (String) reward_ads.keySet().toArray()[0];

		Utils.callScriptFunc("AdMob", "AdMob_Video",
		buildStatus(id, mrv.isLoaded() ? "loaded" : "not_loaded"));
	}

	public RewardedVideoAd createRewardedVideo(final String unitid) {
		RewardedVideoAd mrv = MobileAds.getRewardedVideoAdInstance(activity);
		mrv.setRewardedVideoAdListener(new RewardedVideoAdListener() {

			@Override
			public void onRewardedVideoAdLoaded() {
				Utils.d("AdMob:Video:Loaded");
				Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "loaded"));
			}

			@Override
			public void onRewarded(RewardItem rewardItem) {
				Utils.d("AdMob:Rewarded:Success");

				Dictionary ret = new Dictionary();
				ret.put("RewardType", rewardItem.getType());
				ret.put("RewardAmount", rewardItem.getAmount());
				ret.put("unit_id", unitid);

				Utils.callScriptFunc("AdMob", "AdMobReward", ret);
				reloadRewardedVideo(unitid);
			}

			@Override
			public void onRewardedVideoAdFailedToLoad(int errorCode) {
				Utils.d("AdMob:VideoLoad:Failed");
				Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "load_failed"));
				reloadRewardedVideo(unitid);
			}

			@Override
			public void onRewardedVideoAdClosed() {
				Utils.d("AdMob:VideoAd:Closed");
				Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "closed"));
				reloadRewardedVideo(unitid);
			}

			@Override
			public void onRewardedVideoAdLeftApplication() {
				Utils.d("AdMob:VideoAd:LeftApp");
			}

			@Override
			public void onRewardedVideoAdOpened() {
				Utils.d("AdMob:VideoAd:Opended");
				//Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "opened"));
			}

			@Override
			public void onRewardedVideoStarted() {
				Utils.d("Reward:VideoAd:Started");
				//Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "started"));
			}
		});

		return mrv;
	}

	public void requestRewardedVideoStatus() {
		emitRewardedVideoStatus();
	}

	public void requestRewardedVideoStatus(final String unit_id) {
		emitRewardedVideoStatus(unit_id);
	}

	public void show_rewarded_video(final String id) {
		if (!isInitialized() || reward_ads.size() <= 0) { return; }

		RewardedVideoAd mrv = (RewardedVideoAd) reward_ads.get(id);

		if (mrv.isLoaded()) { mrv.show(); }
		else { Utils.d("AdMob:RewardedVideo:NotLoaded"); }
	}

	public void show_rewarded_video() {
		if (!isInitialized() || reward_ads.size() <= 0) { return; }

		RewardedVideoAd mrv = (RewardedVideoAd) reward_ads.values().toArray()[0];

		if (mrv.isLoaded()) { mrv.show(); }
		else { Utils.d("AdMob:RewardedVideo:NotLoaded"); }
	}

	public void show_banner_ad(final boolean show) {
		if (!isInitialized() || mAdView == null) { return; }

		// Show Ad Banner here

		if (show) {
			if (mAdView.isEnabled()) { mAdView.setEnabled(true); }
			if (mAdView.getVisibility() == View.INVISIBLE) {
				Utils.d("AdMob:Visiblity:On");
				mAdView.setVisibility(View.VISIBLE);
			}
		} else {
			if (mAdView.isEnabled()) { mAdView.setEnabled(false); }
			if (mAdView.getVisibility() != View.INVISIBLE) {
				Utils.d("AdMob:Visiblity:Off");
				mAdView.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void show_interstitial_ad() {
		if (!isInitialized() || mInterstitialAd == null) { return; }

		// Show interstitial ad

		if (mInterstitialAd.isLoaded()) { mInterstitialAd.show(); }
		else { Utils.d("AdMob:Interstitial:NotLoaded"); }
	}

	private void reloadRewardedVideo(final String unitid) {
		RewardedVideoAd mrv = reward_ads.get(unitid);
		requestNewRewardedVideo(mrv, unitid);
	}

	private void requestNewRewardedVideo(RewardedVideoAd mrv, String unitid) {
		Utils.d("AdMob:Loading:RewardedAd:For: "+unitid);
		AdRequest.Builder adRB = new AdRequest.Builder();

		if (BuildConfig.DEBUG) {
			adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRB.addTestDevice(Utils.getDeviceId(activity));
		}

		mrv.loadAd(unitid, adRB.build());
	}

	private void requestNewInterstitial() {
		AdRequest.Builder adRB = new AdRequest.Builder();

		if (BuildConfig.DEBUG) {
			adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRB.addTestDevice(Utils.getDeviceId(activity));
		}

		AdRequest adRequest = adRB.build();

		mInterstitialAd.loadAd(adRequest);
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Utils.d("AdMob:NotInitialized.");
			return false;
		} else {
			return true;
		}
	}

	public void onStart() {

	}

	public void onPause() {
		if (mAdView != null) { mAdView.pause(); }

		for (Map.Entry<String, RewardedVideoAd> entry : reward_ads.entrySet()) {
			entry.getValue().pause(activity);
		}
	}

	public void onResume() {
		if (mAdView != null) { mAdView.resume(); }

		for (Map.Entry<String, RewardedVideoAd> entry : reward_ads.entrySet()) {
			entry.getValue().resume(activity);
		}
	}

	public void onStop() {
		if (mAdView != null) { mAdView.destroy(); }

		for (Map.Entry<String, RewardedVideoAd> entry : reward_ads.entrySet()) {
			entry.getValue().destroy(activity);
		}
	}

	private static Activity activity = null;
	private static AdMob mInstance = null;

	private Map<String, RewardedVideoAd> reward_ads;

	private AdView mAdView = null;
	private InterstitialAd mInterstitialAd = null;

	private FirebaseApp mFirebaseApp = null;

	private JSONObject AdMobConfig = null;
}

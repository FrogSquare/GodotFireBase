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
import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;

import com.google.firebase.FirebaseApp;

import com.godot.game.BuildConfig;
import com.godot.game.R;

import org.godotengine.godot.Godot;
import org.godotengine.godot.Utils;
import org.godotengine.godot.Dictionary;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
//import java.util.Dictionary;

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
			String ad_unit_id = AdMobConfig.optString("RewardedVideoAdId", "");
			List<String> ad_units = new ArrayList<String>();

			if (ad_unit_id.length() <= 0) {
				Utils.d("GodotFireBase", "AdMob:RewardedVideo:UnitId:NotProvided:AddingDefault");
				ad_units.add(activity.getString(R.string.rewarded_video_ad_unit_id));
			}

            reward_ads = new HashMap<String, RewardedAd>();
			ad_units = Arrays.asList(ad_unit_id.split(","));
		    for (String unit_id : ad_units) {
			    reward_ads.put(unit_id, createRewardedVideo(unit_id));
            }
		}

		mAdSize = new Dictionary();
		mAdSize.put("width", 0);
		mAdSize.put("height", 0);

        onStart();
	}

	public Dictionary getBannerSize() {
		if ((int)mAdSize.get("width") == 0 || (int)mAdSize.get("height") == 0) {
			Utils.d("GodotFireBase", "AdView::Not::Loaded::Yet");
		}

		return mAdSize;
	}

	public void setBannerUnitId(final String id) {
		createBanner(id);
	}

	public void createBanner() {
		if (AdMobConfig == null) { return; }

		String ad_unit_id = AdMobConfig.optString("BannerAdId", "");

		if (ad_unit_id.length() <= 0) {
			Utils.d("GodotFireBase", "AdMob:Banner:UnitId:NotProvided:AddingDefault");
			ad_unit_id = activity.getString(R.string.banner_ad_unit_id);
		}

		createBanner(ad_unit_id);
	}

	public void createBanner(final String ad_unit_id) {
        mAdViewLoaded = false;

		FrameLayout layout = ((Godot)activity).layout; // Getting Godots framelayout
		FrameLayout.LayoutParams AdParams = new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.MATCH_PARENT,
							FrameLayout.LayoutParams.WRAP_CONTENT);

		if(mAdView != null) { layout.removeView(mAdView); }

		if (AdMobConfig.optString("BannerGravity", "BOTTOM").equals("BOTTOM")) {
			AdParams.gravity = Gravity.BOTTOM;
		} else { AdParams.gravity = Gravity.TOP; }

		AdRequest.Builder adRequestB = new AdRequest.Builder();
		adRequestB.tagForChildDirectedTreatment(true);

		if (BuildConfig.DEBUG || AdMobConfig.optBoolean("TestAds", false)) {
			adRequestB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRequestB.addTestDevice(AdMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
		}

		AdRequest adRequest = adRequestB.build();

		mAdView	= new AdView(activity);
		mAdView.setBackgroundColor(Color.TRANSPARENT);
		mAdView.setAdUnitId(ad_unit_id);
		mAdView.setAdSize(AdSize.SMART_BANNER);

		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Utils.d("GodotFireBase", "AdMob:Banner:OnAdLoaded");
				AdSize adSize = mAdView.getAdSize();
                mAdViewLoaded = true;

				mAdSize.put("width", adSize.getWidthInPixels(activity));
				mAdSize.put("height", adSize.getHeightInPixels(activity));

				Utils.callScriptFunc("AdMob", "AdMob_Banner", "loaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				Utils.w("GodotFireBase", "AdMob:Banner:onAdFailedToLoad:ErrorCode:" + errorCode);
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
			Utils.d("GodotFireBase", "AdMob:Interstitial:UnitId:NotProvided:AddingDefault");
			ad_unit_id = activity.getString(R.string.interstitial_ad_unit_id);
		}

		mInterstitialAd = new InterstitialAd(activity);
		mInterstitialAd.setAdUnitId(ad_unit_id);
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Utils.d("GodotFireBase", "AdMob:Interstitial:OnAdLoaded");
				Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "loaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				Utils.w("GodotFireBase", "AdMob:Interstitial:onAdFailedToLoad:" + errorCode);
				Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "load_failed");
			}

			@Override
			public void onAdClosed() {
				Utils.w("GodotFireBase", "AdMob:Interstitial:onAdClosed");
				Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "closed");
				requestNewInterstitial();
			}
		});

		requestNewInterstitial();
	}

	public void emitRewardedVideoStatus() {
        for (String unit_id : reward_ads.keySet()) {

	        Utils.callScriptFunc("AdMob", "AdMob_Video",
            buildStatus(unit_id, reward_ads.get(unit_id).isLoaded() ? "loaded" : "not_loaded"));
        }
	}

    public Dictionary buildStatus(String unitid, String status) {
        Dictionary dict = new Dictionary();
        dict.put("unit_id", unitid);
        dict.put("status", status);
    
        return dict;
    }

	public RewardedAd createRewardedVideo(final String unitid) {
        RewardedAd rewardedAd = new RewardedAd(activity, unitid);
        requestNewRewardedVideo(rewardedAd, unitid);
		return rewardedAd;
	}

    public boolean isBannerLoaded() {
        return mAdViewLoaded;
    }

    public boolean isInterstitialLoaded() {
        if (mInterstitialAd != null) {
            return mInterstitialAd.isLoaded();
        }

        Utils.d("GodotFireBase", "Interstitial:NotInitialized");
        return false;
    }

    public boolean isRewardedAdLoaded(final String unit_id) {
		if (!isInitialized() || reward_ads == null) {
            return false;
		}

        return reward_ads.get(unit_id).isLoaded();
    }

	public void requestRewardedVideoStatus() {
		emitRewardedVideoStatus();
	}

	public void show_rewarded_video() {
		if (!isInitialized() || reward_ads == null) {
            Utils.d("GodotFireBase", "AdMob:RewardedVideo:NotConfigured[ reward_ad instance is null ]");
			return;
		}

        show_rewarded_video((String)reward_ads.keySet().toArray()[0]);
    }

	public void show_rewarded_video(final String unit_id) {
		if (!isInitialized() || reward_ads == null) {
            Utils.d("GodotFireBase", "AdMob:RewardedVideo:NotConfigured[ reward_ad instance is null ]");
			return;
		}

        RewardedAdCallback adCallback = new RewardedAdCallback() {
		    @Override
            public void onUserEarnedReward(@NonNull RewardItem reward) {
	    		Utils.d("GodotFireBase", "AdMob:Rewarded:Success");

		        Dictionary ret = new Dictionary();
    			ret.put("RewardType", reward.getType());
        		ret.put("RewardAmount", reward.getAmount());
	        	ret.put("unit_id", unit_id);

		        Utils.callScriptFunc("AdMob", "AdMobReward", ret);
    			reloadRewardedVideo(unit_id);
		    }

    		@Override
	    	public void onRewardedAdClosed() {
		    	Utils.d("GodotFireBase", "AdMob:VideoAd:Closed");
			    Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unit_id, "closed"));
    			reloadRewardedVideo(unit_id);
	    	}

    		@Override
	    	public void onRewardedAdOpened() {
		    	Utils.d("GodotFireBase", "AdMob:VideoAd:Opended");
    		}

        	@Override
	        public void onRewardedAdFailedToShow(int errorCode) {
			    Utils.d("GodotFireBase", "Reward:VideoAd:FailedToShow");
    		}
        };

        rewarded_meta_data.put("unit_id", unit_id);
        RewardedAd reward_ad = reward_ads.get(unit_id);
		if (reward_ad.isLoaded()) { reward_ad.show(activity, adCallback); }
		else { Utils.d("GodotFireBase", "AdMob:RewardedVideo:NotLoaded"); }
	}

	public void show_banner_ad(final boolean show) {
		if (!isInitialized() || mAdView == null) { return; }

		// Show Ad Banner here

		if (show) {
			if (mAdView.isEnabled()) { mAdView.setEnabled(true); }
			if (mAdView.getVisibility() == View.INVISIBLE) {
				Utils.d("GodotFireBase", "AdMob:Visiblity:On");
				mAdView.setVisibility(View.VISIBLE);
			}
		} else {
			if (mAdView.isEnabled()) { mAdView.setEnabled(false); }
			if (mAdView.getVisibility() != View.INVISIBLE) {
				Utils.d("GodotFireBase", "AdMob:Visiblity:Off");
				mAdView.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void show_interstitial_ad() {
		if (!isInitialized() || mInterstitialAd == null) { return; }

		// Show interstitial ad

		if (mInterstitialAd.isLoaded()) { mInterstitialAd.show(); }
		else { Utils.d("GodotFireBase", "AdMob:Interstitial:NotLoaded"); }
	}

	public void reloadRewardedVideo(final String unitid) {
        if (reward_ads == null) {
            return;
		}
		
		if (reload_count <= 3) {
            Utils.d("GodotFireBase", "AdMob:RewardedVideo:Reloading_RewardedVideo_Request");

            RewardedAd reward_ad = reward_ads.get(unitid);
    		requestNewRewardedVideo(reward_ad, unitid);
            
            reload_count += 1;
        }
	}

	private void requestNewRewardedVideo(RewardedAd mrv, final String unitid) {
		Utils.d("GodotFireBase", "AdMob:Loading:RewardedAd:For: " + unitid);

        mAdRewardLoaded = false;
		AdRequest.Builder adRB = new AdRequest.Builder();

		if (BuildConfig.DEBUG || AdMobConfig.optBoolean("TestAds", false)) {
			adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRB.addTestDevice(AdMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
		}

		mrv.loadAd(adRB.build(),  new RewardedAdLoadCallback() {
     	    @Override
    		public void onRewardedAdLoaded() {
    			Utils.d("GodotFireBase", "AdMob:Video:Loaded");
            
                mAdRewardLoaded = true;
			    Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "loaded"));
		    }

    		@Override
	    	public void onRewardedAdFailedToLoad(int errorCode) {
		    	Utils.d("GodotFireBase", "AdMob:VideoLoad:Failed");
			    Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "load_failed"));
    			//reloadRewardedVideo(unitid);
	    	}
        });
	}

	private void requestNewInterstitial() {
		AdRequest.Builder adRB = new AdRequest.Builder();

		if (BuildConfig.DEBUG || AdMobConfig.optBoolean("TestAds", false)) {
			adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRB.addTestDevice(AdMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
		}

		AdRequest adRequest = adRB.build();

		mInterstitialAd.loadAd(adRequest);
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Utils.d("GodotFireBase", "AdMob:NotInitialized.");
			return false;
		} else {
			return true;
		}
	}

	public void onStart() {
        reload_count = 0;
        rewarded_meta_data = new Dictionary();
	}

	public void onPause() {
		if (mAdView != null) { mAdView.pause(); }
		//if (reward_ad != null) { reward_ad.pause(activity); }
	}

	public void onResume() {
		if (mAdView != null) { mAdView.resume(); }
		//if (reward_ad != null) { reward_ad.resume(activity); }
	}

	public void onStop() {
        reload_count = 0;

		if (mAdView != null) { mAdView.destroy(); }
		//if (reward_ad != null) { reward_ad.destroy(activity); }
	}

    private int reload_count = 0;

	private static Activity activity = null;
	private static AdMob mInstance = null;

    private boolean mAdRewardLoaded = false;
    private boolean mAdViewLoaded = false;
	private HashMap<String, RewardedAd> reward_ads = null;

	private AdView mAdView = null;
	private InterstitialAd mInterstitialAd = null;
	private Dictionary mAdSize = null;

	private FirebaseApp mFirebaseApp = null;

	private JSONObject AdMobConfig = null;

    static private Dictionary rewarded_meta_data = null;
}

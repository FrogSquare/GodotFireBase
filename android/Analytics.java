
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

	public static Analytics getInstance(Activity p_activity) {
		if (mInstance == null) {
                        synchronized (Analytics.class) {
				mInstance = new Analytics(p_activity);
			}
		}

		return mInstance;
	}

	public Analytics (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp fireBaseApp) {
		mFirebaseApp = fireBaseApp;
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

		Log.d(TAG, "Firebase Analytics initialized..!");
	}

        public void set_screen_name(final String s_name) {
		Bundle bundle = new Bundle();
		bundle.putString("screen_name", s_name);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent("current_screen", bundle);

		Log.d(TAG, "Setting current screen to: " + s_name);
	}

        public void send_achievement(final String id) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);

		Log.d(TAG, "Sending:AchievementUnlocked: " + id);
	}

	public void send_group(final String groupID) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.GROUP_ID, groupID);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP, bundle);

		Log.d(TAG, "Sending:JoinGroup: " + groupID);
	}

	public void send_level_up(final String character, final int level) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CHARACTER, character);
		bundle.putInt(FirebaseAnalytics.Param.LEVEL, level);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);

		Log.d(TAG, "Sending:Character:LevelUp: {" + character + "|" + level + "}");
	}

	public void send_score(final String character, final int level, final int score) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CHARACTER, character);
		bundle.putInt(FirebaseAnalytics.Param.LEVEL, level);
		bundle.putInt(FirebaseAnalytics.Param.SCORE, score);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);

		Log.d(TAG, "Sending:Level:Score: {" + character + "|" + level + "|" + score + "}");
	}

	public void send_content(final String content_type, final String item_id) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, content_type);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item_id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

		Log.d(TAG, "Sending:Content:Item: {" + content_type + "|" + item_id + "}");
	}

	public void send_share() {
		// TODO

		//SHARE

		/**
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
		Log.d(TAG, "Sending Achievement: " + id);
		**/
	}

	public void earn_currency(final String currency_name, final int value) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, currency_name);
		bundle.putInt(FirebaseAnalytics.Param.VALUE, value);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, bundle);

		Log.d(TAG, "Sending:Currency:Earn: {" + currency_name + "|" + value + "}");
	}

	public void spend_currency(final String item_name, final String currency_name, final int value) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item_name);
		bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, currency_name);
		bundle.putInt(FirebaseAnalytics.Param.VALUE, value);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY, bundle);

		Log.d(TAG,
		"Sending:Currency:Spend: {" + item_name + "|" + currency_name + "|" + value + "}");
	}

	public void send_tutorial_begin() {
		Bundle bundle = new Bundle();
		//bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, bundle);

		Log.d(TAG, "Sending:Tutorial:Begin");
	}

	public void send_tutorial_complete() {
		Bundle bundle = new Bundle();
		//bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, bundle);
		Log.d(TAG, "Sending:Tutorial:Complete");
	}

        public void send_custom(final String key, final String value) {
		Bundle bundle = new Bundle();
		bundle.putString(key, value);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent("appEvent", bundle);
		Log.d(TAG, "Sending:App:Event:" + bundle.toString());
	}

	private static Context context;
	private static Activity activity = null;
	private static Analytics mInstance = null;

	private FirebaseApp mFirebaseApp = null;
	private FirebaseAnalytics mFirebaseAnalytics = null;

	private static final String TAG = "FireBase";
}

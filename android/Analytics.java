
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
		Log.d(TAG, "Firebase Analytics initialized..!");

		mFirebaseApp = fireBaseApp;
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
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
		Log.d(TAG, "Sending Achievement: " + id);
	}

        public void send_custom(final String key, final String value) {
		Bundle bundle = new Bundle();
		bundle.putString(key, value);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent("appEvent", bundle);
		Log.d(TAG, "Sending App Event: " + bundle.toString());
	}

	private static Context context;
	private static Activity activity = null;
	private static Analytics mInstance = null;

	private FirebaseApp mFirebaseApp = null;
	private FirebaseAnalytics mFirebaseAnalytics = null;

	private static final String TAG = "FireBase";
}

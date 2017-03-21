
package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.content.Intent;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FireBase extends Godot.SingletonBase {

	static public Godot.SingletonBase initialize (Activity p_activity) {
		return new FireBase(p_activity);
	}

	public FireBase(Activity p_activity) {
		registerClass ("FireBase", new String[] {
			"init", "setScreenName", "sendAchievement", "sendCustom", "alert",
			"subscribeToTopic", "getToken"
		});

		activity = p_activity;
	}

	private void initFireBase() {
		mFirebaseApp = FirebaseApp.initializeApp(activity);

		Analytics.getInstance(activity).init(mFirebaseApp);

		Log.d(TAG, "FireBase initialized.");
	}

	public void alertMsg(String message) {
		AlertDialog.Builder bld;

		bld = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT);
		bld.setIcon (com.godot.game.R.drawable.icon);
		bld.setTitle("FireBase");
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		bld.create().show();
	}

	public void initMessaging() {
		Notification.getInstance(activity).init(mFirebaseApp);
		Log.d(TAG, "Cloud Messaging initialized..!");
	}

	public void init() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				initFireBase();
			}
		});
	}

	public void setScreenName (final String screen_name) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Analytics.getInstance(activity).set_screen_name(screen_name);
			}
		});
	}

	public void sendAchievement(final String a_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Analytics.getInstance(activity).send_achievement(a_id);
			}
		});
	}

	public void sendCustom(final String key, final String value) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Analytics.getInstance(activity).send_custom(key, value);
			}
		});
	}

	public void alert(final String message) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				alertMsg(message);
			}
		});
	}

	public void subscribeToTopic (final String topic) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Notification.getInstance(activity).subscribe(topic);
			}
		});
	}

	public String getToken (final String topic) {
		return Notification.getInstance(activity).getFirebaseMessagingToken();
	}

	public void sendTokenToServer () {
		activity.runOnUiThread(new Runnable() {
			public void run() {

			}
		});
	}

	protected void onMainActivityResult (int requestCode, int resultCode, Intent data) {
		// Analytics.getInstance(activity).onActivityResult(requestCode, resultCode, data);
	}

	protected void onMainPause () {
		// Analytics.getInstance(activity).onPause();
	}

	protected void onMainResume () {
		// Analytics.getInstance(activity).onResume();
	}

	protected void onMainDestroy () {
		// Analytics.getInstance(activity).onStop();
	}

	private static Context context;
	private static Activity activity = null;
	protected static String currentScreen = "None";

	private FirebaseApp mFirebaseApp = null;
	private static final String TAG = "FireBase";
}

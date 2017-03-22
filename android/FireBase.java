
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
			"init", "setScreenName", "sendAchievement", "sendCustom",
			"subscribeToTopic", "getToken", "invite", "getRemoteValue",
			"setRemoteDefaults", "alert"
		});

		activity = p_activity;
	}

	private void initFireBase(final String data) {
		JSONObject config = null;
		mFirebaseApp = FirebaseApp.initializeApp(activity);

		Analytics.getInstance(activity).init(mFirebaseApp);

		if (data.length() <= 0) {
			Log.d(TAG, "FireBase initialized.");
			return;
		}

		try { config = new JSONObject(data); }
		catch (JSONException e) { Log.d(TAG, "JSON Parse error: " + e.toString()); }

		if (config.optBoolean("Notification", false)) {
			Log.d(TAG, "Initializing Firebase Notification.");
			Notification.getInstance(activity).init(mFirebaseApp);
		}

		if (config.optBoolean("RemoteConfig", false)) {
			Log.d(TAG, "Initializing Firebase RemoteConfig.");
			RemoteConfig.getInstance(activity).init(mFirebaseApp);
		}

		/**
		 * Experimental
		 * TODO: check multiple senarious.!
		 **/
		if (config.optBoolean("Invites", false)) {
			Log.d(TAG, "Initializing Firebase Invites.");
			Invites.getInstance(activity).init(mFirebaseApp);
		}

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

	public void init(final String config) {
		if (config.length() <= 0) {
			Log.d(TAG, "Config not provided; initializing Analytics only.");
		}

		activity.runOnUiThread(new Runnable() {
			public void run() {
				initFireBase(config);
			}
		});
	}

	public void setScreenName (final String screen_name) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (screen_name.length() <= 0) {
					Log.d(TAG, "Screen name is empty defaults to main");
					Analytics.getInstance(activity).set_screen_name("Main Screen");
				} else {
					Analytics.getInstance(activity).set_screen_name(screen_name);
				}
			}
		});
	}

	public void sendAchievement(final String a_id) {
		if (a_id.length() <= 0) {
			Log.d(TAG, "Achievement id not provided");
			return;
		}

		activity.runOnUiThread(new Runnable() {
			public void run() {
				Analytics.getInstance(activity).send_achievement(a_id);
			}
		});
	}

	public void sendCustom(final String key, final String value) {
		if (key.length() <= 0 || value.length() <= 0) {
			Log.d(TAG, "Key or Value is null.");
			return;
		}

		activity.runOnUiThread(new Runnable() {
			public void run() {
				Analytics.getInstance(activity).send_custom(key, value);
			}
		});
	}

	public void alert(final String message) {
		if (message.length() <= 0) {
			Log.d(TAG, "Message is empty.");
			return;
		}

		activity.runOnUiThread(new Runnable() {
			public void run() {
				alertMsg(message);
			}
		});
	}

	public void subscribeToTopic (final String topic) {
		if (topic.length() <= 0) {
			Log.d(TAG, "Topic id not provided.");
			return;
		}

		activity.runOnUiThread(new Runnable() {
			public void run() {
				Notification.getInstance(activity).subscribe(topic);
			}
		});
	}

	public String getToken () {
		return Notification.getInstance(activity).getFirebaseMessagingToken();
	}

	public void sendTokenToServer () {
		activity.runOnUiThread(new Runnable() {
			public void run() {

			}
		});
	}

	public String getRemoteValue (final String key) {
		if (key.length() <= 0) {
			Log.d(TAG, "getting remote config: key not provided, returning null");
			return "NULL";
		}

		return RemoteConfig.getInstance(activity).getValue(key);
	}

	public void setRemoteDefaults (final String jsonData) {
		if (jsonData.length() <= 0) {
			Log.d(TAG, "No defaults were provided.");
			return;
		}

		activity.runOnUiThread(new Runnable() {
			public void run() {
				RemoteConfig.getInstance(activity).setDefaults(jsonData);
			}
		});
	}

	public void invite () {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Invites.getInstance(activity).invite();
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

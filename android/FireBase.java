
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
			"init", "setScreenName", "sendAchievement", "sendCustom", "alert"
		});

		activity = p_activity;
	}

	private void initFireBase() {
		mFirebaseApp = FirebaseApp.initializeApp(activity);
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

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
				set_screen_name(screen_name);
			}
		});
	}

	public void sendAchievement(final String a_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				send_achievement(a_id);
			}
		});
	}

	public void sendCustom(final String key, final String value) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				send_custom(key, value);
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

	private void set_screen_name(final String s_name) {
		this.currentScreen = s_name;

		Bundle bundle = new Bundle();
		bundle.putString("screen_name", s_name);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent("current_screen", bundle);
		Log.d(TAG, "Setting current screen to: " + s_name);
	}

	private void send_achievement(final String id) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
		Log.d(TAG, "Sending Achievement: " + id);
	}

	private void send_custom(final String key, final String value) {
		Bundle bundle = new Bundle();
		bundle.putString(key, value);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent("appEvent", bundle);
		Log.d(TAG, "Sending App Event: " + bundle.toString());
	}

	private static Context context;
	private static Activity activity = null;
	protected static String currentScreen = "None";

	private FirebaseApp mFirebaseApp = null;
	private FirebaseAnalytics mFirebaseAnalytics = null;

	private static final String TAG = "FireBase";
}

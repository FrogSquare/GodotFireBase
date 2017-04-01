package org.godotengine.godot.storage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import org.json.JSONObject;
import org.json.JSONException;

public class Storage {

	public static Storage getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Storage(p_activity);
		}

		return mInstance;
	}

	public Storage(Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	public void onStart() {

	}

	public void onPause() {

	}

	public void onResume() {

	}

	public void onStop() {

	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Log.d(TAG, "FireBase Storage, not initialized");
			return false;
		}

		return true;
	}

	private static Activity activity = null;
	private static Storage mInstance = null;

	private FirebaseApp mFirebaseApp = null;

	private static final String TAG = "FireBase";

}

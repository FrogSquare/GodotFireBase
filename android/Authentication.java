
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import com.godot.game.BuildConfig;
import com.godot.game.R;

import org.json.JSONObject;
import org.json.JSONException;

public class Authentication {

	public static Authentication getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Authentication(p_activity);
		}

		return mInstance;
	}

	public Authentication (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
	}

	private boolean isInitialized () {
		if (mFirebaseApp == null) {
			Log.d(TAG, "Authentication is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Activity activity = null;
	private static Context context = null;
	private static Authentication mInstance = null;

	private FirebaseApp mFirebaseApp = null;

	private static final String TAG = "FireBase";
}


package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

public class Invites {

	public static Invites getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Invites(p_activity);
		}

		return mInstance;
	}

	public Invites(Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
	}

	public void invite () {
		if (!isInitialized()) { return; }

	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Log.d(TAG, "Invites is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Activity activity = null;
	private static Context context = null;
	private static Invites mInstance = null;

	private FirebaseApp mFirebaseApp = null;

	private static final String TAG = "FireBase";
}

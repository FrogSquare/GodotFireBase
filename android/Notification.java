
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class Notification {

	public static Notification getInstance (Activity p_activity) {
		if (mInstance == null) {
			synchronized (Notification.class) {
				mInstance = new Notification(p_activity);
			}
		}

		return mInstance;
	}

	public Notification (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
		String token = getFirebaseMessagingToken();

		Log.d(TAG, "Firebase Cloud messaging token: " + token);
	}

	public void subscribe(final String topic) {
		if (!isInitialized()) { return; }

		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

	public String getFirebaseMessagingToken() {
		if (!isInitialized()) { return "NULL"; }

		return FirebaseInstanceId.getInstance().getToken();
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Log.d(TAG, "Notification is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Context context;
	private static Activity activity;

	private FirebaseApp mFirebaseApp = null;

	private static Notification mInstance = null;

	private static final String TAG = "FireBase";
}

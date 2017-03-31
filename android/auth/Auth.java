
package org.godotengine.godot.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import org.json.JSONObject;
import org.json.JSONException;

public class Auth {

	public static final int GOOGLE_AUTH 	= 0x0003;
	public static final int FACEBOOK_AUTH	= 0x0004;

	public static Auth getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Auth(p_activity);
		}

		return mInstance;
	}

	public Auth(Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
	}

	public void configure (final String configData) {
		try { config = new JSONObject(configData); }
		catch (JSONException e) { Log.d(TAG, "JSONException, parse error: " + e.toString()); }

		if (config.optBoolean("Google", false)) {
			GoogleSignIn.getInstance(activity).init();
		}

		if (config.optBoolean("Facebook", false)) {
			FacebookSignIn.getInstance(activity).init();
		}
	}

	public void sign_in (final int type_id) {
		if (!isInitialized()) { return; }

		Log.d(TAG, "Auth:SignIn:TAG:" + type_id);

		switch (type_id) {
			case GOOGLE_AUTH:
				Log.d(TAG, "Auth google sign in");
				GoogleSignIn.getInstance(activity).signIn();
				break;
			case FACEBOOK_AUTH:
				Log.d(TAG, "Auth facebook sign in");
				FacebookSignIn.getInstance(activity).signIn();
				break;
			default:
				Log.d(TAG, "Auth type not available.");
				break;
		}
	}

	public void sign_out (final int type_id) {
		if (!isInitialized()) { return; }

		Log.d(TAG, "Auth:SignOut:TAG:" + type_id);

		switch (type_id) {
			case GOOGLE_AUTH:
				Log.d(TAG, "Auth google sign out");
				GoogleSignIn.getInstance(activity).signOut();
				break;
			case FACEBOOK_AUTH:
				Log.d(TAG, "Auth facebook sign out");
				FacebookSignIn.getInstance(activity).signOut();
				break;
			default:
				Log.d(TAG, "Auth type not available.");
				break;
		}
	}

	public void revoke(final int type_id) {
		if (!isInitialized()) { return; }

		Log.d(TAG, "FB:Auth:!evoke:" + type_id);

		switch (type_id) {
			case GOOGLE_AUTH:
				Log.d(TAG, "FB:Revoke:Google");
				GoogleSignIn.getInstance(activity).revokeAccess();
				break;
			case FACEBOOK_AUTH:
				Log.d(TAG, "FB:Revoke:Facebook");
				FacebookSignIn.getInstance(activity).revokeAccess();
				break;
			default:
				Log.d(TAG, "FB:Auth:Type:NotFound");
		}

	}

	public String getUserDetails(final int type_id) {
		if (!isInitialized()) { return "NULL"; }

		Log.d(TAG, "UserDetails:TAG:" + type_id);

		if (type_id == GOOGLE_AUTH && GoogleSignIn.getInstance(activity).isConnected()) {
			Log.d(TAG, "Getting Google user details");
			return GoogleSignIn.getInstance(activity).getUserDetails();
		}

		if (type_id == FACEBOOK_AUTH && FacebookSignIn.getInstance(activity).isConnected()) {
			Log.d(TAG, "Getting Facebook user details");
			return FacebookSignIn.getInstance(activity).getUserDetails();
		}

		return "NULL";
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Log.d(TAG, "FireBase Auth, not initialized");
			return false;
		}

		if (config == null) {
			Log.d(TAG, "FireBase Auth, not Configured");
			return false;
		}

		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!isInitialized()) { return; }

		if (config.optBoolean("Google", false)) {
			GoogleSignIn.getInstance(activity)
			.onActivityResult(requestCode, resultCode, data);
		}

		if (config.optBoolean("Facebook", false)) {
			FacebookSignIn.getInstance(activity)
			.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onStart() {
		if (!isInitialized()) { return; }

		if (config.optBoolean("Google", false)) {
			GoogleSignIn.getInstance(activity).onStart();
		}

		if (config.optBoolean("Facebook", false)) {
			FacebookSignIn.getInstance(activity).onStart();
		}

	}

	public void onPause() {

	}

	public void onResume () {

	}

	public void onStop() {
		if (!isInitialized()) { return; }

		if (config.optBoolean("Google", false)) {
			GoogleSignIn.getInstance(activity).onStop();
		}

		if (config.optBoolean("Facebook", false)) {
			FacebookSignIn.getInstance(activity).onStop();
		}
	}

	private static Activity activity = null;
	private static Auth mInstance = null;

	private static JSONObject config = null;

	private FirebaseApp mFirebaseApp = null;

	private static final String TAG = "FireBase";
}



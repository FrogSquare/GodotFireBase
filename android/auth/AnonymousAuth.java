
package org.godotengine.godot.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.godotengine.godot.FireBase;
import org.godotengine.godot.KeyValueStorage;
import org.godotengine.godot.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class AnonymousAuth {

	public static AnonymousAuth getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new AnonymousAuth(p_activity);
		}

		return mInstance;
	}

	public AnonymousAuth(Activity p_activity) {
		activity = p_activity;
	}

	public void init() {
		// Initialize listener.
		// ...
	}

	protected void successSignIn () {

	}

	protected void successSignOut () {

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ...
	}

	public void onStart () {

	}

	public void onPause () {

	}

	public void onResume () {

	}

	public void onStop () {

	}

	private static Activity activity = null;
	private static AnonymousAuth mInstance = null;

	private static final String TAG = "FireBase";
}

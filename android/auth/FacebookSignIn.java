
package org.godotengine.godot.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.GameRequestContent.ActionType;
import com.facebook.share.widget.GameRequestDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.godot.game.R;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.KeyValueStorage;
import org.godotengine.godot.Utils;

public class FacebookSignIn {

	public static FacebookSignIn getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new FacebookSignIn(p_activity);
		}

		return mInstance;
	}

	public FacebookSignIn(Activity p_activity) {
		activity = p_activity;
	}

	public void init() {
		// Initialize listener.
		// ...

		FacebookSdk.sdkInitialize(activity);
		// FacebookSdk.setApplicationId(activity.getString(R.string.facebook_app_id));

		mAuth = FirebaseAuth.getInstance();
		mAuthListener = new FirebaseAuth.AuthStateListener() {

			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					Log.d(TAG, "FB:onAuthStateChanged:signed_in:" + user.getUid());
					successLogin(user);
				} else {
					// User is signed out
					Log.d(TAG, "FB:onAuthStateChanged:signed_out");
					successLogOut();
				}

				// update user details;
			}
		};

		// AppEvenntsLogger.activityApp(activity);

		initCallbacks();

		Log.d(TAG, "Facebook auth initialized.");
	}

	private void initCallbacks() {
		callbackManager = CallbackManager.Factory.create();
		/**
		requestDialog = new GameRequestDialog(activity);
		requestDialog.registerCallback(callbackManager,
		new FacebookCallback<GameRequestDialog.Result>() {
			@Override
			public void onSuccess (GameRequestDialog.Result result) {
				Log.d(TAG, "Facebook request sent.");
			}
		});
		**/

		LoginManager.getInstance().registerCallback(callbackManager,
		new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult result) {
				Log.d(TAG, "FB:Connected");
				handleAccessToken(result.getAccessToken());
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "FB:Canceled");
			}

			@Override
			public void onError(FacebookException exception) {
				Log.d(TAG, "FB:Error, " + exception.toString());
			}
		});

		mAccessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(
			AccessToken old, AccessToken current) {

				Log.d(TAG, "FB:AccessToken:Changed");
				if (current == null) { /** Logged out **/ }
				else { /** Signed in **/ }
			}
		};

		mProfileTracker = new ProfileTracker() {
			@Override
			protected void onCurrentProfileChanged(Profile old, Profile current) {
				Log.d(TAG, "FB:Profile:Changed");
			}
		};
	}

	public void signIn() {
		if (callbackManager == null) {
			Log.d(TAG, "FB:Initialized");
			return;
		}

		LoginManager.getInstance().logInWithReadPermissions(
		activity, Arrays.asList("email", "public_profile"));
	}

	public void signOut() {
		if (callbackManager == null) { return; }

		Log.d(TAG, "FB:Logout");

		mAuth.signOut();
		LoginManager.getInstance().logOut();
	}

	public void handleAccessToken(AccessToken token) {
		Log.d(TAG, "FB:Handle:AccessToken: " + token);
		// showProgressDialog();

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

		mAuth.signInWithCredential(credential)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {

			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Log.d(TAG, "FB:signInWithCredential:onComplete:" + task.isSuccessful());

				// If sign in fails, display a message to the user. If sign in succeeds
				// the auth state listener will be notified and logic to handle the
				// signed in user can be handled in the listener.

				if (!task.isSuccessful()) {
					Log.w(TAG, "FB:signInWithCredential", task.getException());
				}

				// hideProgressDialog();
			}
		});
	}

	protected void successLogin (FirebaseUser user) {
		isFacebookConnected = true;

		try {
			currentFBUser.put("name", user.getDisplayName());
			currentFBUser.put("email", user.getEmail());
			currentFBUser.put("photoUri", user.getPhotoUrl());
		} catch (JSONException e) { Log.d(TAG, "FB:JSON:Error:" + e.toString()); }

		// call Script
		Utils.callScriptFunc("FacebookLogin", "true");
	}

	protected void successLogOut () {
		isFacebookConnected = false;

		currentFBUser = null;
		currentFBUser = new JSONObject();

		// call script.
		Utils.callScriptFunc("FacebookLogin", "false");
	}

	public String getUserDetails() {
		return currentFBUser.toString();
	}

	public boolean isConnected() {
		return isFacebookConnected;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	public void onStart () {
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop () {
		if (mAuthListener != null) { mAuth.removeAuthStateListener(mAuthListener); }

		isFacebookConnected = false;
		activity = null;
	}


	private static Activity activity = null;
	private static FacebookSignIn mInstance = null;

	// private static GameRequestDialog requestDialog;
	private static CallbackManager callbackManager;

	private static AccessTokenTracker mAccessTokenTracker;
	private static ProfileTracker mProfileTracker;

	private Boolean isFacebookConnected = false;
	private JSONObject currentFBUser = new JSONObject();

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;

	private static final String TAG = "FireBase";
}

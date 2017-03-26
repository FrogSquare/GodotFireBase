
package org.godotengine.godot.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.godot.game.R;

import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.FireBase;
import org.godotengine.godot.Utils;

public class GoogleSignIn
	implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

	public static GoogleSignIn getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new GoogleSignIn(p_activity);
		}

		return mInstance;
	}

	public GoogleSignIn(Activity p_activity) {
		activity = p_activity;
	}

	public void init() {
		// Initialize listener.
		// ...

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
		.requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
		.build();

		mGoogleApiClient = new GoogleApiClient.Builder(activity)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
		.build();

		Log.d(TAG, "Google:Initialized");

		mAuth = FirebaseAuth.getInstance();

		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();

				if (user != null) {
					// User is signed in
					Log.d(TAG,
					"Google:onAuthStateChanged:signed_in:" + user.getUid());

					if (!mGoogleApiClient.isConnected() &&
					!mGoogleApiClient.isConnecting()) {
						mGoogleApiClient.connect();
					}

					successSignIn(user);
				} else {
					// User is signed out
					Log.d(TAG, "Google:onAuthStateChanged:signed_out");
					successSignOut();
				}

				// update firebase auth dets.
			}
		};

		onStart(); // calling on start form init
	}

	public void signIn() {
		if (mGoogleApiClient == null) {
			Log.d(TAG, "Google:NotInitialized");
			return;
		}

		if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
			Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
			activity.startActivityForResult(signInIntent, Utils.FIREBASE_GOOGLE_SIGN_IN);
		} else { Log.d(TAG, "Google auth connected."); }
	}

	public void signOut() {
		// Firebase sign out
		mAuth.signOut();

		// Google sign out
		if (mGoogleApiClient.isConnected()) {
			Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
			new ResultCallback<Status>() {

				@Override
				public void onResult(@NonNull Status status) {
					// update user details.
				}
			});
		}
	}

	public void revokeAccess () {
		mAuth.signOut();

		// Google sign out
		Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
		new ResultCallback<Status>() {

			@Override
			public void onResult(@NonNull Status status) {
				// update user details.
			}
		});
	}

	public String getUserDetails() {
		return currentGoogleUser.toString();
	}

	public boolean isConnected() {
		return isGooglePlayConnected;
	}

	protected void successSignIn(FirebaseUser user) {
		Log.d(TAG, "Google:Connection:Success");

		isResolvingConnectionFailure = false;
		isGooglePlayConnected = true;
		isRequestingSignIn = false;

		try {
			currentGoogleUser.put("name", user.getDisplayName());
			currentGoogleUser.put("email", user.getEmail());
			currentGoogleUser.put("photoUri", user.getPhotoUrl());
		} catch (JSONException e) { Log.d(TAG, "Google:JSON:Error:" + e.toString()); }

		Utils.callScriptFunc("GoogleLogin", "true");
	}

	protected void successSignOut() {
		Log.d(TAG, "Google:Disconnected");

		isGooglePlayConnected = false;

		currentGoogleUser = null;
		currentGoogleUser = new JSONObject();

		Utils.callScriptFunc("GoogleLogin", "false");
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

		Log.d(TAG, "Google:FirebaseAuthWithGoogle:" + acct.getId());

		// FireBase.showProgressDialog();

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {

			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Log.d(TAG,
				"Google:SignInWithCredential:onComplete:" + task.isSuccessful());

				// If sign in fails, display a message to the user. If sign in succeeds
				// the auth state listener will be notified and logic to handle the
				// signed in user can be handled in the listener.
				if (!task.isSuccessful()) {
					Log.w(TAG, "Google:SignInWithCredential", task.getException());
				}

				// FireBase.hideProgressDialog();
			}
		});
	}

	@Override
	public void onConnected(Bundle m_bundle) {
		Log.d(TAG, "Google:onConnected");

		if (m_bundle != null) {

		}
	}

	@Override
	public void onConnectionSuspended(int m_cause) {
		Log.d(TAG, "Google:Connection:Suspended:Check:Internet");
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult m_result) {
		Log.d(TAG, "Google:Connection:Failed");

		if (isResolvingConnectionFailure) { return; }
		if(!isIntentInProgress && m_result.hasResolution()) {
			try {
				isIntentInProgress = true;

				activity.startIntentSenderForResult(
				m_result.getResolution().getIntentSender(),
				Utils.FIREBASE_GOOGLE_SIGN_IN, null, 0, 0, 0);

			} catch (SendIntentException ex) {
				isIntentInProgress = false;
				signIn();
			}

			isResolvingConnectionFailure = true;
			Log.d(TAG, "Google:Connection:Resolving.");
                }
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

		if (requestCode == Utils.FIREBASE_GOOGLE_SIGN_IN) {
			GoogleSignInResult result =
			Auth.GoogleSignInApi.getSignInResultFromIntent(data);

			isIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) { mGoogleApiClient.connect(); }

			if (result.isSuccess()) {
				// Google Sign In was successful, authenticate with Firebase

				GoogleSignInAccount account = result.getSignInAccount();
				firebaseAuthWithGoogle(account);
			} else {
				// Google Sign In failed, update UI appropriately
				// updateUI(null);
			}
		}
	}

	public void onStart () {
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop () {
		if (mAuthListener != null) { mAuth.removeAuthStateListener(mAuthListener); }
		if (mGoogleApiClient.isConnected()) { mGoogleApiClient.disconnect(); }

		isGooglePlayConnected = false;
		activity = null;
	}

	private static Activity activity = null;
	private static GoogleSignIn mInstance = null;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;

	private JSONObject currentGoogleUser = new JSONObject();

	private Boolean isRequestingSignIn = false;
        private Boolean isIntentInProgress = false;
        private Boolean isGooglePlayConnected = false;
        private Boolean isResolvingConnectionFailure = false;

	private static GoogleApiClient mGoogleApiClient = null;

	private static final String TAG = "FireBase";
}

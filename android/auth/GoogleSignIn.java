/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

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
import com.google.firebase.auth.UserInfo;

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

		/**

		try {
			Class.forName("org.godotengine.godot.PlayService");
		} catch () {  }

		 **/

		mGoogleApiClient = new GoogleApiClient.Builder(activity)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
		.build();

		Utils.d("Google:Initialized");

		mAuth = FirebaseAuth.getInstance();

		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();

				if (user != null) {
					for (UserInfo usr : user.getProviderData()) {
						if (usr.getProviderId().equals("google.com")) {
							Utils.d("Google:AuthStateChanged:signed_in:"+
							user.getUid());

							successSignIn(user);
						}
					}
				} else {
					// User is signed out
					Utils.d("Google:onAuthStateChanged:signed_out");
					successSignOut();
				}

				// update firebase auth dets.
			}
		};

		onStart(); // calling on start form init
	}

	public void signIn() {
		if (mGoogleApiClient == null) {
			Utils.d("Google:NotInitialized");
			return;
		}

		if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
			Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
			activity.startActivityForResult(signInIntent, Utils.FIREBASE_GOOGLE_SIGN_IN);
		} else { Utils.d("Google auth connected."); }
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
		Utils.d("Google:Connection:Success");

		isResolvingConnectionFailure = false;
		isGooglePlayConnected = true;
		isRequestingSignIn = false;

		try {
			currentGoogleUser.put("name", user.getDisplayName());
			currentGoogleUser.put("email_id", user.getEmail());
			currentGoogleUser.put("photo_uri", user.getPhotoUrl());
		} catch (JSONException e) { Utils.d("Google:JSON:Error:" + e.toString()); }

		Utils.callScriptFunc("GoogleLogin", "true");
	}

	protected void successSignOut() {
		Utils.d("Google:Disconnected");

		isGooglePlayConnected = false;

		currentGoogleUser = null;
		currentGoogleUser = new JSONObject();

		Utils.callScriptFunc("GoogleLogin", "false");
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

		Utils.d("Google:FirebaseAuthWithGoogle:" + acct.getId());

		// FireBase.showProgressDialog();

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {

			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Utils.d(
				"Google:SignInWithCredential:onComplete:" + task.isSuccessful());

				// If sign in fails, display a message to the user. If sign in succeeds
				// the auth state listener will be notified and logic to handle the
				// signed in user can be handled in the listener.
				if (!task.isSuccessful()) {
					Utils.w("Google:SignInWithCredential:" + task.getException());
				}

				// FireBase.hideProgressDialog();
			}
		});
	}

	@Override
	public void onConnected(Bundle m_bundle) {
		Utils.d("Google:onConnected");

		if (m_bundle != null) {

		}
	}

	@Override
	public void onConnectionSuspended(int m_cause) {
		Utils.d("Google:Connection:Suspended:Check:Internet");
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult m_result) {
		Utils.d("Google:Connection:Failed");

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
			Utils.d("Google:Connection:Resolving.");
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
}

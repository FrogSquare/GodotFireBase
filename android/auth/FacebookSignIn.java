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

import android.app.*;
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
import com.facebook.HttpMethod;
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
import com.google.firebase.auth.UserInfo;

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
					for (UserInfo usr : user.getProviderData()) {
						if (usr.getProviderId().equals("facebook.com")) {
							Utils.d("FB:AuthStateChanged:signed_in:"+
							user.getUid());
							successLogin(user);
						}
					}
				} else {
					// User is signed out
					Utils.d("FB:onAuthStateChanged:signed_out");
					successLogOut();
				}

				// update user details;
			}
		};

		// AppEventsLogger.activityApp(activity);

		initCallbacks();
		onStart();

		Utils.d("Facebook auth initialized.");
	}

	private void initCallbacks() {
		callbackManager = CallbackManager.Factory.create();
		/**
		requestDialog = new GameRequestDialog(activity);
		requestDialog.registerCallback(callbackManager,
		new FacebookCallback<GameRequestDialog.Result>() {
			@Override
			public void onSuccess (GameRequestDialog.Result result) {
				Utils.d("Facebook request sent.");
			}
		});
		**/

		LoginManager.getInstance().registerCallback(callbackManager,
		new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult result) {
				Utils.d("FB:Connected");
				handleAccessToken(result.getAccessToken());
			}

			@Override
			public void onCancel() {
				Utils.d("FB:Canceled");
			}

			@Override
			public void onError(FacebookException exception) {
				Utils.d("FB:Error, " + exception.toString());
			}
		});

		mAccessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(
			AccessToken old, AccessToken current) {

				Utils.d("FB:AccessToken:Changed");
				if (current == null) { successLogOut(); }
				else {
					accessToken = current;

					try {
						currentFBUser
						.put("token", accessToken.getToken().toString());
					} catch (JSONException e) {
						Utils.d("FB:JSON:Error:162:" + e.toString());
					}
				}
			}
		};

		mProfileTracker = new ProfileTracker() {
			@Override
			protected void onCurrentProfileChanged(Profile old, Profile current) {
				Utils.d("FB:Profile:Changed");
				profile = current;
			}
		};

		mAccessTokenTracker.startTracking();
		mProfileTracker.startTracking();

		accessToken = AccessToken.getCurrentAccessToken();
		profile = Profile.getCurrentProfile();
	}

	public boolean isPermissionGiven (final String permission) {
		Utils.d("FB:Checking:Available:Permissions:For: " + permission);
		accessToken = AccessToken.getCurrentAccessToken();

		if (accessToken == null && accessToken.isExpired()) {
			Utils.d("FB:Token:NotValid");
			return false;
		}

		return (mUserPermissions.contains(permission));
	}

	public void getPermissions() {
		String uri = "me/permissions/";

		new GraphRequest(AccessToken.getCurrentAccessToken(),
		uri, null, HttpMethod.GET,
			new GraphRequest.Callback() {
				public void onCompleted(GraphResponse response) {
					/* handle the result */
					JSONArray data = response.getJSONObject().optJSONArray("data");
					mUserPermissions.clear();

					for (int i = 0; i < data.length(); i++) {
						JSONObject dd = data.optJSONObject(i);

						if (dd.optString("status").equals("granted")) {
							mUserPermissions
							.add(dd.optString("permission"));
						}
					}
				}
			}
		).executeAsync();
	}

	public String getUserPermissions() {
		if (!isConnected() && mUserPermissions.size() > 0) {
			Utils.d("FB:Check:Login");
			return "NULL";
		}

		return mUserPermissions.toString();
	}

	public void revokePermission(final String permission) {
		AccessToken token = AccessToken.getCurrentAccessToken();

		String uri = "me/permissions/" + permission;

		GraphRequest graphRequest = GraphRequest.newDeleteObjectRequest(
		token, uri, new GraphRequest.Callback() {
			@Override
			public void onCompleted(GraphResponse response) {
				FacebookRequestError error = response.getError();
				if (error == null) {
					Utils.d("FB:Revoke:Response:" + response.toString());
					getPermissions();
				}
			}
		});

		graphRequest.executeAsync();
	}

	public void askForPermission (
	final String title, final String message, final String perm, final boolean read) {

		new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if (!read) { requestPublishPermissions(Arrays.asList(perm)); }
				else { requestReadPermissions(Arrays.asList(perm)); }
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		})
		.setTitle(title)
		.setMessage(message)
		.show();
	}

	public void requestReadPermissions(List permissions) {
		LoginManager.getInstance().logInWithReadPermissions(
		activity, permissions);
	}

	public void requestPublishPermissions(List permissions) {
		LoginManager.getInstance().logInWithPublishPermissions(
		activity, permissions);
	}

	public void signIn() {
		if (callbackManager == null) {
			Utils.d("FB:Initialized");
			return;
		}

		requestReadPermissions(Arrays.asList("email", "public_profile"));
//		LoginManager.getInstance().logInWithReadPermissions(
//		activity, Arrays.asList("email", "public_profile"));
	}

	public void signOut() {
		if (callbackManager == null) { return; }

		Utils.d("FB:Logout");

		mAuth.signOut();
		LoginManager.getInstance().logOut();
	}

	/** GraphRequest **/

	/**
	public void newRequest(final String uri, final String data) {

	}

	public void newPostRequest(final String uri, final String data) {
		AccessToken token = AccessToken.getCurrentAccessToken();

		GraphRequest graphRequest = GraphRequest.newDeleteObjectRequest(
		token, uri, new GraphRequest.Callback() {
			@Override
			public void onCompleted(GraphResponse response) {
				Utils.d("Revoke Permission: " + permission);
			}
		});

		graphRequest.executeAsync();
	}

	public void newDeleteRequest(final String uri, final String data) {

	}
	**/

	/** GraphRequest **/

	public void revokeAccess() {
		mAuth.signOut();

		AccessToken token = AccessToken.getCurrentAccessToken();
		GraphRequest graphRequest = GraphRequest.newDeleteObjectRequest(
		token, "me/permissions", new GraphRequest.Callback() {
			@Override
			public void onCompleted(GraphResponse response) {
				FacebookRequestError error = response.getError();
				if (error == null) {
					Utils.d("FB:Delete:Access" + response.toString());
				}
			}
		});

		graphRequest.executeAsync();
	}

	public void handleAccessToken(AccessToken token) {
		Utils.d("FB:Handle:AccessToken: " + token.getToken());
		// showProgressDialog();

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

		mAuth.signInWithCredential(credential)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {

			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Utils.d("FB:signInWithCredential:onComplete:" + task.isSuccessful());

				// If sign in fails, display a message to the user. If sign in succeeds
				// the auth state listener will be notified and logic to handle the
				// signed in user can be handled in the listener.

				if (!task.isSuccessful()) {
					Utils.w("FB:signInWithCredential" + 
						task.getException().toString());
				}

				// hideProgressDialog();
			}
		});
	}

	protected void successLogin (FirebaseUser user) {
		Utils.d("FB:Login:Success");

		isFacebookConnected = true;
		accessToken = AccessToken.getCurrentAccessToken();

		try {
			currentFBUser.put("name", user.getDisplayName());
			currentFBUser.put("email_id", user.getEmail());
			currentFBUser.put("photo_uri", user.getPhotoUrl());
			currentFBUser.put("token", accessToken.getToken().toString());

		} catch (JSONException e) { Utils.d("FB:JSON:Error:" + e.toString()); }

		getPermissions();

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

		mAccessTokenTracker.stopTracking();
		mProfileTracker.stopTracking();
	}

	private static Activity activity = null;
	private static FacebookSignIn mInstance = null;

	// private static GameRequestDialog requestDialog;
	private static CallbackManager callbackManager;

	private static AccessTokenTracker mAccessTokenTracker;
	private static ProfileTracker mProfileTracker;

	private static AccessToken accessToken;
	private static Profile profile;

	private Boolean isFacebookConnected = false;
	private JSONObject currentFBUser = new JSONObject();

	private ArrayList mUserPermissions = new ArrayList<String>();

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
}

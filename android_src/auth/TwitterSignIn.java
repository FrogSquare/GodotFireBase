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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import io.fabric.sdk.android.Fabric;

import com.godot.game.R;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.KeyValueStorage;
import org.godotengine.godot.Utils;

public class TwitterSignIn {

	public static TwitterSignIn getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new TwitterSignIn(p_activity);
		}

		return mInstance;
	}

	public TwitterSignIn(Activity p_activity) {
		activity = p_activity;
	}

	public void init() {
		TwitterAuthConfig authConfig = new TwitterAuthConfig(
		activity.getString(R.string.twitter_consumer_key),
		activity.getString(R.string.twitter_consumer_secret));

		Fabric.with(activity, new Twitter(authConfig));

		mAuth = FirebaseAuth.getInstance();

		twitterAuthClient = new TwitterAuthClient();
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();

				if (user != null) {
					// User is signed in
					for (UserInfo usr : user.getProviderData()) {
						if (usr.getProviderId().equals("twitter.com")) {
							Utils.d("Twitter:AuthStateChanged:signed_in:"+
							user.getUid());

							successSignIn(user);
						}
					}

				} else {
					// User is signed out
					Utils.d("Twitter:onAuthStateChanged:signed_out");
					successSignOut();
				}

				// update firebase auth dets.
			}
		};

		onStart();
	}

	public void signIn() {
		twitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
			@Override
			public void success(final Result<TwitterSession> result) {
				final TwitterSession sessionData = result.data;
				handleTwitterSession(sessionData);
			}

			@Override
			public void failure(final TwitterException e) {
				// Do something on fail
				Utils.d("Twitter::Login:Failed");
			}
		});
	}

	public void signOut() {
		mAuth.signOut();
	}

	protected void successSignIn(FirebaseUser user) {
		Utils.d("Twitter:Connection:Success");

		isTwitterConnected = true;

		try {
			currentTwitterUser.put("uid", user.getUid());
			currentTwitterUser.put("name", user.getDisplayName());
			currentTwitterUser.put("email_id", user.getEmail());
			currentTwitterUser.put("photo_uri", user.getPhotoUrl());
		} catch (JSONException e) { Utils.d("Twitter:JSON:Error:" + e.toString()); }

		Utils.callScriptFunc("Auth", "TwitterLogin", "true");
	}

	protected void successSignOut() {
		Utils.d("Twitter:Disconnected");

		isTwitterConnected = false;

		currentTwitterUser = null;
		currentTwitterUser = new JSONObject();

		Utils.callScriptFunc("Auth", "TwitterLogin", "false");
	}

	private void handleTwitterSession(TwitterSession session) {
		Utils.d("Twitter:HandleSession:" + session);

		AuthCredential credential = TwitterAuthProvider.getCredential(
		session.getAuthToken().token,
		session.getAuthToken().secret);

		mAuth.signInWithCredential(credential)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					// Sign in success, update UI with the signed-in user's information
					Utils.d("signInWithCredential:success");
				} else {
					// If sign in fails, display a message to the user.
					Utils.w("signInWithCredential:failure: " + task.getException());
				}

				// ...
			}
		});
	}

	public void onStart () {
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop () {
		if (mAuthListener != null) { mAuth.removeAuthStateListener(mAuthListener); }

		isTwitterConnected = false;
		activity = null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		twitterAuthClient.onActivityResult(requestCode, resultCode, data);
	}

	private static Activity activity = null;
	private static TwitterSignIn mInstance = null;
	private static TwitterAuthClient twitterAuthClient = null;

	private JSONObject currentTwitterUser = new JSONObject();
	private Boolean isTwitterConnected = false;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
}

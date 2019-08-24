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
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;

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

		mAuth = FirebaseAuth.getInstance();
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();

				if (user != null) {
					// User is signed in
					Utils.d("GodotFireBase", 
					"Anonymous:onAuthStateChanged:signed_in:" + user.getUid());

					successSignIn(user);
				} else {
					// User is signed out
					Utils.d("GodotFireBase", "Anonymous:onAuthStateChanged:signed_out");
					successSignOut();
				}

				// update firebase auth dets.
			}
		};

		onStart();
	}

	public void signIn() {
		mAuth.signInAnonymously()
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					//Sign in success, update with the signed-in user's information
					Utils.d("GodotFireBase", "Anonymous:SignIn:Success");
				}
			}

		});
	}

	public void signUp (final int authType) {
/**
		AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		AuthCredential credential = EmailAuthProvider.getCredential(email, password);

		mAuth.getCurrentUser().linkWithCredential(credential)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {

				} else {

				}

				// ...
			}
		});
**/
	}

	public void signOut() {
		mAuth.signOut();
	}

	protected void successSignIn (FirebaseUser user) {
		isAnonymousConnected = true;
	}

	protected void successSignOut () {
		isAnonymousConnected =false;
	}

	public boolean isConnected () {
		return isAnonymousConnected;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ...
	}

	public void onStart () {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) { successSignIn(user); }

		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop () {
		if (mAuthListener != null) { mAuth.removeAuthStateListener(mAuthListener); }

		isAnonymousConnected = false;
		activity = null;
	}

	private static Activity activity = null;
	private static AnonymousAuth mInstance = null;

	private static boolean isAnonymousConnected = false;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
}

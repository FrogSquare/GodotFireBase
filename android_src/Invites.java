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

package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import com.google.firebase.FirebaseApp;

import com.godot.game.R;

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

	public void invite (final String message) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, message);
		sendIntent.setType("text/plain");

		activity.startActivity(Intent.createChooser(sendIntent,
		activity.getResources().getText(R.string.send_to)));
	}

	public void invite (final String message, final String deepLink) {
		if (!isInitialized()) { return; }
		if (message.length() > AppInviteInvitation.IntentBuilder.MAX_MESSAGE_LENGTH) {
			Utils.d("Message is too big for Invite, Max 100 characters.");
			return;
		}

		Intent intent = new AppInviteInvitation.IntentBuilder("Invite Friends")
		.setMessage(message)
		.setCallToActionText("Install!")
		.setDeepLink(Uri.parse(deepLink))
		.build();

		activity.startActivityForResult(intent, Utils.FIREBASE_INVITE_REQUEST);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Utils.d("onActivityResult: reqCode=" + requestCode + ", resCode=" + resultCode);

		if (requestCode == Utils.FIREBASE_INVITE_REQUEST) {
			if (resultCode == activity.RESULT_OK) {
				// Get the invitation IDs of all sent messages
				Utils.d("Invite sent...!");

				String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);

				for (String id : ids) {
					Utils.d("onActivityResult: sent invitation " + id);
				}
			} else {
				// Sending failed or it was canceled, show failure message to the user
				Utils.d("Invite send failed...!");
			}
		}
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Utils.d("Invites is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Activity activity = null;
	private static Context context = null;
	private static Invites mInstance = null;

	private FirebaseApp mFirebaseApp = null;
}

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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.*;

import org.godotengine.godot.auth.Auth;
import org.godotengine.godot.Dictionary;

public class Firestore {

	public static Firestore getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Firestore(p_activity);
		}

		return mInstance;
	}

	public Firestore(Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;

		// Enable Firestore logging
		FirebaseFirestore.setLoggingEnabled(true);
		db = FirebaseFirestore.getInstance();

		Utils.d("Firestore::Initialized");
	}

	public void loadDocuments (final String p_name) {
		Utils.d("Firestore::LoadData");

		db.collection(p_name).get()
		.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if (task.isSuccessful()) {
					JSONObject jobject = new JSONObject();

					try {
						for (DocumentSnapshot document : task.getResult()) {
							jobject.put(
							document.getId(), document.getData());
						}

						Utils.d("Data: " + jobject.toString());
						Utils.callScriptFunc(
						"Firestore", "Documents", jobject.toString());
					} catch (JSONException e) {
						Utils.d("JSON Exception: " + e.toString());
					}
				} else {
					Utils.w("Error getting documents: " + task.getException());
				}
			}
		});
	}

	public void addDocument (final String p_name, final Dictionary p_dict) {
		Utils.d("Firestore::AddData");

		// Add a new document with a generated ID
		db.collection(p_name)
		.add(p_dict) // AutoGrenerate ID use .document("name").set(p_dict)
		.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
			@Override
			public void onSuccess(DocumentReference documentReference) {
				Utils.d("DocumentSnapshot added with ID: " + documentReference.getId());
				Utils.callScriptFunc("Firestore", "DocumentAdded", true);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Utils.w("Error adding document: " + e);
				Utils.callScriptFunc("Firestore", "DocumentAdded", false);
			}
		});
	}

	public void setData(final String p_col_name, final String p_doc_name, final Dictionary p_dict) {
		db.collection(p_col_name).document(p_doc_name)
		.set(p_dict, SetOptions.merge())
		.addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Utils.d("DocumentSnapshot successfully written!");
				Utils.callScriptFunc("Firestore", "DocumentAdded", true);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Utils.w("Error adding document: " + e);
				Utils.callScriptFunc("Firestore", "DocumentAdded", false);
			}
		});

	}

	private FirebaseFirestore db = null;
	private static Activity activity = null;
	private static Firestore mInstance = null;

	private FirebaseApp mFirebaseApp = null;
}

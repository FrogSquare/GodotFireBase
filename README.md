# GodotFireBase

GodotFireBase is a Firebase integration module for Godot Android.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://github.com/FrogSquare/GodotFireBase)
[![GodotEngine](https://img.shields.io/badge/Godot_Engine-2.X%20/%203.X-blue.svg)](https://github.com/godotengine/godot)
[![LICENCE](https://img.shields.io/badge/License-Apache_V2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![PATREON](https://img.shields.io/badge/Patreon-support-yellow.svg)](https://www.patreon.com/bePatron?u=5130479)

# Depends on

> Godot game engine: `git clone https://github.com/godotengine/godot`

> GodotSQL: `git clone https://github.com/FrogSquare/GodotSQL`

# Available Features

> AdMob

> Analytics

> Authentication [W.I.P] Google, Facebook, Twitter

> Firebase Notification

> RemoteConfig

> Storage

> Firestore (W.I.P)

# Build/Compile module

* Copy your `google-services.json` file to `[GODOT-ROOT]/platform/android/java/`
* Edit file modules/FireBase/config.py at line 11

```
p_app_id = "com.your.appid"     # config.py L:11
```

* Replace `com.your.appid` with you android application id.

* To build Admob you need to set your AdMob Application ID in the `AndroidManifestChunk.xml` file. To do this, edit the file `android_src/AndroidManifestChunk.xml`. Navigate to the end of the file. Replace the **value** for APPLICATION_ID with your real application ID.


*For customizing the module go [here](https://github.com/FrogSquare/GodotFireBase/wiki/Customize-GodotFireBase)*

# FAQ

**Should I rename the android_src folder after customization?**

> No, After customization the folder used by the module will be `android`, And `android_src` folder will be a backup for future customization.

# Initialize FireBase

Edit engine.cfg and add

```
[android]
modules="org/godotengine/godot/FireBase,org/godotengine/godot/SQLBridge"
```

RemoteConfigs default parameters `.xml` file is at `[GODOT-ROOT]/modules/FireBase/res/xml/remote_config_defaults.xml`

# GDScript - getting module singleton and initializing;

### On 2.X

```
var firebase = Globals.get_singleton("FireBase");
```

### On 3.X (latest from git)

```
var firebase = Engine.get_singleton("FireBase");
```

For Analytics only `firebase.init("", get_instance_ID())` or to user RemoteConfig or Notifications (subscribing to topic)

# GodotFireBase: copy `godot-firebase-config.json` to your projects root directord.
GodotFireBase config file, By default every feature is disabled.

```
{
	"AdMob" : true,
	"Authentication" : true,
	"RemoteConfig" : true,
	"Notification" : true,
	"Storage" : true,
	"Firestore" : true,

	"AuthConf" : 
	{
		"Google" : true,
		"Twitter" : true,
		"Facebook" : true,
		"FacebookAppId" : "1234566789875"
	},

	"Ads" : 
	{
		"AppId": "YOUR_APP_ID_HERE",
		"BannerAd" : true,
		"BannerGravity" : "BOTTOM",
		"BannerAdId" : "",

		"InterstitialAd" : true,
		"InterstitialAdId" : "",

		"RewardedVideoAd" : true,
		"RewardedVideoAdId" : "",

        "TestAds" : false
	}
}
```

And initialize firebase with file path, `RewardedVideoAdId` is a string array i.e `"string1,string2"`

```
func _ready():
    if OS.get_name() == "Android":
        firebase.initWithFile("res://godot-firebase-config.json", get_instance_ID())

func _receive_message(tag, from, key, data):
    if tag == "FrogSquare":
        print("From: ", from, " Key: ", key, " Data: ", data)
```

# Using FireBase Analytics

```
firebase.send_events("EventName", Dictionary)
firebase.send_custom("TestKey", "SomeValue")

firebase.setScreenName("Screen_name")
firebase.sendAchievement("someAchievementId")		# unlock achievement
firebase.join_group("clan_name")			# join clan/group
firebase.level_up("character_name", level)		# send character level
firebase.post_score("charcter name", level, score)	# post your score
firebase.earn_currency("currency", amount);		# when play earn some virtual currency gold/Diamond/any
firebase.spend_currency("item_id", "currency", amount)	# when user spend virtual currency

firebase.tutorial_begin()				# tutorial begin
firebase.tutorial_complete()				# tutorial end

Reference: https://support.google.com/firebase/answer/6317494?hl=en
```

# AlertDialog aditional

```
firebase.alert("Message goes here..!") # Show a simple AlertDialog
firebase.set_debug(true) # Enable/Disable `GodotFireBase` debug messages
```

# Authentication

For Facebook edit `res/values/ids.xml` and replace facebook_app_id with your Facebook App Id

```
firebase.authConfig("'Google':true,'Facebook':true") # Configure Auth service

firebase.get_id_token()

firebase.google_sign_in() # Firebase connect to google.
firebase.facebook_sign_in() # Firebase connect to facebook.
firebase.twitter_sign_in() # Firebase connect to twitter.
firebase.anonymous_sign_in() # Firebase connect anonymously.

firebase.google_sign_out() # Firebase disconnect from google.
firebase.facebook_sign_out() # Firebase disconnect from facebook.
firebase.twitter_sign_out() # Firebase disconnect from twitter.
firebase.anonymous_sign_out() # Firebase disconnect anonymously.

var gUserDetails = firebase.get_google_user() # returns name, email_id, photo_uri
var fbUserDetails = firebase.get_facebook_user() # returns name, email_id, photo_uri

firebase.google_revoke_access()
firebase.facebook_revoke_access()

firebase.is_google_connected() # bool check for google authentication (google)
firebase.is_facebook_connected() # bool check for facebook authentication (facebook)
firebase.is_anonymous_connected() # bool check for facebook authentication (anonymous)
```

More for facebook permissions

```
firebase.facebook_has_permission("publish_actions") # Check for availabe permission

firebase.revoke_facebook_permission("publish_actions") # revoke permission

firebase.ask_facebook_publish_permission("publish_actions"); # asking write permission

firebase.ask_facebook_read_permission("email"); # asking read only permission

firbase.get_facebook_permissions() # getting available permissions
```

Recive message from java

```
func _receive_message(tag, from, key, data):
    if tag == "FrogSquare":
        if from == "Auth":
            if key == "GoogleLogin" && data == "true": print("User Signed in.")
            if key == "FacebookLogin" && data == "true": print("User Signed in.")
```

# Firebase Notification API

```
firebase.subscribeToTopic("topic") # Subscribe to particular topic.
firebase.getToken() # Get current client TokenID

If recived notifiction has a payload, it will be saved inside SQL Database under key: "firebase_notification_data"

firebase.notifyInMins("message", 60) # Shedule notification in 60 min
firebase.notifyInSecs("message", 3200) # Shedule notification in 3200 seconds

var dict = {}
dict["title"] = "Notification title"
dict["message"] = "This is a text message"
dict["image_uri"] = "res://big_image_in_notification_body.png"
dict["type"] = "text"

firebase.notifyOnComplete(dict, 3200) # Shedule notification in 3200 seconds
```

# RemoteConfig API

```
firebase.getRemoteValue("remote_key") # Return String value
```

# Settings RemoteConfig default values

```
var defs = Dictionary()
defs["some_remoteconfig_key1"] = "remote_config_value1"
defs["some_remoteconfig_key2"] = "remote_config_value2"

firebase.setRemoteDefaults(defs.to_json())
```

OR load from json file

```
firebase.setRemoteDefaultsFile("res://path/to/jsonfile.json")
```

# Firebase Storage

```
# Upload Files from sdcard
firebase.upload("images/file", "destFolder") # uploads file from sdcard to firebase

# Download Files from Firebase
firebase.download("file", "images") # Saves file from firebase to sdcard
```

# Firebase AdMob

```
firebase.is_banner_loaded()     # Returns `true` if banner is loaded
firebase.is_interstitial_loaded() # Returns `true` if interstitial is loaded

firebase.show_banner_ad(true)	# Show Banner Ad
firebase.show_banner_ad(false)	# Hide Banner Ad
firebase.set_banner_unitid("unit_id") # Change current Ad unit ID

firebase.show_interstitial_ad() # Show Interstitial Ad
firebase.show_rewarded_video()	# Show Rewarded Video Ad
firebase.show_rvideo("unit_id") # Show Rewarded Video Ad

firebase.request_rewarded_video_status() # Request the rewarded video status
```

AdMob Recive message from java

```
func _receive_message(tag, from, key, data):
    if tag == "FrogSquare" and from == "AdMob":
        if key == "AdMobReward":
            # when rewared video play complete
            print("json data with [RewardType & RewardAmount]: ", data);

        elif key == "AdMob_Video":
            # when rewarded video loaded
            # data will be `loaded` or `load_failed and `loaded` or `not_loaded` with `firebase.request_rewarded_video_status()`
            print("AdMob rewarded video status is ", data);

        elif key == "AdMob_Banner":
            # when banner loaded
            # data will be `loaded` or `load_failed`
            print("Banner Status: ", data);

        elif key == "AdMob_Interstitial" and data == "loaded":
            # when Interstitial loaded
            # data will be `loaded` or `load_failed`
            print("Interstitial Status: ", data);
```

# Firebase Firestore

```
firebase.add_document("collection_name", dict) # Auto created new Document under collection_name
firebase.set_document("collection_name", "document_name", data) # Set document data, Data's are merged by default
firebase.load_document("collection_name") # load or retrive from the server,

# Note: documents will be sent to the `_receive_message` function as json
```

# Note

While exporting, don't forget to add `*.json` under Resources tab,
![alt text](http://preview.ibb.co/fTwC8Q/Screenshot_from_2017_06_17_18_44_25.png)

# Log Event

```
adb -d logcat godot:V FrogSquare:V DEBUG:V AndroidRuntime:V ValidateServiceOp:V *:S
```

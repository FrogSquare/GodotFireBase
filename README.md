## GodotFireBase
Godot_FireBase is a firebase integration for godot android;

# Available Features
> Analytics

> Firebase Notification

# Build/Compile module
copy your `google-services.json` file to `[GODOT-ROOT]\platform/android/java\` and edit file modules/FireBase/config.py at line 17
```
env.android_add_default_config("applicationId 'com.your.appid'")
```
replay `com.your.appid` with you android application id.

# Initialize FireBase

edit engine.cfg and add
```
[android]
modules="org/godotengine/godot/FireBase"
```

# GDScript - getting module singleton and initializing;
```
firebase = Globals.get_singleton("FireBase");
firebase.init();
```
# Using FireBase Analytics
```
firebase.sendCustom("TestKey", "SomeValue");
firebase.setScreenName("Screen_name");
firebase.sendAchievement("someAchievementId");
```

# AlertDialog aditional;
```
firebase.alert("Message goes here..!");
```

# Subscribe to Firebase topic
```
firebase.subscribeToTopic("topic");
```

# get firebase token
```
firebase.getToken();
```

# Log FireBase Events

```
adb shell setprop log.tag.FA VERBOSE
adb shell setprop log.tag.FA-SVC VERBOSE
adb shell setprop debug.firebase.analytics.app {org.example.appname}
adb logcat -v time -s FA FA-SV
```

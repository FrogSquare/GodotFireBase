## GodotFireBase
Godot_FireBase is a firebase integration for godot android;

# Available Features
> Analytics

Build/Compile module
Edit file modules/FireBase/config.py Line 17 with
```
env.android_add_default_config("applicationId 'com.your.appid'")
```

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
firebase.sendAchivement("someAchivementId");
```

# AlertDialog aditional;
```
firebase.alert("Message goes here..!");
```

# Log FireBase Events

```
adb shell setprop log.tag.FA VERBOSE
adb shell setprop log.tag.FA-SVC VERBOSE
adb shell setprop debug.firebase.analytics.app {org.example.appname}
adb logcat -v time -s FA FA-SV
```

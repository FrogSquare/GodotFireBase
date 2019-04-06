
import sys
import json
import os
import re
import shutil

from colors import *
from helper import *

# Set your Android app ID
p_app_id = "com.your.id"

# Update this to customize the module
_config = {
"Analytics"      : True,
"AdMob"          : True,
"Invites"        : True,
"RemoteConfig"   : True,
"Notification"   : True,
"Storage"        : False,
"Firestore"      : True,

"Authentication" : True,
"AuthGoogle"     : True,
"AuthFacebook"   : False,
"AuthTwitter"    : False
}

def can_build(env_plat, plat = None):
    #return False
    if plat == None:
        #print("`GodotFireBase`"+RED+" master "+RESET+" branch not compatable with godot 2.X")
        #print("Try using `GodotFireBase` "+GREEN+" 2.X "+RESET+" branch for Godot 2.X")

        if isinstance(env_plat, basestring):
            plat = env_plat
        else:
            print("GodotFireBase: "+RED+" Platform not set, Disabling GodotFireBase "+RESET)
            print("GodotFireBase: "+RED+" To use `GodotFireBase` in Godot 2.X copy the `build.gradle.template` from Godot 3.X and place it in `platform/android/`"+RESET)
            return False

    if plat == "android":
        print("GodotFireBase: " + GREEN + "Enabled" + RESET)
        return True
    else:
        print("GodotFireBase: " + RED + "Disabled" + RESET)
        return False
    pass   

def copytree(src, dst, symlinks=False, ignore=None):
    for item in os.listdir(src):
        if not os.path.exists(dst): os.makedirs(dst)

        s = os.path.join(src, item)
        d = os.path.join(dst, item)

        if os.path.isdir(s): shutil.copytree(s, d, symlinks, ignore)
        else: shutil.copyfile(s, d)
    pass

def parse_file_data(file_data, regex_list, file_type = "Java"):
    final_data = [];

    for rr in regex_list:
        re_start = rr[0]
        re_stop = rr[1]

        # print("Using Regex: " + re_start);

        skip_line = False
        blank_line = False;

        for line in file_data:
            if re_start.search(line) and not skip_line:
                skip_line = True
                continue
            elif re_stop.search(line) and skip_line:
                skip_line = False
                continue
            elif empty_line.match(line):
                blank_line = True;
                continue

            if blank_line and len(final_data) > 0:
                if final_data[-1] != "\n": final_data.append("\n");
                blank_line = False;

            if not skip_line: final_data.append(line);

        file_data = final_data;
        final_data = []

    return file_data;
    pass

def parse_java_file(p_file_src, p_file_dst, p_regex_list):
    p_file_data = []

    try:
        with open(p_file_src, 'r') as file_in:
            p_file_data = file_in.readlines()
    except (OSError, IOError): return res

    out_file = open(p_file_dst, 'w')
    p_file_data = parse_file_data(p_file_data, p_regex_list, "JAVA")
    out_file.write("".join(p_file_data))
    out_file.close()
    pass

def update_module(env):
    src_dir = os.path.dirname(os.path.abspath(__file__)) + "/android_src/"
    target_dir = os.path.dirname(os.path.abspath(__file__)) + "/android/"

    if os.path.exists(target_dir):
        shutil.rmtree(target_dir)

    if not os.path.exists(target_dir):
        os.makedirs(target_dir)

    _config["Auth"] = _config["Authentication"]

    if (_config["Storage"] or _config["Firestore"]) and not _config["Auth"]:
        sys.stdout.write(RED)
        print("Storage/Firestore needs FireBase Authentication, Skipping `GodotFireBase` module")
        sys.stdout.write(RESET)

        return False

    data_to_check = \
    ["Analytics", "AdMob", "Auth", "Invites", "Notification", "RemoteConfig",\
    "Storage", "Firestore", "AuthFacebook", "AuthGoogle", "AuthTwitter"]

    regex_list = []

    for _file in FILES_LIST["Base"]: shutil.copyfile(src_dir+_file, target_dir+_file)

    if not _config["Auth"]:
        _config["AuthGoogle"] = False
        _config["AuthFacebook"] = False
        _config["AuthTwitter"] = False

    dbg_msg = ""
    for d in data_to_check:
        if d == "AdMob":
            if any(elem in env.module_list for elem in ["GodotAds"]):
                _config[d] = False
        elif d == "AuthGoogle":
            if any(elem in env.module_list for elem in ["GodotGoogleService"]):
                _config[d] = False

        if not _config[d]:
            regex_list.append(\
            [re.compile(r'([\/]+'+d+'[\+]+)'), re.compile(r'([\/]+'+d+'[\-]+)')])
        else:
            dbg_msg += " %s," % d

            if d != "Storage":
                if d == "Auth":
                    if not os.path.exists(target_dir+"auth/"): os.makedirs(target_dir+"auth/")
                for files in FILES_LIST[d]:
                    if d == "Auth" or (d.startswith("Auth")):
                        shutil.copyfile(src_dir+"auth/"+files, target_dir+"auth/"+files)
                    else: shutil.copyfile(src_dir+files, target_dir+files)
            else: copytree(src_dir+d.lower(), target_dir+d.lower())

    print("GodotFireBase: [" + dbg_msg[1:-1] + "]")

    # Copy FireBase.java file into memory
    parse_java_file(src_dir+"FireBase.java", target_dir+"FireBase.java", regex_list)

    if _config["Auth"] and (not _config["AuthGoogle"] or not _config["AuthFacebook"] or not _config["AuthTwitter"]):
        parse_java_file(src_dir+"auth/Auth.java", target_dir+"auth/Auth.java", regex_list)

    # Parsing AndroidManifest
    regex_list = []

    for d in data_to_check:
        if not _config[d]:
            regex_list.append(\
            [re.compile(r'(<\![\-]+ '+d+' [\-]+>)'), re.compile(r'(<\![\-]+ '+d+' [\-]+>)')])

    out_file = open(target_dir+"AndroidManifestChunk.xml", 'w')
    file_data = []

    try:
        with open(src_dir+"AndroidManifestChunk.xml", 'r') as file_in:
            file_data = file_in.readlines()
    except (OSError, IOError): return res

    file_data = parse_file_data(file_data, regex_list, "XML")

    out_file.write("".join(file_data))
    out_file.close()

    return True

def implement(api, support=True):
    supportv4 = "{exclude group: 'com.android.support' exclude module: 'support-v4'}"
    return "implementation('"+api+"')" + (supportv4 if support else "")
    pass

def configure(env):
    global p_app_id
    if env["platform"] == "android":
        if (not update_module(env)):
            print("Error updating module.")
            return

        if env.get("application_id", None) != None:
            p_app_id = env["application_id"]

        env.android_add_maven_repository("url 'https://maven.fabric.io/public'")
        env.android_add_maven_repository("url 'https://maven.google.com'")
        env.android_add_maven_repository(\
        "url 'https://oss.sonatype.org/content/repositories/snapshots'")

        env.android_add_gradle_classpath("com.google.gms:google-services:4.1.0")
        env.android_add_gradle_plugin("com.google.gms.google-services")

        env.android_add_dependency("implementation 'com.android.support:support-annotations:25.0.1'")
        env.android_add_dependency(implement("com.google.firebase:firebase-core:16.0.7"))
        env.android_add_dependency(implement("com.google.android.gms:play-services-measurement-base:16.0.0"))

        if _config["Auth"]:
            env.android_add_dependency(implement("com.google.firebase:firebase-auth:16.1.0"))
            if _config["AuthGoogle"]:
                env.android_add_dependency(implement("com.google.android.gms:play-services-auth:16.0.1"))

            if _config["AuthFacebook"]:
                env.android_add_dependency(implement("com.facebook.android:facebook-android-sdk:4.18.0", False))

            if _config["AuthTwitter"]:
                env.android_add_dependency(\
                "implementation('com.twitter.sdk.android:twitter-core:1.6.6@aar') { transitive = true }")
                env.android_add_dependency(\
                "implementation('com.twitter.sdk.android:twitter:1.13.1@aar') { transitive = true }")

        if _config["AdMob"]:
            if any(elem in env.module_list for elem in ["GodotAds"]): pass
            else:
                env.android_add_dependency(implement("com.google.firebase:firebase-ads:17.1.3"))

        if _config["RemoteConfig"]:
            env.android_add_dependency(implement("com.google.firebase:firebase-config:16.3.0"))

        if _config["Notification"]:
            env.android_add_dependency(implement("com.google.firebase:firebase-messaging:17.3.4"))
            env.android_add_dependency(implement("com.firebase:firebase-jobdispatcher:0.8.5"))

        if _config["Invites"]:
            env.android_add_dependency(implement("com.google.firebase:firebase-invites:16.1.0"))

        if _config["Storage"]:
            env.android_add_dependency(implement("com.google.firebase:firebase-storage:16.0.5"))

        if _config["Firestore"]:
            env.android_add_dependency(implement("com.google.firebase:firebase-firestore:18.0.1"))

        env.android_add_dependency("implementation 'commons-codec:commons-codec:1.10'")

        env.android_add_java_dir("android");
        env.android_add_res_dir("res");

        if "frogutils" in [os.path.split(path)[1] for path in env.android_java_dirs]: pass
        else: env.android_add_java_dir("frogutils");

        env.android_add_to_manifest("android/AndroidManifestChunk.xml");
        env.android_add_to_permissions("android/AndroidPermissionsChunk.xml");
        env.android_add_default_config("minSdkVersion 18")
        env.android_add_default_config("applicationId '"+ p_app_id +"'")

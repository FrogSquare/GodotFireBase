
import json
import os
import re
import shutil

# Update this to customize the module
_config = {
"Analytics"      : True,
"AdMob"          : True,
"Invites"        : True,
"RemoteConfig"   : True,
"Notification"   : True,
"Storage"        : True,
"Firestore"      : False,

"Authentication" : True,
"AuthGoogle"     : True,
"AuthFacebook"   : True,
"AuthTwitter"    : True
}

FILES_LIST		= \
{
"AdMob"		: ["AdMob.java"],
"Analytics"	: ["Analytics.java"],
"Auth"		: ["AnonymousAuth.java", "Auth.java", "EmailAndPassword.java"],
"Base"		: ["Config.java", "FireBase.java", "Utils.java", "AndroidPermissionsChunk.xml"],
"Invites"	: ["Invites.java"],
"Notification"	: ["MessagingService.java", "Notification.java", \
                   "NotifyInTime.java", "InstanceIDService.java"],
"RemoteConfig"	: ["RemoteConfig.java"],
"Storage"	: ["storage/"],
"Firestore"	: ["Firestore.java"],

"AuthGoogle"    : ["GoogleSignIn.java"],
"AuthFacebook"  : ["FacebookSignIn.java"],
"AuthTwitter"   : ["TwitterSignIn.java"],
}

directory = "android"
empty_line = re.compile(r'^\s*$')

def can_build(plat):
    return update_module() if plat == "android" else False

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

def update_module():
    src_dir = os.path.dirname(os.path.abspath(__file__)) + "/android_src/"
    target_dir = os.path.dirname(os.path.abspath(__file__)) + "/android/"

    if os.path.exists(target_dir):
        shutil.rmtree(target_dir)

    if not os.path.exists(target_dir):
        os.makedirs(target_dir)

    _config["Auth"] = _config["Authentication"]

    if (_config["Storage"] or _config["Firestore"]) and not _config["Auth"]:
        print("Storage/Firestore needs FireBase Authentication, Skipping `GodotFireBase` module")
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

    for d in data_to_check:
        if not _config[d]:
            regex_list.append(\
            [re.compile(r'([\/]+'+d+'[\+]+)'), re.compile(r'([\/]+'+d+'[\-]+)')])
        else:
            if d != "Storage":
                if d == "Auth":
                    if not os.path.exists(target_dir+"auth/"): os.makedirs(target_dir+"auth/")
                for files in FILES_LIST[d]:
                    if d == "Auth" or (d.startswith("Auth")):
                        shutil.copyfile(src_dir+"auth/"+files, target_dir+"auth/"+files)
                    else: shutil.copyfile(src_dir+files, target_dir+files)
            else: copytree(src_dir+d.lower(), target_dir+d.lower())

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

def configure(env):
    if env["platform"] == "android":
        env.android_add_maven_repository("url 'https://maven.fabric.io/public'")
        env.android_add_maven_repository("url 'https://maven.google.com'")
        env.android_add_maven_repository(\
        "url 'https://oss.sonatype.org/content/repositories/snapshots'")

        env.android_add_gradle_classpath("com.google.gms:google-services:3.1.1")
        env.android_add_gradle_plugin("com.google.gms.google-services")

        env.android_add_dependency("compile 'com.android.support:support-annotations:25.0.1'")
        env.android_add_dependency("compile 'com.google.firebase:firebase-core:11.6.0'")

        if _config["Auth"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-auth:11.6.0'")
            if _config["AuthGoogle"]:
                env.android_add_dependency("compile 'com.google.android.gms:play-services-auth:11.6.0'")

            if _config["AuthFacebook"]:
                env.android_add_dependency("compile 'com.facebook.android:facebook-android-sdk:4.18.0'")

            if _config["AuthTwitter"]:
                env.android_add_dependency(\
                "compile('com.twitter.sdk.android:twitter-core:1.6.6@aar') { transitive = true }")
                env.android_add_dependency(\
                "compile('com.twitter.sdk.android:twitter:1.13.1@aar') { transitive = true }")

        if _config["AdMob"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-ads:11.6.0'")

        if _config["RemoteConfig"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-config:11.6.0'")

        if _config["Notification"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-messaging:11.6.0'")
            env.android_add_dependency("compile 'com.firebase:firebase-jobdispatcher:0.5.2'")

        if _config["Invites"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-invites:11.6.0'")

        if _config["Storage"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-storage:11.6.0'")

        if _config["Firestore"]:
            env.android_add_dependency("compile 'com.google.firebase:firebase-firestore:11.6.0'")

        env.android_add_dependency("compile 'commons-codec:commons-codec:1.10'")

        env.android_add_java_dir("android");
        env.android_add_res_dir("res");
        env.android_add_to_manifest("android/AndroidManifestChunk.xml");
        env.android_add_to_permissions("android/AndroidPermissionsChunk.xml");
        env.android_add_default_config("minSdkVersion 15")
        env.android_add_default_config("applicationId 'com.froglogics.dotsndots'")

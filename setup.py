#!/usr/bin/env python3

import json
import os
import re
import shutil

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

"AuthGoogle" : ["GoogleSignIn.java"],
"AuthFacebook" : ["FacebookSignIn.java"],
"AuthTwitter" : ["TwitterSignIn.java"],
}

directory = "android_new";
empty_line = re.compile(r'^\s*$');

def copytree(src, dst, symlinks=False, ignore=None):
    for item in os.listdir(src):
        if not os.path.exists(dst): os.makedirs(dst)

        s = os.path.join(src, item)
        d = os.path.join(dst, item)

        if os.path.isdir(s): shutil.copytree(s, d, symlinks, ignore)
        else: shutil.copyfile(s, d)

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

def main():
    if os.path.exists(directory):
        shutil.rmtree(directory);

    if not os.path.exists(directory):
        os.makedirs(directory)

    # regex = re.compile(r'^([\/]+[a-z-A-Z]+[\+]+)')
    # Read json here..!
    data = {}

    try:
        with open("godot-firebase-config.json", 'r') as file_in:
            data = json.load(file_in);
    except (OSError, IOError): return res
    finally:
        data["Auth"] = data["Authentication"]

    if not data:
        print("Json error..!")
        return

    data_to_check = \
    ["Analytics", "AdMob", "Auth", "Invites", "Notification", "RemoteConfig",\
    "Storage", "Firestore", "AuthFacebook", "AuthGoogle", "AuthTwitter"];

    regex_list = []

    for files in FILES_LIST["Base"]: shutil.copyfile("android/"+files, directory+"/"+files);

    if data["Storage"] and not data["Auth"]:
        print("Activating FireBase Auth to support Storage...!");
        data["Auth"] = True;

    if not data["Auth"]:
        data["AuthGoogle"] = False
        data["AuthFacebook"] = False
        data["AuthTwitter"] = False
	
    for d in data_to_check:
        if not data[d]:
            regex_list.append(\
            [re.compile(r'([\/]+'+d+'[\+]+)'), re.compile(r'([\/]+'+d+'[\-]+)')]);
        else:
            if d != "Storage":
                if d == "Auth":
                    if not os.path.exists(directory+"/auth/"): os.makedirs(directory+"/auth/")
                for files in FILES_LIST[d]:
                    if d == "Auth" or (d.startswith("Auth")):
                        shutil.copyfile("android/auth/"+files, directory+"/auth/"+files)
                    else: shutil.copyfile("android/"+files, directory+"/"+files)
            else: copytree("android/"+d.lower(), directory+"/"+d.lower())

    # Copy FireBase.java file into memory
    parse_java_file("android/FireBase.java", "android_new/FireBase.java", regex_list)

    if data["Auth"] and (not data["AuthGoogle"] or not data["AuthFacebook"] or not data["AuthTwitter"]):
        parse_java_file("android/auth/Auth.java", "android_new/auth/Auth.java", regex_list)

    """
    out_file = open("android_new/FireBase.java", 'w')
    file_data = parse_file_data(file_data, regex_list, "JAVA");
    out_file.write("".join(file_data));
    out_file.close();
    """

    # Parsing AndroidManifest
    regex_list = [];

    for d in data_to_check:
        if not data[d]:
            regex_list.append(\
            [re.compile(r'(<\![\-]+ '+d+' [\-]+>)'), re.compile(r'(<\![\-]+ '+d+' [\-]+>)')]);

    out_file = open("android_new/AndroidManifestChunk.xml", 'w')
    file_data = []

    try:
        with open("android/AndroidManifestChunk.xml", 'r') as file_in:
            file_data = file_in.readlines();
    except (OSError, IOError): return res

    file_data = parse_file_data(file_data, regex_list, "XML");

    out_file.write("".join(file_data));
    out_file.close();

    # Parsing config.py
    regex_list = [];

    for d in data_to_check:
        if not data[d]:
            regex_list.append(\
            [re.compile(r'([\#]{2}'+d+'[\+]{2})'), re.compile(r'([\#]{2}'+d+'[\-]{2})')]);

    # Original config.py BackUp
    out_file = open("config.py", 'w')
    file_data = []

    try:
        with open("config.py.back", 'r') as file_in:
             file_data = file_in.readlines();
    except (OSError, IOError): return res

    file_data = parse_file_data(file_data, regex_list, "PY")
    file_data[-7] = '\t\tenv.android_add_java_dir("android_new");\n'

    out_file.write("".join(file_data))
    out_file.close()
    pass

if __name__ == "__main__":
    main();

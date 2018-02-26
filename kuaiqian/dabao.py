#!/usr/bin/python	
# -*- coding: utf-8 -*-

import os
import shutil
import random
import string

rootpath = "D:\git\siteFengZhuang\kuaiqian\\"

# rootpath = "E:\studio-projects\siteFengZhuang\kuaiqian\\"

new_app = "newapp"
base = "base\\"

os.chdir(rootpath)

def file_extension(path):
	return os.path.splitext(path)[1] 

def copyRes(from_name,to_name):
	

	drawable_path = to_name+"\\res\\drawable-xhdpi"
	values_path = to_name+"\\res\\values"
	os.makedirs(drawable_path)
	os.makedirs(values_path)

	for file_name in os.listdir(from_name):
		ext = file_extension(file_name)
		
		if "jpg" in ext or "png" in ext:
			
			shutil.copyfile(from_name+"\\"+file_name,drawable_path+"\\"+file_name)
		elif "xml" in ext:
			
			shutil.copyfile(from_name+"\\"+file_name,values_path+"\\"+file_name)
		pass
	
	# if tiantianhongbao==1 :
	# 	tiantianhongbao(from_name,drawable_path)
	# pass

def tiantianhongbao(from_name,to_name):
	# shutil.copyfile(base+"back_hb.png",to_name+"\\back_hb.png")
	# shutil.copyfile(base+"home_hb.png",to_name+"\\home_hb.png")
	pass


def randomStr(start,end):
	num = random.randint(start,end)
	salt = ''.join(random.sample(string.ascii_letters, num))
	return salt

def update_gradle_file(new_name):

	file = open("app\\build.gradle",'r',0)
	lines = file.read()
	lines = lines.replace("//----","        "+new_name+" {\n            applicationId = \"com."+randomStr(4,6).lower()+"."+new_name+"\"\n        }\n//----")
	file = open("app\\build.gradle",'w',0)
	file.write(lines)
	file.flush()
	file.close()
	
	pass

build_string = "gradlew clean"

for dir_name in os.listdir(new_app) :
	print dir_name
	new_name = dir_name
	if not os.path.isdir(new_app+"\\"+dir_name) :
		continue
	if os.path.exists("app\src\\"+new_name) :
		for index in range(10000) : 
			new_name = dir_name+str(index)
			
			if not os.path.exists("app\src\\"+new_name) :
				break
			pass
		pass
	pass
	build_string = build_string + " assemble"+new_name.capitalize()+"Release"
	# input("config the strings,any key to continue....")
	print "new_name:"+new_name
	copyRes(new_app+"\\"+dir_name,"app\src\\"+new_name)
	update_gradle_file(new_name)
pass




# if daohang==1 :
# 	print "YES"
# else :
# 	print "NO"
# pass

print "build_string:"+build_string

os.system(build_string)

#!/bin/bash

# Usage:
# update [message] [-uv] [-v [version]]
# updates to git with [message]
# -uv -increments front-running version number
# -v updates version number to [version]

# First get version number from file version
#version_prefix=0
#version_suffix=0

while read line
do
	dot_index=`expr index "$line" .`
	
	dot_index=$dot_index-1
	version_prefix=${line:0:dot_index}
	
	dot_index=$dot_index+1
	version_suffix=${line:dot_index}
	
done < version

#get message
message=$1

#check if there are more flags
if  [ "$#" -gt 1 ]; then
	#get the second argument
	flag=$2
	if [ "$flag" = "-v" ]; then
		if [ "$#" -gt 2 ]; then
			new_version=$3
			dot_index=`expr index "$new_version" .`
			version_suffix=${new_version:dot_index}
			dot_index=$dot_index-1
			version_prefix=${new_version:0:dot_index}
		fi
	elif [ "$flag" = "-uv" ]; then
		let "version_prefix += 1"
	fi
else 
	let "version_suffix += 1"
fi

ver="v"$version_prefix"."$version_suffix
message="$ver $message"

git add *.java
git commit -m "$message"
git push web master

rm version
touch version
echo $ver > version


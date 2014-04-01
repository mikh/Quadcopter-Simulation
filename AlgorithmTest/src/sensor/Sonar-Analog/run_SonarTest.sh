#!/bin/bash
echo cape-bone-iio > /sys/devices/bone_capemgr.*/slots 
JAVA_HOME=/home/root/java/jdk1.7.0_51/
cd /home/root/sonar
/home/root/java/jdk1.7.0_51/bin/java SonarTest 2>> error.txt

# run pilot
stty -F /dev/ttyO4 raw
stty -F /dev/ttyO4 9600
JAVA_HOME=/home/root/java/jdk1.7.0_51/
rm /var/lock/LCK..ttyO4
cd /home/root/sensor_manager
/home/root/java/jdk1.7.0_51/bin/java -cp .:/usr/share/java/rxtx.jar -Djava.library.path=/usr/lib/jni/ -Dgnu.io.rxtx.SerialPorts=/dev/ttyO4 Quadcopter 2>> error.txt

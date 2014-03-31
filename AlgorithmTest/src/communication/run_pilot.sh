stty -F /dev/ttyO4 raw
stty -F /dev/ttyO4 9600
echo "starting operation " > /dev/ttyO4
#echo $PATH > /dev/ttyO4
JAVA_HOME=/home/root/java/jdk1.7.0_51/
echo $JAVA_HOME > /dev/ttyO4

cd /home/root/pilot
/home/root/java/jdk1.7.0_51/bin/java -cp .:/usr/share/java/rxtx.jar -Djava.library.path=/usr/lib/jni/ -Dgnu.io.rxtx.SerialPorts=/dev/ttyO4 Pilot 2>> error.txt

#send sonar gpio to bbb
scp SonarGPIOSensorInterface.java sonar_gpio.c compile_c_gpio.sh run_c_gpio.sh clean.sh root@192.168.7.2:~/sonar_gpio

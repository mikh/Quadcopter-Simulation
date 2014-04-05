//  
 // BB-DRO.c - Read iGaging Scales on a BeagleBone Black  
 //  
 // Copyright (C) 2013, Exadler Technologies Inc.  
 // http://www.exadler.com  
 //  
 // Based on a similar work (ArduinoDRO_V2_2) by Yuriy Krushelnytskiy  
 // http://www.yuriystoys.com  
 //  
 // This code is is licensed under   
 // Creative Commons Attribution-ShareAlike 3.0 Unported License.   
 //   
 //  
 //  
 #include <fcntl.h>  
 #include <unistd.h>  
 #include <stdio.h>  
 #include <stdlib.h>  
 #include <string.h>  
 //  
 // Setup GPIO and return a unix file descriptor handle to  
 // that GPIO. The user must close that file descriptor when finished  
 //   
 int setupGPIO(char *pszGPIO, char *pszDir)  
 {  
   int fd;  
   int len;  
   int result;  
   char buf[120];  
   if (pszGPIO == NULL) {  
     printf("Error: pszGPIO is NULL!\n");  
     exit(1);  
   }  
   if (pszDir == NULL) {  
     printf("Error: pszDir is NULL!\n");  
     exit(1);  
   }  
   if ((strcmp(pszDir,"in") != 0) && (strcmp(pszDir,"out") != 0)) {  
     printf("Error: pszDir must be either 'in' or 'out'\n");  
     exit(1);  
   }  
   len = strlen(pszGPIO);  
   if ((len < 1) || (len > 40)) {  
     printf("Error: pszGPIO = %s is too long or too short\n",pszGPIO);  
     exit(1);  
   }  
   // First enable GPIO30  
   fd = open("/sys/class/gpio/export",O_WRONLY | O_APPEND);  
   if (fd == -1) {  
     printf("Cannot open /sys/class/gpio/export to setup GPIO\n");  
     exit(1);  
   }  
   result = write( fd, pszGPIO, len );  
   if (result != len) {  
     printf("Cannot write to /sys/class/gpio/export to setup GPIO %s\n",pszGPIO);  
     printf("Wrote %d bytes\n",result);  
     close(fd);  
     // Remove exiting here for now  
     // as it will fail if the GPIO was already exported  
     // so just ignore the error  
 //    exit(1);  
   }  
   close(fd);  
   // Now set GPIO 30 direction to output  
   sprintf(buf, "/sys/class/gpio/gpio%s/direction", pszGPIO);  
   fd = open(buf,O_WRONLY);  
   if (fd == -1) {  
     printf("Cannot open %s to setup GPIO\n",buf);  
     exit(1);  
   }  
   result = write( fd, pszDir, strlen(pszDir) );  
   if (result != strlen(pszDir)) {  
     printf("Cannot write to %s setup GPIO\n", buf);  
     close(fd);  
     exit(1);  
   }  
   close(fd);    
   // Now open the GPIO value for writing  
   sprintf(buf, "/sys/class/gpio/gpio%s/value", pszGPIO);  
   fd = open(buf,O_WRONLY);  
   if (fd == -1) {  
     printf("Cannot open %s to write to the GPIO\n", buf);  
     exit(1);  
   }  
   return fd;  
 }  
 //  
 // freeGPIO - release the GPIO port  
 //   
 void freeGPIO(char *pszGPIO)  
 {  
   int fd;  
   int len;  
   int result;  
   if (pszGPIO == NULL) {  
     printf("Error: pszGPIO is NULL!\n");  
     exit(1);  
   }  
   // unexport the GPIO  
   // First open the unexport file  
   fd = open("/sys/class/gpio/unexport",O_WRONLY );  
   if (fd == -1) {  
     printf("Cannot open /sys/class/gpio/unexport to free GPIO\n");  
     exit(1);  
   }  
   len = strlen(pszGPIO);  
   result = write( fd, pszGPIO, len );  
   if (result != len) {  
     printf("Cannot write to /sys/class/gpio/unexport to setup GPIO %s\n",pszGPIO);  
     printf("Wrote %d bytes\n",result);  
     close(fd);  
     // Remove exiting here for now  
     // as it will fail if the GPIO was already exported  
     // so just ignore the error  
 //    exit(1);  
   }  
   close(fd);  
 }  
 inline void tickTock(int fd)  
 {  
   int result;  
   int j;  
   if (fd == -1) {  
     printf("Error: tickTock called with bad fd\n");  
     exit(1);  
   }  
   for (j=0;j<1000;j++) ;  
   result = write( fd, "1" , 1);  
   if (result != 1) {  
     printf("Cannot write to fd to set GPIO\n");  
     close(fd);  
     exit(1);  
   }  
   for (j=0;j<1000;j++) ;  
   result = write( fd, "0" , 1);  
   if (result != 1) {  
     printf("Cannot write to fd to set GPIO\n");  
     close(fd);  
     exit(1);  
   }  
 }  
 int main(int argc, char *argv[])  
 {  
   int fd30;  
   // First enable GPIO30  
   fd30 = setupGPIO("30","out");  
   int i;  
   for (i=0; i < 10; i++) {  
     tickTock(fd30);  
   }  
   close(fd30);    
 //  freeGPIO("30");  
   return 0;  
 }  
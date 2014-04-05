#include <stdlib.h>
#include <sys/time.h>
#include <fcntl.h>  
#include <unistd.h>  
#include <stdio.h>  
#include <string.h>  
#include <sched.h>

//#define _POSIX_C_SOURCE >= 199309L

void delay(long microseconds);
int setupGPIO(char *pszGPIO, char *pszDir, int direction);
void freeGPIO(char *pszGPIO); 
void pulse_out(int fd, long microseconds); 
int pulse_in(int fd);
struct sched_param param;

int main(){
	param.sched_priority = sched_get_priority_max(SCHED_RR);
	if(sched_setscheduler(0, SCHED_RR, &param) != 0){
		printf("sched_setscheduler error");
		exit(EXIT_FAILURE);
	}
	int pin3, pin2;  
	// First enable GPIO30  
	printf("Enabling gpio port\n");
	pin3 = setupGPIO("3","out", 1);  
	pin2 = setupGPIO("2","in", 0);
	close(pin2);
	while(1){
		printf("PULSE\n");
		pulse_out(pin3, 10);
		pulse_in(pin2);
		//delay(100);
		sleep(1);
	}
	close(pin3);
	close(pin2);
	return 0;
}

void delay(long microseconds){
	nanosleep(microseconds);
	//usleep(microseconds);
	/*
	struct timeval start, duration;
	gettimeofday(&start, NULL);
	long us = start.tv_usec;
	gettimeofday(&duration, NULL);
	while(duration.tv_usec - us < microseconds){
		gettimeofday(&duration, NULL);
	}
	*/
		/*
	struct timespec start, duration;
	clock_gettime(CLOCK_REALTIME, &start);
	long ns = start.tv_nsec;
	microseconds *= 1000;
	clock_gettime(CLOCK_REALTIME, &duration);
	while(duration.tv_nsec - ns < microseconds){
		clock_gettime(CLOCK_REALTIME, &duration);
	}
	*/

/*
	struct timespec sleepTime;
	struct timespec returnTime;
	sleepTime.tv_sec = 0;
	returnTime.tv_nsec = microseconds*1000;
	while(1){
		nanosleep(&sleepTime, &returnTime);
	}
	*/
}



int setupGPIO(char *pszGPIO, char *pszDir, int direction)  
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
   if(direction){
	   sprintf(buf, "/sys/class/gpio/gpio%s/value", pszGPIO);  
	   fd = open(buf,O_WRONLY);  
	   if (fd == -1) {  
	     printf("Cannot open %s to write to the GPIO\n", buf);  
	     exit(1);  
	   }  
	} else{
	   sprintf(buf, "/sys/class/gpio/gpio%s/value", pszGPIO);  
	   fd = open(buf,O_RDONLY);  
	   if (fd == -1) {  
	     printf("Cannot open %s to write to the GPIO\n", buf);  
	     exit(1);  
	   } 
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

 void pulse_out(int fd, long microseconds)  
 {  
   int result;  
   int j;  
   if (fd == -1) {  
     printf("Error: pulseOut called with bad fd\n");  
     exit(1);  
   }  

   for (j=0;j<1000;j++);  
   
   result = write( fd, "1" , 1);  
   if (result != 1) {  
     printf("Cannot write to fd to set GPIO\n");  
     close(fd);  
     exit(1);  
   }  
   
   delay(microseconds);
  // for(j=0;j<100000;j++);
   result = write( fd, "0" , 1);  

   if (result != 1) {  
     printf("Cannot write to fd to set GPIO\n");  
     close(fd);  
     exit(1);  
   }  
 }  

int pulse_in(int fd){
	int value = 0;
	int start = 0,  end = 0;
	char ch;
	long cycles = 0;
	struct timeval s_time, e_time;
	//gettimeofday(&start, NULL);
    if (fd == -1) {  
    	printf("Error: pulseOut called with bad fd\n");  
    	exit(1);  
    }  

    
    while(!end){

		fd = open("/sys/class/gpio/gpio2/value", O_RDONLY);
		if (fd < 0) {
			perror("gpio/get-value");
			return fd;
		}
	
		read(fd, &ch, 1);
		close(fd);
		if (ch != '0') {
			value = 1;
			//printf("%d\n", value);
		} else {
			value = 0;
			//printf("%d\n", value);
		}
		//printf("%d\n", value);
		if(value && ! start){
			gettimeofday(&s_time, NULL);
			start = 1;
		} else if(!value && start){
			gettimeofday(&e_time, NULL);
			end = 1;
		}

		cycles++;
		/*if(cycles > 10000000000){
			printf("Timeout");
			exit(0);
		}*/
    }
    printf("Sense time = %d\n", e_time.tv_usec - s_time.tv_usec);

}
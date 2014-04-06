#include <stdlib.h>
#include <sys/time.h>
#include <fcntl.h>  
#include <unistd.h>  
#include <stdio.h>  
#include <string.h>  
#include <sched.h>


int setupGPIO(char *pszGPIO, char *pszDir, int direction);
void freeGPIO(char *pszGPIO); 
void pulse_out(int fd, long microseconds); 
int pulse_in(int fd);
struct sched_param param;

char* filename;

int main(int argc, char *argv[]){
	if(argc == 1)
		filename = "sensor_data.txt";
	else if(argc > 1)
		filename = argv[1];

	param.sched_priority = sched_get_priority_max(SCHED_RR);
	if(sched_setscheduler(0, SCHED_RR, &param) != 0){
		printf("sched_setscheduler error");
		exit(EXIT_FAILURE);
	}
	int pin3, pin2;  
	pin3 = setupGPIO("3","out", 1);  
	pin2 = setupGPIO("2","in", 0);
	close(pin2);
	pulse_out(pin3, 10);
	pulse_in(pin2);
	close(pin3);
	return 0;
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
  /* if (result != len) {  
    // printf("Cannot write to /sys/class/gpio/export to setup GPIO %s\n",pszGPIO);  
     printf("Wrote %d bytes\n",result);  
     close(fd);  
     // Remove exiting here for now  
     // as it will fail if the GPIO was already exported  
     // so just ignore the error  
 //    exit(1);  
   }  */
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

void pulse_out(int fd, long microseconds){  
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
   
    nanosleep(microseconds);
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
	char buffer[100];
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
		} else {
			value = 0;
		}
		if(value && ! start){
			gettimeofday(&s_time, NULL);
			start = 1;
		} else if(!value && start){
			gettimeofday(&e_time, NULL);
			end = 1;
		}
    }
    printf("%f\n\0", (float)(e_time.tv_usec - s_time.tv_usec)/58.0);
  /*  FILE *output_file = NULL;
    output_file = fopen(filename, "w");
    sprintf(buffer, "%f\n\0", (float)(e_time.tv_usec - s_time.tv_usec)/58.0);
    fwrite(buffer, sizeof(char), strlen(buffer), output_file);
    fclose(output_file);*/
}
import java.io.IOException;
import java.util.TooManyListenersException;

public class Quadcopter {
 private  static Quadcopter myQuadcopter;
 private XbeeInterface xbeeInterface;

 private int currState = 0;
 private int nextState = 0;

 /***********
 FSM
 0 = Disarm
 1 = Test 1
 2 = Takeoff
 3 = Test 2
 4 = Hover | Room | Hallway
 5 = Land
 ***********/

 //Variables
 private Boolean Xtest1 = false;
 private Boolean Xtest2 = false;
 private Boolean XtakeoffLand = false;
 
 private Boolean Xreturn = false;
 private Boolean Xmanual = false;
 
 private Boolean Xstart = false;
 
 private Boolean Qtest1 = false;
 private Boolean Qtest2 = false;
 //Quadcopter next actions;
 //10 = hover; 11 = room; 12 = hallway; 13 = Land
 //each of the hover(), room(), hallway() functions 
 //  should return one of these numbers
 private int QcurrAction = 10;

 private Quadcopter() throws TooManyListenersException { 
  myQuadcopter = Quadcopter.getInstance(); 
 }
 
 public static Quadcopter getInstance() throws TooManyListenersException {
  if(myQuadcopter == null) myQuadcopter = new Quadcopter();
  return myQuadcopter;
 }
 
 private void parseCommand (String str) {
  if (str.equals("Start"))
   //do something....
    return;
  else if (str.equals("Test1"))
   Xtest1 = true;
  else if (str.equals("Test2"))
   Xtest2 = true;
  else if (str.equals("Return"))
   //return to base (Navigation?)
    return;
  else if (str.equals("Takeoff and land"))
   //PilotController.takeoffLand();
    return;
  else
    try{
    xbeeInterface.sendCopterData("Error parsing.");
  }
  catch (Exception e) {
     System.err.println(e.toString()); 
  }
  
 }
 
 public void main (String[] args){
  while(true){
   switch (currState) {
    case 0: 
     if (Xtest1)
      nextState = 1;
     else
      nextState = 0;
     break;
    
    case 1:
     //Qtest1 = TEST 1
     if (Qtest1)
      nextState = 2;
     else
      nextState = 0;
     //Reset Qtest1? 
     break;
     
    case 2: 
     //PilotController.takeoff();
     nextState = 3;
     break;
    
    case 3:
     //Qtest2 = TEST 2
     if (Qtest2)
      nextState = 4;
     else
      nextState = 5;
     //Reset Qtest2?
     break;
     
    case 4:
     if (QcurrAction == 11) 
      //QcurrAction = Navigation.room();
      return;
     else if (QcurrAction == 12)
      //QcurrAction = Navigation.hallway();
      return;
     else if (QcurrAction == 13)
      nextState = 5; 
     else //If not room or hallway, just hover
      //QcurrAction = Navigation.hover();
       return;
     break;
    
    case 5:
     //PilotController.land();
     nextState = 0;
     break;
    
    default:
     //Navigation.hover();
     break;
   }
   currState = nextState;
  }
 }
}

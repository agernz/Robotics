#include<Servo.h>

//Variable for storing received data
char data = 0;   

//Servos
Servo servo_arm;
Servo servo_claw;
Servo servo_ramp;
Servo servo_base;
Servo servo_joint;

//commands for servos
//arm up, down, buttons released
//Y up, A down
String arm_commands[] = {"b21","b01","b20","b00"};

//ramp up, ramp down,
//Trigger buttons for position 60 and 40
//Left trigger up, Right trigger down
String ramp_commands[] = {"ty8","tx8", "b51","b41","b50","b40"};

//for continuos servos
int servo_forward = 180;
int servo_off = 92;
int servo_reverse = 0;

//input pins for ir sensors
int sensor_pin1 = 13;
int sensor_pin2 = 12;
int sensor_pin3 = 11;
int sensor_pin4 = 10;

String command;

//loop counter
int i;

void setup()
{
  //Sets the baud for serial data transmission  
  Serial.begin(9600);  

  //Set up servo pins for output
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
     
  //Servo pins for servo control
  servo_arm.attach(2);
  servo_ramp.attach(3);

  //Set up sensor input pins
  pinMode(sensor_pin1, INPUT);
  pinMode(sensor_pin2, INPUT);  
  pinMode(sensor_pin3, INPUT);  
  pinMode(sensor_pin4, INPUT);  
}

void loop()
{
  //Wait for data to be recieved
  if(Serial.available() > 0)      
  { 
    //Clear command
    command = "";

    //Store 3 char command
    for(i = 0; i < 3; ++i)
    {
      //Store incoming data
      data = Serial.read();
      command += data;
      delay(3);
    }

    //Move arm up
    if(command == arm_commands[0])
    {
      servo_arm.write(servo_forward);
    }
    //Move arm down
    else if(command == arm_commands[1])
    {
      servo_arm.write(servo_reverse);
    }
    //Stop movement
    else if(command == arm_commands[2] || 
        command == arm_commands[3])
    {
      servo_arm.write(servo_off);
    }

    //Ramp up
    if(command[1] == 'y' && 
        command[2] > ramp_commands[0][2])
    {
      servo_ramp.write(150);
    }
    //stop movement
    else if(command[1] == 'y' &&
        command[2] < ramp_commands[0][2])
    {
      servo_ramp.write(servo_off);
    }
    //Ramp down
    else if(command[1] == 'x' && 
        command[2] > ramp_commands[1][2])
    {
      servo_ramp.write(20);
    }
    //Stop movement
    else if(command[1] == 'x' &&
        command[2] < ramp_commands[1][2])
    {
      servo_ramp.write(servo_off);
    }
    //Ramp 60
    if(command == ramp_commands[2])
    {
      servo_ramp.write(60);
    }
    //Ramp 40
    else if(command == ramp_commands[3])
    {
      servo_ramp.write(40);
    }
    //Stop movement
    else if(command == ramp_commands[4] || 
        command == ramp_commands[5])
    {
      servo_ramp.write(servo_off);
    }
    
  }

  //Send sensor data
  Serial.println(digitalRead(sensor_pin1));
  Serial.println(digitalRead(sensor_pin2));
  Serial.println(digitalRead(sensor_pin3));
  Serial.println(digitalRead(sensor_pin4));
}

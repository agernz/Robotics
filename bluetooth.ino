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

//claw open, claw close, buttons released
//Left bumper open, Right bumper close
String claw_commands[] = {"b51","b41","b50","b40"};

//ramp up, ramp down, buttons released
//Left trigger up, Right trigger down
String ramp_commands[] = {"ty8","tx8"};

//base up, base down, buttons released
//Left joystick up, Left joystick down
String base_commands[] = {"y-6","y+6"};

//joints up, joints down, buttons released
//Right joystick up, Right joystick down
String joint_commands[] = {"r-6","r+6"};

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
  pinMode(4, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
     
  //Servo pins for servo control
  servo_arm.attach(2);
  servo_claw.attach(3);
  servo_ramp.attach(4);
  servo_base.attach(5);
  servo_joint.attach(6);

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

    //Open claw
    if(command == claw_commands[0])
    {
      servo_claw.write(servo_forward);
    }
    //Close claw
    else if(command == claw_commands[1])
    {
      servo_claw.write(servo_reverse);
    }
    //Stop movement
    else if(command == claw_commands[2] || 
        command == claw_commands[3])
    {
      servo_claw.write(0);
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

    //Base up
    if(command[0] == 'y' && 
        command[1] == '-' && 
        command[2] > base_commands[0][2])
    {
      servo_base.write(servo_forward);
    }
    //stop movement
    else if(command[0] == 'y' && 
        command[1] == '-' && 
        command[2] < base_commands[0][2])
    {
      servo_base.write(servo_off);
    }
    //Base down
    if(command[0] == 'y' && 
        command[1] == '+' && 
        command[2] > base_commands[1][2])
    {
      servo_base.write(servo_reverse);
    }
    //stop movement
    else if(command[0] == 'y' && 
        command[1] == '+' && 
        command[2] < base_commands[1][2])
    {
      servo_base.write(servo_off);
    }

    //Joint up
    if(command[0] == 'r' && 
        command[1] == '-' && 
        command[2] > joint_commands[0][2])
    {
      servo_joint.write(servo_forward);
    }
    //stop movement
    else if(command[0] == 'r' && 
        command[1] == '-' && 
        command[2] < joint_commands[0][2])
    {
      servo_joint.write(servo_off);
    }
    //Joint down
    if(command[0] == 'r' && 
        command[1] == '+' && 
        command[2] > joint_commands[1][2])
    {
      servo_joint.write(servo_reverse);
    }
    //stop movement
    else if(command[0] == 'r' && 
        command[1] == '+' && 
        command[2] < joint_commands[1][2])
    {
      servo_joint.write(servo_off);
    }
    
  }

  //Send sensor data
  Serial.println(digitalRead(sensor_pin1));
  Serial.println(digitalRead(sensor_pin2));
  Serial.println(digitalRead(sensor_pin3));
  Serial.println(digitalRead(sensor_pin4));
}

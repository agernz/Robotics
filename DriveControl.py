"""
    Adam Gernes     1/31/17
    Code for the 2017 MRDC Robotics competition.
    This code will drive a robot with mecanum wheels by sending input
    from an Xbox controller to a server hosted on the robot's pi which
    will then command the arduino.
"""

# Using pygame library for controller input
import pygame
# Using serial library to communicate with arduino
import serial

# Must initialize the library
pygame.init()

# Initialize controllers
try:
    joy1 = pygame.joystick.Joystick(0)
    joy1.init()
except Error as e:
    print('Controller 1 not connected!')

#------Global Variables------#

# Serial connection with arduino over usb, baud rate of 9600
ser = serial.Serial('/dev/ttyACM0', 9600)

# Game loop condition
running = True

# Codes for commands to send
forward = 'N'
reverse = 'S'
strafeL = 'E'
strafeR = 'W'
rotateR = 'R'
rotateL = 'L'
EXIT = 'Q'

# Buttons and Joystick threshold
Button_QUIT = 7
THRESHOLD = .15

#------Functions------#

# End program
def stop():
    ser.write(EXIT)
    pygame.quit()
    print('Program Exited')


#------Main Loop------#
while running:
    
    # User did something
    for event in pygame.event.get(): 
        
        # Check if input was from controller
        if event.type == pygame.JOYBUTTONDOWN or event.type == pygame.JOYAXISMOTION:

            # Quit
            if joystick.get_button(7):
                running = False

            #--Drive--#
            axis1_vert = joystick.get_axis(1)
            axis1_hor = joystick.get_axis(0)
            axis2_vert = joystick.get_axis(3)
            axis2_hor = joystick.get_axis(4)

            # Forward, left joystick moved up
            if  axis1_vert < -THRESHOLD:
                ser.write('{0}:{:>6.2f}'.format(forward, axis1_vert))
                
            # Reverse, left joystick moved down
            elif axis1_vert > THRESHOLD:
                ser.write('{0}:{:>6.2f}'.format(reverse, axis1_vert))

            # Strafe left, left joystick moved to the left
            elif  axis1_hor < -THRESHOLD:
                ser.write('{0}:{:>6.2f}'.format(strafeL, axis1_hor))
                
            # Strafe right, left joystick moved to the right
            elif axis1_hor > THRESHOLD:
                ser.write('{0}:{:>6.2f}'.format(strafeR, axis1_hor))

            # Rotate left, right joystick moved to the left
            elif  axis2_hor < -THRESHOLD:
                ser.write('{0}:{:>6.2f}'.format(rotateL, axis2_hor))
                
            # Rotate right, right joystick moved to the right
            elif axis2_hor > THRESHOLD:
                ser.write('{0}:{:>6.2f}'.format(rotateR, axis2_hor))
                

stop() 

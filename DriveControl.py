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
# Using struct library to convert to binary
import struct

# Must initialize the library
pygame.init()

# Initialize controllers
try:
    joy1 = pygame.joystick.Joystick(0)
    joy1.init()
except pygame.error as e:
    print('Controller 1 not connected!')

#------Global Variables------#

# Serial connection with arduino over usb, baud rate of 9600
try:
    ser = serial.Serial('COM4', 9600)
except serial.SerialException as e:
    print('Can not connect to Arduino')

# Game loop condition
running = True

# Codes for commands to send
forward = 1
reverse = 2
strafeL = 3
strafeR = 4
rotateR = 5
rotateL = 6
EXIT = 255

# Buttons and Joystick threshold
Button_QUIT = 7
THRESHOLD = .2

#------Functions------#

# End program
def stop():
    send(EXIT)
    ser.close()
    pygame.quit()
    print('Program Exited')

def send(value, speed = 0):
    ser.write(struct.pack('>B', value))
    ser.write(struct.pack('>B', abs(speed)))


#------Main Loop------#
while running:

    # User did something
    for event in pygame.event.get(): 
        
        # Check if input was from controller
        if event.type == pygame.JOYBUTTONDOWN or event.type == pygame.JOYAXISMOTION:

            # Quit
            if joy1.get_button(7):
                running = False

            #--Drive--#
            axis1_vert = joy1.get_axis(1)
            axis1_hor = joy1.get_axis(0)
            axis2_vert = joy1.get_axis(3)
            axis2_hor = joy1.get_axis(4)

            # Forward, left joystick moved up
            if  axis1_vert < -THRESHOLD:
                print('forward')
                send(forward, int(100*axis1_vert))

            # Reverse, left joystick moved down
            elif axis1_vert > THRESHOLD:
                print('reverse')
                send(reverse, int(100*axis1_vert))

            # Strafe left, left joystick moved to the left
            elif  axis1_hor < -THRESHOLD:
                print('strafe left')
                send(strafeL, int(100*axis1_hor))
                
            # Strafe right, left joystick moved to the right
            elif axis1_hor > THRESHOLD:
                print('strafe right')
                send(strafeR, int(100*axis1_hor))

            # Rotate left, right joystick moved to the left
            elif  axis2_hor < -THRESHOLD:
                print('rotate left')
                send(rotateL, int(100*axis2_hor))
                
            # Rotate right, right joystick moved to the right
            elif axis2_hor > THRESHOLD:
                print('rotate right')
                send(rotateR, int(100*axis2_hor))
            
            else:
                send(0)
                

stop() 

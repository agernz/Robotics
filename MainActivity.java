package com.agernz.btrobotcontrol;

import android.bluetooth.BluetoothSocket;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Intent;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String BT_ADAPTER = "HC-05";
    private BluetoothAdapter BA;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Boolean connected = false;
    private Boolean paired = false;

    ScrollView sv;
    TextView tv;
    TextView sensorv;

    private Button connectB;
    private Button clearB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectB = (Button)findViewById(R.id.connectButton);
        connectB.setOnClickListener(connectListener);

        clearB = (Button)findViewById(R.id.clearButton);
        clearB.setOnClickListener(clearListener);

        BA = BluetoothAdapter.getDefaultAdapter();
        sv = (ScrollView)findViewById(R.id.scrollView);
        tv = (TextView)findViewById(R.id.textView);
        sensorv = (TextView)findViewById(R.id.sensorText);
    }

    private  OnClickListener clearListener = new OnClickListener() {
        //Clear textView
        @Override
        public void onClick(View v) {
            tv.setText("");
        }
    };


    private OnClickListener connectListener = new OnClickListener() {
        //Turn on Bluetooth and connect to HC-05
        @Override
        public void onClick(View v) {
            //ensure bluetooth is turned on
            if (!BA.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
            }

            //Only connect if device is not already connected
            if (!connected) {
                //query paired devices
                Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    tv.setText("");
                    for (BluetoothDevice device : pairedDevices) {
                        //find HC-05
                        if (BT_ADAPTER.equals(device.getName())) {
                            //Device is paired
                            paired = true;

                            connected = true;
                            //Get MAC address
                            String address = device.getAddress();

                            print("Device Paired");

                            //Open a socket connection
                            try {
                                print("Creating socket connection...");
                                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                            } catch (Exception e) {
                                print("Failed to create socket!");
                                connected = false;
                            }

                            //End discovery to save bandwidth
                            BA.cancelDiscovery();

                            // Establish the connection.  This will block until it connects.
                            print("Connecting to " + BT_ADAPTER + "...");
                            try {
                                btSocket.connect();
                                print("Connection established, data link open!");
                            } catch (Exception e) {
                                connected = false;
                                print("Connection Failed!");
                                try {
                                    btSocket.close();
                                } catch (Exception e2) {
                                    print("Fatal Error while trying to close socket!");
                                }
                            }

                            // Create a data stream so we can talk to server.
                            print("Creating an output stream...");
                            try {
                                outStream = btSocket.getOutputStream();
                                if (connected)
                                    print("Connection successfully established!");
                            } catch (Exception e) {
                                connected = false;
                                print("Failed to create an output stream");
                            }

                            //found device, so exit loop
                            break;
                        }
                    }
                }
            } else {
                print("Already connected");
            }

            //HC-05 not paired, send user to settings to pair device
            if (!paired) {
                print(BT_ADAPTER + " not paired, please pair device");
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        }
    };

    //Handle gamepad button input to control robot
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        //Check if event is from gamepad
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
                switch (keyCode) {
                    case (KeyEvent.KEYCODE_BUTTON_A):
                        sendData("b01");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_B):
                        sendData("b11");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_Y):
                        sendData("b21");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_X):
                        sendData("b31");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_R1):
                        sendData("b41");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_L1):
                        sendData("b51");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_THUMBR):
                        sendData("b61");
                        break;
                    case (KeyEvent.KEYCODE_BUTTON_THUMBL):
                        sendData("b71");
                        break;
                    default:
                        break;
                }
        }
        return false;
    }

    //Handle gamepad button input to control robot
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean handled = false;
        //Check if event is from gamepad
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            switch (keyCode) {
                case (KeyEvent.KEYCODE_BUTTON_A):
                    sendData("b00");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_B):
                    sendData("b10");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_Y):
                    sendData("b20");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_X):
                    sendData("b30");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_R1):
                    sendData("b40");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_L1):
                    sendData("b50");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_THUMBR):
                    sendData("b60");
                    break;
                case (KeyEvent.KEYCODE_BUTTON_THUMBL):
                    sendData("b70");
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    //Handle gamepad axis input to control robot
    //DPAD
    private int lastdx = 0;
    private  int lastdy = 0;
    private  float thresh = .5f;
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            //get dpad input
            int dx = (int)(event.getAxisValue(MotionEvent.AXIS_HAT_X));
            int dy = (int)(event.getAxisValue(MotionEvent.AXIS_HAT_Y));

            //get axis input
            int x = (int)((event.getAxisValue(MotionEvent.AXIS_X))*10);
            int y = (int)((event.getAxisValue(MotionEvent.AXIS_Y))*10);
            int z = (int)((event.getAxisValue(MotionEvent.AXIS_Z))*10);
            int rz = (int)((event.getAxisValue(MotionEvent.AXIS_RZ))*10);

            //get trigger input
            int triggerx = (int)((event.getAxisValue(MotionEvent.AXIS_RTRIGGER))*10);
            int triggery = (int)((event.getAxisValue(MotionEvent.AXIS_LTRIGGER))*10);

            //handle dpad, send data only if change in value
            if (lastdx != dx) {
                if(dx == 1)
                    sendData("d31");
                else if (dx == -1)
                    sendData("d11");
                else if (dx == 0) {
                    sendData("d30");
                    sendData("d10");
                }
            }
            if (lastdy != dy) {
                if(dy == 1)
                    sendData("d01");
                else if (dy == -1)
                    sendData("d21");
                else if (dy == 0) {
                    sendData("d00");
                    sendData("d20");
                }
            }
            lastdx = dx;
            lastdy = dy;

            //Handle joystick input
            if(x > thresh || x < -thresh) {
                //send a '+' sign to indicate positive value
                //and make data 3 characters
                //9 is the max value to send to stay within 3 chars
                if (x > 0) {
                    if(x > 9)
                        x = 9;
                    sendData("x+" + Integer.toString(x));
                }
                else {
                    if(x < -9)
                        x = -9;
                    sendData("x" + Integer.toString(x));
                }
            }
            if(y > thresh || y < -thresh) {
                //send a '+' sign to indicate positive value
                //and make data 3 characters
                //9 is the max value to send to stay within 3 chars
                if (y > 0) {
                    if(y > 9)
                        y = 9;
                    sendData("y+" + Integer.toString(y));
                }
                else {
                    if(y < -9)
                        y = -9;
                    sendData("y" + Integer.toString(y));
                }
            }
            if(z > thresh || z < -thresh) {
                //send a '+' sign to indicate positive value
                //and make data 3 characters
                //9 is the max value to send to stay within 3 chars
                if (z > 0) {
                    if(z > 9)
                        z = 9;
                    sendData("z+" + Integer.toString(z));
                }
                else {
                    if(z < -9)
                        z = -9;
                    sendData("z" + Integer.toString(z));
                }
            }
            if(rz > thresh || rz < -thresh) {
                //send a '+' sign to indicate positive value
                //and make data 3 characters
                //9 is the max value to send to stay within 3 chars
                if (rz > 0) {
                    if(rz > 9)
                        rz = 9;
                    sendData("r+" + Integer.toString(rz));
                }
                else {
                    if(rz < -9)
                        rz = -9;
                    sendData("r" + Integer.toString(rz));
                }
            }

            //Handle trigger input
            if(triggerx > thresh)
            {
                if(triggerx > 9)
                    triggerx = 9;
                sendData("tx" + Integer.toString(triggerx));
            }
            if(triggery > thresh)
            {
                if(triggery > 9)
                    triggery = 9;
                sendData("ty" + Integer.toString(triggery));
            }
            return true;
        }
        return false;
    }

    //Send data over Bluetooth
    private void sendData(String msg) {
        byte[] msgBuffer = msg.getBytes();

        try {
            outStream.write(msgBuffer);
            print("Sent Data: " + msg);
        } catch (Exception e) {
            print("Failed to send data");
        }
    }

    //Write to textView, make new lines
    //Auto Scroll
    private  void print(String text) {
        if(tv.getText().length() > 1000)
            tv.setText("");
        tv.setText(tv.getText() + "\n>" + text);
        sv.scrollBy(100,100);
    }

    //Send remaining data and close connection
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (Exception e) {
                print("Failed to send data");
            }
        }
        try {
            btSocket.close();
        } catch (Exception e) {
            print("Failed to close socket");
        }
    }
}

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
    String BT_ADAPTER = "HC-05";
    private BluetoothAdapter BA;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Boolean paired = false;

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
            if (btSocket == null) {
                //query paired devices
                Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    tv.setText("");
                    for (BluetoothDevice device : pairedDevices) {
                        //find HC-05
                        if (BT_ADAPTER.equals(device.getName())) {
                            //Device is paired
                            paired = true;
                            //Get MAC address
                            String address = device.getAddress();

                            print("Device Paired");

                            //Open a socket connection
                            try {
                                print("Creating socket connection...");
                                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                            } catch (Exception e) {
                                print("Failed to create socket!");
                            }

                            //End discovery to save bandwidth
                            BA.cancelDiscovery();

                            // Establish the connection.  This will block until it connects.
                            print("Connecting to " + BT_ADAPTER + "...");
                            try {
                                btSocket.connect();
                                print("Connection established, data link open!");
                            } catch (Exception e) {
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
                                print("Connection successfully established!");
                            } catch (Exception e) {
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
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            int x = (int)(event.getAxisValue(MotionEvent.AXIS_HAT_X));
            int y = (int)(event.getAxisValue(MotionEvent.AXIS_HAT_Y));

            if (x == 1)
                sendData("d1");
            else if(x == -1)
                    sendData("d3");;
            if (y == 1)
                sendData("d0");
            else if(y == -1)
                sendData("d2");

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

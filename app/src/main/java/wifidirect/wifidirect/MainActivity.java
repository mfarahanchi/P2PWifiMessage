package wifidirect.wifidirect;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnonoff, btnDiscover, btnSend;
    ToggleButton btnPrivateGroup;
    ListView listView;
    static TextView read_msg_box;
    TextView ConnectionStatus;
    EditText writeMsg;

    boolean isGroup = false ;

    WifiManager wifiManager;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;

    BroadcastReceiver Receiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    // Use this array to show Device name in ListView
    String[] DeviceNameArray;

    // Use this array to connect to device
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    Server server;
    Client client;
    static SendReceive sendReceive;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Initialize();

        // Location permission is necessary to use Wifi Direct
        // for android 6 and later
        checkLocationPermission();

        Listener();
    }

    // Register the Broadcast Reciever
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(Receiver, intentFilter);
    }

    // UnRegister the Broadcast Reciever
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(Receiver);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != getPackageManager().PERMISSION_GRANTED) {
            // the permission aren’t granted and  use requestPermissions to ask the user to grant.
            // The response from the user is captured in the onRequestPermissionsResult callback.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "permission was granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void Listener() {
        // Check current Status of WiFi
        //if WiFi is on, set it off. Otherwise set it on.
        btnonoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnonoff.setText("on");
                } else {
                    wifiManager.setWifiEnabled(true);
                    btnonoff.setText("off");
                }

            }
        });

        // Discover nearby available peers
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //Discovery Started successfully and
                        // the system broadcasts the WIFI_P2P_PEERS_CHANGED_ACTION intent
                        //which you can listen for in a broadcast receiver to obtain a list of peers
                        ConnectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        //Discovery not started
                        ConnectionStatus.setText("Discovery Starting failed");

                    }
                });
            }
        });

        // Change the mode to application
        // there is two mode:
        // 1. Private mode: Send and recieve messages between two people
        // 2. Group mode: Send and recieve messages between more than two peoples
        btnPrivateGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Toast.makeText(getApplicationContext(),"Group mode",Toast.LENGTH_SHORT).show();
                    btnPrivateGroup.setTextOn("Group mode");
                    isGroup = true;
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Privatie mode",Toast.LENGTH_SHORT).show();
                    btnPrivateGroup.setTextOff("Private mode");
                    isGroup = false;
                }
            }
        });

        // Connect to peer
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                if(!isGroup)
                {
                    final WifiP2pDevice device = deviceArray[i];
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;

                    manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(getApplicationContext(), " Not Connected to ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(),"Group is created successfully",Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(getApplicationContext(),"Create Group is failed",Toast.LENGTH_SHORT);
                        }
                    });

                }


            }
        });

        //Send Message
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                sendReceive.Write(msg.getBytes());
            }
        });

    }

    private void Initialize() {
//        Button to enable and disable WiFi
        btnonoff = (Button) findViewById(R.id.onoff);

//        Button to discover peers
        btnDiscover = (Button) findViewById(R.id.discover);

//        Button to send message
        btnSend = (Button) findViewById(R.id.sendButton);

//        Button to change private mode and group mode
        btnPrivateGroup = findViewById(R.id.privateGroupbtn);

//        listView to Show all available peers
        listView = (ListView) findViewById(R.id.peerListView);

//        TextView to show message
        read_msg_box = (TextView) findViewById(R.id.readMsg);

//        TextView to show connection status
        ConnectionStatus = (TextView) findViewById(R.id.connectionStatus);

//        EditText to write meassage
        writeMsg = (EditText) findViewById(R.id.writeMsg);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

//        This class provides the API for managing  Wifi peer to peer connectivity
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

//        A channel  that connects the application to the WiFi p2p framework
        channel = manager.initialize(this, getMainLooper(), null);

        Receiver = new WifiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    //  Fetch the list of peers.
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            // Check Wether the available peers change or not
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                DeviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    DeviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, DeviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                ConnectionStatus.setText("Host");
                server = new Server();
                server.start();
            } else if (wifiP2pInfo.groupFormed) {
                ConnectionStatus.setText("Client");
                client = new Client(groupOwnerAddress);
                client.start();
            }
        }
    };

     static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMSg = new String(readBuff, 0, msg.arg1);
                    read_msg_box.setText(tempMSg);
                    break;
            }
            return true;
        }
    });
}

package wifidirect.wifidirect;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.widget.Toast;

/*
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       MainActivity mainActivity)
    {
        super();
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Check the current action of WiFi
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // Check to see if WiFi is Enabled and notify approprite activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Toast.makeText(context,"Wifi is On",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context,"Wifi is OFF",Toast.LENGTH_SHORT).show();
            }
        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if(manager != null)
            {
                manager.requestPeers(channel,mainActivity.peerListListener);
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            // Respond to new connection or disconnection
            if(manager == null)
            {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected())
            {
                manager.requestConnectionInfo(channel, mainActivity.connectionInfoListener);

            }
            else
            {
                mainActivity.ConnectionStatus.setText("Device Disconnected");
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            // Respond to this Device's Wifi state changing
        }
    }
}

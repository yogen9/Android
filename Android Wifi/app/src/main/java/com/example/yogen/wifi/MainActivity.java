package com.example.yogen.wifi;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    ToggleButton setWifi;
    Button hotSpot;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    List<String> listOfProvider;
    ListAdapter adapter;
    ListView listViwProvider;

    String ssid = "Game";//your SSID
    String pass = "12345678";// your Password


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listOfProvider = new ArrayList<String>();

		/*setting the resources in class*/
        listViwProvider = (ListView) findViewById(R.id.list_view_wifi);
        setWifi = (ToggleButton) findViewById(R.id.btn_wifi);
        hotSpot = (Button) findViewById(R.id.button);

        //Functionality for hotspot buttom
        hotSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result;
                try {
                    Log.d("TAG", "start");
                    wifiManager.setWifiEnabled(false);
                    Method enableWifi = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    WifiConfiguration myConfig = new WifiConfiguration();
                    myConfig.SSID = ssid;
                    myConfig.preSharedKey = pass;
                    Log.d("TAG", myConfig.SSID);
                    myConfig.status = WifiConfiguration.Status.ENABLED;
                    myConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    myConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    myConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    myConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    myConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    //this "if" is for getting write_setting permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.System.canWrite(getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 200);
                        }
                    }
                    //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_SETTINGS}, Integer.parseInt(Settings.ACTION_MANAGE_WRITE_SETTINGS));
                    result = (Boolean) enableWifi.invoke(wifiManager, myConfig, true);
                    Log.d("TAG", "true");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAG", "false ");
                    result = false;
                }
            }
        });

        //this "if" is for location Permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        //Functionality of Wifi button :
        setWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                    setWifi.setText("ON");
                    listViwProvider.setVisibility(ListView.VISIBLE);
                    scaning();
                    Log.d("TAG", "ON");


                    //AutometicConnect
                    WifiConfiguration gameWifi=new WifiConfiguration();
                    gameWifi.SSID= String.format("\"%s\"", ssid); //**Double quates Are Nessasory
                    Log.d("TAG",gameWifi.SSID);
                    gameWifi.preSharedKey=  String.format("\"%s\"", pass);
                    gameWifi.status = WifiConfiguration.Status.ENABLED;
                    gameWifi.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    gameWifi.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    gameWifi.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    gameWifi.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    gameWifi.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    gameWifi.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    gameWifi.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    int networkId = wifiManager.addNetwork(gameWifi);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(networkId,false);
                    wifiManager.reconnect();


                } else {
                    wifiManager.setWifiEnabled(false);
                    setWifi.setText("OFF");
                    listViwProvider.setVisibility(ListView.GONE);
                    Log.d("TAG", "OFF");
                }

            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        /*checking wifi connection
		 * if wifi is on searching availab>le wifi provider*/
        if (wifiManager.isWifiEnabled()) {
            setWifi.setText("ON");
            scaning();
        }

		/*opening a detail dialog of provider on click   */
        listViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ImportDialog action = new ImportDialog(MainActivity.this, (wifiList.get(position)).toString());
                action.showDialog();
            }
        });
    }

    private void scaning() {
        // wifi scaned value broadcast receiver
        receiverWifi = new WifiReceiver();
        // Register broadcast receiver
        // Broacast receiver will automatically call when number of wifi
        // connections changed

        Log.d("TAG", "scanning");
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Log.d("TAG", "scanning__1");
    }


    protected void onPause() {
        Log.d("TAG", "onPause");
        super.onPause();
        //unregisterReceiver(receiverWifi);
    }

    protected void onResume() {

        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
        Log.d("TAG", "OnResume");
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            Log.d("TAG", "OnRecieve");
            // call this method only if you are on 6.0 and up, otherwise call doGetWifi()
            wifiList = wifiManager.getScanResults();
            Log.d("TAG", wifiList.toString());
			/* sorting of wifi provider based on level */
            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return (lhs.level > rhs.level ? -1
                            : (lhs.level == rhs.level ? 0 : 1));
                }
            });
            listOfProvider.clear();
            String providerName;
            for (int i = 0; i < wifiList.size(); i++) {
				/* to get SSID and BSSID of wifi provider*/
                providerName = (wifiList.get(i).SSID).toString()
                        + "\n" + (wifiList.get(i).BSSID).toString();
                listOfProvider.add(providerName);
                Log.d("TAG", (wifiList.get(i).SSID).toString()
                        + "\n" + (wifiList.get(i).BSSID).toString());
            }
			/*setting list of all wifi provider in a List*/
            adapter = new ListAdapter(MainActivity.this, listOfProvider);
            listViwProvider.setAdapter(adapter);

            adapter.notifyDataSetChanged();
            Log.d("TAG", "OnRecieve__1");
        }
    }
}

package com.example.changenotificationtone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoReceiver extends BroadcastReceiver {
	public Boolean wasConnected;
	public AutoReceiver() {
		wasConnected = false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String selectedSSID = sharedPref.getString("wiFiSSID", "");
		String currentSSID = wifiInfo.getSSID();;
		Log.i("SelectedSSID", selectedSSID);
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if(currentSSID.equals(selectedSSID))
		{			
			audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			wasConnected = true;
		}
		else
		{			
			if(wasConnected == true)
			{
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			}			
			wasConnected = false;
		}
		Log.i("wifiInfo1", wifiInfo.toString());
		Log.i("SSID1",wifiInfo.getSSID());            
	}
}

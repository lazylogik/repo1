package com.example.changenotificationtone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
		String action = intent.getAction();
		if(action == ConnectivityManager.CONNECTIVITY_ACTION)
		{
			WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();		
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String selectedSSID = sharedPref.getString("wiFiSSID", "");
			wasConnected = sharedPref.getBoolean("silentMode", false);
			String currentSSID = wifiInfo.getSSID();;
			Log.i("SelectedSSID", selectedSSID);
			AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if(currentSSID.equals(selectedSSID))
			{			
				audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				wasConnected = true;
				// We need an Editor object to make preference changes.
			    // All objects are from android.context.Context
			    SharedPreferences.Editor editor = sharedPref.edit();
			    editor.putBoolean("silentMode", true);
			    // Commit the edits!
			    editor.commit();
			}
			else
			{			
				if(wasConnected == true)
				{
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);					
					wasConnected = false;
					// We need an Editor object to make preference changes.
				    // All objects are from android.context.Context
				    SharedPreferences.Editor editor = sharedPref.edit();
				    editor.putBoolean("silentMode", false);
				}	
				
				
			}
			Log.i("wifiInfo1", wifiInfo.toString());
			Log.i("SSID1",wifiInfo.getSSID());            
		}
		if(action == "com.exmaple.ChangeNotification.MorningTime")
		{
			Log.i("AutoReceiver", "Morning");
			Uri uri;
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String string1 = sharedPref.getString("pref_tone1", "");
            
            if(string1 == "")
            {
            	string1="Null";
            	uri = null;
            }                	
            else
            {
           	 uri = Uri.parse(string1);
            }
            RingtoneManager.setActualDefaultRingtoneUri(context, 
                    RingtoneManager.TYPE_NOTIFICATION, uri);
		}
		if(action == "com.exmaple.ChangeNotification.NightTime")
		{
			Log.i("AutoReceiver", "Night");
			Uri uri;
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String string1 = sharedPref.getString("pref_tone2", "");
            
            if(string1 == "")
            {
            	string1="Null";
            	uri = null;
            }                	
            else
            {
           	 uri = Uri.parse(string1);
            }
            RingtoneManager.setActualDefaultRingtoneUri(context, 
                    RingtoneManager.TYPE_NOTIFICATION, uri);
		}
	}
}

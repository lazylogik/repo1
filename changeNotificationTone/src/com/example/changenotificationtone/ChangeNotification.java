package com.example.changenotificationtone;

import java.util.List;
import java.util.ListIterator;

import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;




public class ChangeNotification extends Activity {
	
	public CharSequence[] m_listOfwifiSSIDs;
	
	boolean mIsBound;
	
	Messenger mService = null;
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);            
            try {
                Message msg = Message.obtain(null, ToggleNotificationService.MSG_REGISTER_CLIENT);
                //msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
        }
    };
	
	public static class SettingsFragment extends PreferenceFragment {
		public CharSequence[] m_listOfWifi;
		public SettingsFragment()
		{
			
		}
		public void setListofWifi(CharSequence[] listOfWifi)
		{
			m_listOfWifi = listOfWifi;
		}
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);	        

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
	        	        
	        ListPreference wifiListPreference = (ListPreference) findPreference("wiFiSSID");	        
	        wifiListPreference.setEntries(m_listOfWifi);
	        wifiListPreference.setEntryValues(m_listOfWifi);
	    }
	 
	}
	
	public static class SettingsActivity extends Activity {
		@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        CharSequence[] listOfWifi = (CharSequence[]) getIntent().getSerializableExtra("listOfWifi");
	        SettingsFragment fragment = new SettingsFragment();
	        fragment.setListofWifi(listOfWifi);
	        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();	       
	        //setContentView(R.layout.activity_change_notification);
	        }
		
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsBound = false;
        setContentView(R.layout.activity_change_notification);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> knownWifiConfigurations = wifiManager.getConfiguredNetworks();
        
        m_listOfwifiSSIDs = new CharSequence[knownWifiConfigurations.size()];
        int count = 0;
        for (ListIterator<WifiConfiguration> iter = knownWifiConfigurations.listIterator(); iter.hasNext(); ) {
        	WifiConfiguration element = iter.next();
        	m_listOfwifiSSIDs[count++] = element.SSID;            
        }
                
        final ToggleButton button = (ToggleButton) findViewById(R.id.button1);
        boolean serviceRunning =isMyServiceRunning();
        if(serviceRunning)
        {
        	button.setChecked(true);
        	doBindService();
        }
        
        //bnOnClick bnOnClick1 = new bnOnClick();
        //button.setOnClickListener(bnOnClick1);	
    }
    
    private boolean isMyServiceRunning() {
        return isMyServiceRunning(false);
    }
    
    private boolean isMyServiceRunning(boolean wake) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo runningservice : manager.getRunningServices(Integer.MAX_VALUE)) {        	
            if (ToggleNotificationService.class.getName().equals(runningservice.service.getClassName())) {
            	if(wake == true)
            	{
            		//ToggleNotificationService service1 = (ToggleNotificationService).;
            		synchronized(runningservice.service)            		
            		{            			
            			runningservice.service.notifyAll();
            		}
            	}
                return true;
            }
        }
        return false;
    }
    
    void doBindService() {
        bindService(new Intent(this, ToggleNotificationService.class), mConnection, Context.BIND_IMPORTANT);
        mIsBound = true;        
    }
    
    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ToggleNotificationService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    //msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
    
    public void onWakeClicked(View view)
    {
    	//isMyServiceRunning(true);
    	sendMessageToService(1);
    }
    
    public void onToggleClicked(View view)
    {
    	
    	boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
        	if(isMyServiceRunning() == false)
        	{
        		Intent intent = new Intent(view.getContext(), ToggleNotificationService.class);            	
        		view.getContext().startService(intent);
        		doBindService();
        	}
            // Enable vibrate
        } else {
        	if(isMyServiceRunning() == true)
        	{
        		Intent intent = new Intent(view.getContext(), ToggleNotificationService.class);            	
        		view.getContext().stopService(intent);
        	}
            // Disable vibrate
        } 
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
        	if(requestCode == 1)
        	{
	            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
	            String uriString = "Null";
	            if (uri != null) {
	                uriString = uri.toString();
	                Log.i("Log", "uriString is " + uriString);
	           }            
	          //myPrefs.edit().beepUri().put(uriString).apply();
	           RingtoneManager.setActualDefaultRingtoneUri(this,  
	                    RingtoneManager.TYPE_NOTIFICATION, uri);
        	}
        	if(requestCode == 2)
        	{
	            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
	            String uriString = "Null";
	            if (uri != null) {
	                uriString = uri.toString();
	                Log.i("Log", "uriString is " + uriString);
	           }            
	          //myPrefs.edit().beepUri().put(uriString).apply();
	           RingtoneManager.setActualDefaultRingtoneUri(this,  
	                    RingtoneManager.TYPE_NOTIFICATION, uri);
        	}
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.change_notification, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

     /*
      * Because it's onlt ONE option in the menu.
      * In order to make it simple, We always start SetPreferenceActivity
      * without checking.
      */
     
     Intent intent = new Intent();
           intent.setClass(this, SettingsActivity.class);
           intent.putExtra("listOfWifi", m_listOfwifiSSIDs);
           startActivityForResult(intent, 0); 
     
           return true;
    }
    
    
}

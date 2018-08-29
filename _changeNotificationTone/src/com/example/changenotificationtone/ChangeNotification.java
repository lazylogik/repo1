package com.example.changenotificationtone;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

import android.media.RingtoneManager;
import android.net.ConnectivityManager;
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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.ToggleButton;




public class ChangeNotification extends Activity {
	
	public CharSequence[] m_listOfwifiSSIDs;
	
	private AlarmManager m_am;
	private int mId;
	private AutoReceiver m_autoReceiver;
	private PendingIntent morningPi, nightPi;
	private Intent startTimersPi;
	private SharedPreferences.OnSharedPreferenceChangeListener spChanged; 
	
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
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_notification);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> knownWifiConfigurations = wifiManager.getConfiguredNetworks();
                
        m_listOfwifiSSIDs = new CharSequence[knownWifiConfigurations.size()];
        int count = 0;
        for (ListIterator<WifiConfiguration> iter = knownWifiConfigurations.listIterator(); iter.hasNext(); ) {
        	WifiConfiguration element = iter.next();
        	m_listOfwifiSSIDs[count++] = element.SSID;        	
        }
                
        m_am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
        morningPi = PendingIntent.getBroadcast( this, 0, new Intent("com.exmaple.ChangeNotification.MorningTime"),
     			0 );
        nightPi = PendingIntent.getBroadcast( this, 0, new Intent("com.exmaple.ChangeNotification.NightTime"),
     			0 );
        startTimersPi = new Intent();
        startTimersPi.setAction("com.exmaple.ChangeNotification.StartTimers");
        
        spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {

					@Override
					public void onSharedPreferenceChanged(
							SharedPreferences arg0, String arg1) {
						// TODO Auto-generated method stub
						sendBroadcast(startTimersPi);
						
					}

        		};
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(spChanged);
        CharSequence[] listOfWifi = (CharSequence[]) getIntent().getSerializableExtra("listOfWifi");
        SettingsFragment fragment = new SettingsFragment();
        fragment.setListofWifi(m_listOfwifiSSIDs);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();	 
        		//PendingIntent.getBroadcast( this, 0, new Intent("com.exmaple.ChangeNotification.StartTimers"),
     			//0 );          	
    }   
    
    

    public void onWakeClicked(View view)
    {
    	//startTimers();
    	sendBroadcast(startTimersPi);
    }    

   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.change_notification, menu);
        return true;
    }    
    
    public void postNotification(String contentText)
	  {		   
		    RemoteViews remoteViews = new RemoteViews(getPackageName(),  
                R.layout.customnotification);
		    NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_stat_name)
			        .setContentTitle("MyApp")
			        .setContentText("Test");
			        //.setContent(remoteViews);
		    
		    NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();  
		    bigText.bigText(contentText);  
	        bigText.setBigContentTitle("MyApp");	          
	        mBuilder.setStyle(bigText);  
		  
		    remoteViews.setTextViewText(R.id.textView1, contentText);
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this, ChangeNotification.class);

			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(ChangeNotification.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(mId++, mBuilder.build());		  
		  
	  }
    
    
}

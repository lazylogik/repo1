package com.example.changenotificationtone;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;


public class ToggleNotificationService extends IntentService {
	
	private Handler handler;
	private int mId;
	private AutoReceiver autoReceiver;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	private Semaphore mSempahore;	
	
	static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    private AlarmManager m_am;
    PendingIntent pi;
    BroadcastReceiver br;
    
    class MyTimerTask extends TimerTask
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mSempahore.notifyAll();
			
		}    	
    }
    
    class myRunnable implements Runnable 
    {

    	  @Override
    	  public void run() {
    	   // TODO Auto-generated method stub
    		  synchronized (mSempahore) {
    		  mSempahore.notifyAll();
    		  }
    	  }
	}
    
    private myRunnable mMyRunnable;
    
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SET_INT_VALUE:
            	synchronized (mSempahore) {
            		mSempahore.notifyAll();
				}
            	
                //incrementby = msg.arg1;
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    private MyTimerTask mMyTimerTask;

	/**
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public ToggleNotificationService() {		  
	      super("HelloIntentService");
	      mId = 0;
	      
	  }
	  
	  @Override
	    public IBinder onBind(Intent intent) {
	        return mMessenger.getBinder();
	    }
	  
	  
	  @Override  
	  public int onStartCommand(Intent intent, int flags, int startId) {  
	     handler = new Handler();
	     mSempahore =new Semaphore(1);
	     mMyRunnable = new myRunnable(); 
	     mMyTimerTask = new MyTimerTask();
	     Notification notification = new Notification();
     	 Intent notificationIntent = new Intent(this, ChangeNotification.class);
     	 PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
     	 notification.setLatestEventInfo(this, "Title 1",
     	        "Message 1", pendingIntent);
     	 startForeground(1, notification);
     	 autoReceiver = new AutoReceiver();
     	 final IntentFilter intentFilter = new IntentFilter();     	 
     	 intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
     	 m_am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
     	 pi = PendingIntent.getBroadcast( this, 0, new Intent("com.exmaple.ToggleNotificationService"),
     			0 );
     	br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
            	synchronized (mSempahore) {
          		  mSempahore.notifyAll();
          		  }
                   }
            };
     	 this.registerReceiver(autoReceiver, intentFilter);
     	 this.registerReceiver(br, new IntentFilter("com.exmaple.ToggleNotificationService"));
	     return super.onStartCommand(intent, flags, startId);  
	  } 
	  
	  @Override
	  public void onDestroy() {
	      // Make sure our notification is gone.
	      this.unregisterReceiver(autoReceiver);
	      this.unregisterReceiver(br);
	  }
	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	  @Override
	  protected void onHandleIntent(Intent intent) {
		  int currentTone = -1;
		  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		  String timeNightString = sharedPref.getString("NightTime", "");
		  String timeMorningString = sharedPref.getString("MorningTime", "");
		  Time timeNow = new Time(Time.getCurrentTimezone());
		  Time timeNight = new Time(Time.getCurrentTimezone());
		  Time timeMorning = new Time(Time.getCurrentTimezone());
		  Timer myTimer = new Timer("WakeTimer");
	    
		  
		  while(true)
		  {			  
		      synchronized (mSempahore) {
		              try {	
		            	  long msSleep = 1000;
		            	  Log.i("ToggleNotificationService","Woke Up");
		                  timeNow.setToNow();
		                  timeNight.setToNow();
		                  timeNight.hour = TimePreference.getHour(timeNightString);
		                  timeNight.minute = TimePreference.getMinute(timeNightString);
		                  timeNight.second = 00;
		                  timeMorning.setToNow();
		                  timeMorning.hour = TimePreference.getHour(timeMorningString);
		                  timeMorning.minute = TimePreference.getMinute(timeMorningString);
		                  timeMorning.second = 00;
		                  String notificationText = "Woke Up ";

		                  Uri uri;	                  
		                  
		                  if(((!timeNow.before(timeNight)) || timeNow.before(timeMorning)))
		                  {
		                	  if( currentTone != 1)
		                	  {
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
				                  RingtoneManager.setActualDefaultRingtoneUri(this, 
				                          RingtoneManager.TYPE_NOTIFICATION, uri);
				                  currentTone = 1;
				                  notificationText += "Notification Tone 2 ";
				                  Log.i("ToggleNotificationService",notificationText);				                  
		                	  }
		                	  if(timeNow.before(timeMorning))
		                	  {
		                		  long msMorning = timeMorning.toMillis(false);
		                		  long msNow = timeNow.toMillis(false);
		                		  msSleep = msMorning - msNow;
		                		  notificationText += "Before Morning Sleeping for " + msSleep; 
		                		  Log.i("ToggleNotificationService", notificationText);		                		  
		                	  }
		                	  else //After Night
	                		  {
	                			  long msMorning = timeMorning.toMillis(false) + 86400000;
		                		  long msNow = timeNow.toMillis(false);
		                		  msSleep = msMorning - msNow;
		                		  notificationText += "After Morning, After Night Sleeping for " + msSleep;
		                		  Log.i("ToggleNotificationService",notificationText);		                		  
	                		  }
		                  }
		                  else //if(timeNow.before(timeNight) && (timeNow.after(timeMorning) || timeNow.equals(timeMorning)))
		                  {
		                	  if( currentTone != 2)
		                	  {
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
				                  RingtoneManager.setActualDefaultRingtoneUri(this, 
				                          RingtoneManager.TYPE_NOTIFICATION, uri);
				                  notificationText += "Notification Tone 1";
				                  Log.i("ToggleNotificationService",notificationText);
				                  currentTone = 2;				                  
		                	  }
		                	  long msNight = timeNight.toMillis(false);
	                		  long msNow = timeNow.toMillis(false);		                		  
	                		  msSleep = msNight - msNow;
	                		  notificationText += "After Morning Before Night Sleeping for " + msSleep;
	                		  Log.i("ToggleNotificationService", notificationText);	                		  
		                  }
		                  if(msSleep <= 0)
		                	  msSleep = 1000;
		                  
		                  postNotification(notificationText);		                  
		                  m_am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 
		                		  msSleep, pi );
		                  mSempahore.wait();		                  
		                  
		                  } catch (Exception e) {
		              }
		          }		      
		  }
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

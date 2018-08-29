package com.example.changenotificationtone;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.Toast;

public class ToggleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toggle);
		 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         String string1 = sharedPref.getString("pref_tone1", "");
         Uri uri;
         if(string1 == "")
         {
         	string1="Null";
         	uri = null;
         }                	
         else
         {
        	 uri = Uri.parse(string1);
         }
         
         Uri currentUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
         String string2;
         CharSequence text;
         if(currentUri == null)
         	string2 = "Null";
         else
         	string2 = currentUri.toString();
         if(string1.compareTo(string2) == 0)
         {
         	uri = null;
         	text = "Notification disabled";
         }
         else
         {
        	 text = "Notification changed to " + string1; 
         }
         
         RingtoneManager.setActualDefaultRingtoneUri(this, 
                 RingtoneManager.TYPE_NOTIFICATION, uri);
         Context context = getApplicationContext();
         
         int duration = Toast.LENGTH_SHORT;

         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
         finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.toggle, menu);
		return true;
	}

}

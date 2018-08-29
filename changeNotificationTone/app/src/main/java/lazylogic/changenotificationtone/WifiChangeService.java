
package lazylogic.changenotificationtone;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by u324267 on 5/7/2018.
 */

public class WifiChangeService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int wifiState = wifiManager.getWifiState();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedSSID = sharedPref.getString("wiFiSSID", "");
        Boolean wasConnected;
        wasConnected = sharedPref.getBoolean("silentMode", false);
        String currentSSID = wifiInfo.getSSID();;
        Log.i("SelectedSSID", selectedSSID);
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
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
                editor.commit();
            }

        }
        Log.i("wifiInfo1", wifiInfo.toString());
        Log.i("SSID1",wifiInfo.getSSID());
        jobFinished(params, false);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(cm.isActiveNetworkMetered()) {
            JobScheduler js =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    0,
                    new ComponentName(this, WifiChangeService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .build();
            js.schedule(job);
        }
        else
        {
            JobScheduler js =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    0 + 1,
                    new ComponentName(this, WifiChangeService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED)
                    .build();
            js.schedule(job);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}

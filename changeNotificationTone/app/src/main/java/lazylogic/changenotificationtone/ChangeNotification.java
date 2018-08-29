package lazylogic.changenotificationtone;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

import android.util.Log;


public class ChangeNotification extends Activity {

    public CharSequence[] m_listOfwifiSSIDs;

    private AlarmManager m_am;
    private int mId;
    private AutoReceiver m_autoReceiver;
    private PendingIntent morningPi, nightPi;
    private Intent startTimersPi;
    private SharedPreferences.OnSharedPreferenceChangeListener spChanged;
    private Boolean doWeHaveWriteSettingsPermimssion;
    private Boolean doWeHaveAccessWifiStatePermission;
    private Boolean doWeHaveReceiveBootCompletedPermission;

    public static class PopupWritePermission extends DialogFragment {
        private void openAndroidPermissionsMenu() {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            startActivity(intent);
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Allow write settings permission")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    openAndroidPermissionsMenu();
                                }
                            }
                    )
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }
                    )
                    .create();
        }
    }

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
    private Boolean requestPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        requestCode);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
            if(retVal){
            }else{
                FragmentManager fm = getFragmentManager();
                PopupWritePermission dialogFragment = new PopupWritePermission();
                dialogFragment.show(fm, "Allow Write permission");
            }
        }
        return retVal;
    }


    public static final int MY_BACKGROUND_JOB = 0;
    public void scheduleJob() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(cm.isActiveNetworkMetered()) {
            JobScheduler js =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    MY_BACKGROUND_JOB,
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
                    MY_BACKGROUND_JOB + 1,
                    new ComponentName(this, WifiChangeService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED)
                    .build();
            js.schedule(job);
        }
        /*
        JobScheduler js =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo job = new JobInfo.Builder(
                MY_BACKGROUND_JOB ,
                new ComponentName(this, WifiChangeService.class))
                .setPeriodic(5000, 1000)
                .setPersisted(true)
                .build();
        long minPeriod = job.getMinPeriodMillis();
        Log.i("Info", "Period is " + Long.toString(minPeriod));
        int ret = js.schedule(job);
        if(ret == JobScheduler.RESULT_FAILURE)
        {
            Log.e("Error", "Can't Schedule");
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doWeHaveWriteSettingsPermimssion = checkSystemWritePermission();
        doWeHaveAccessWifiStatePermission = requestPermission(Manifest.permission.ACCESS_WIFI_STATE, 2 );
        doWeHaveReceiveBootCompletedPermission = requestPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED, 3);

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
        scheduleJob();
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

}

package lazylogic.changenotificationtone;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;

import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.Menu;
import java.util.List;
import java.util.ListIterator;



public class ChangeNotification extends Activity {

    public CharSequence[] m_listOfwifiSSIDs;

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



    private void sendLocalBroadcast()
    {
        Intent startTimersPi;
        startTimersPi = new Intent();
        startTimersPi.setAction("lazylogic.changenotificationtone.StartTimers");
        LocalBroadcastManager.getInstance(this).sendBroadcast(startTimersPi);
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

        spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {

                    @Override
                    public void onSharedPreferenceChanged(
                            SharedPreferences arg0, String arg1) {
                        // TODO Auto-generated method stub
                        sendLocalBroadcast();

                    }

                };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(spChanged);
        CharSequence[] listOfWifi = (CharSequence[]) getIntent().getSerializableExtra("listOfWifi");
        SettingsFragment fragment = new SettingsFragment();
        fragment.setListofWifi(m_listOfwifiSSIDs);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
        registerBroadcasts();
        sendLocalBroadcast();
        //PendingIntent.getBroadcast( this, 0, new Intent("com.exmaple.ChangeNotification.StartTimers"),
        //0 );
    }

    private void registerBroadcasts()
    {
        BroadcastReceiver br = new AutoReceiver();
        IntentFilter filter = new IntentFilter("lazylogic.changenotificationtone.StartTimers");
        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.change_notification, menu);
        return true;
    }

}

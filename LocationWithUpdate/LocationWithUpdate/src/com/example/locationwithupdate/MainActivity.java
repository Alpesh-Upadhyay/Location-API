package com.example.locationwithupdate;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends Activity implements
LocationListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {

private static final String TAG = "MainActivity";
private static final long INTERVAL = 1000 * 10;
private static final long FASTEST_INTERVAL = 1000 * 5;
Button btnFusedLocation;
TextView tvLocation;
LocationRequest mLocationRequest;
GoogleApiClient mGoogleApiClient;
Location mCurrentLocation;
String mLastUpdateTime;

protected void createLocationRequest() {
mLocationRequest = new LocationRequest();
mLocationRequest.setInterval(INTERVAL);
mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
}

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);


if (!isGooglePlayServicesAvailable()) {
    finish();
}
createLocationRequest();
mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();

setContentView(R.layout.activity_main);
tvLocation = (TextView) findViewById(R.id.tvLocation);



btnFusedLocation = (Button) findViewById(R.id.btnShowLocation);
btnFusedLocation.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View arg0) {
        updateUI();
    }
});

}

@Override
public void onStart() {
super.onStart();
Log.d(TAG, "onStart fired ..............");
if(isOnlineINET())
mGoogleApiClient.connect();
}

@Override
public void onStop() {
super.onStop();
Log.d(TAG, "onStop fired ..............");
if(isOnlineINET())
mGoogleApiClient.disconnect();

}

private boolean isGooglePlayServicesAvailable() {
int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
if (ConnectionResult.SUCCESS == status) {
    return true;
} else {
    GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
    return false;
}
}

@Override
public void onConnected(Bundle bundle) {

startLocationUpdates();
}

protected void startLocationUpdates() {
PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
        mGoogleApiClient, mLocationRequest, this);
Log.d(TAG, "Location update started ..............: ");
}

@Override
public void onConnectionSuspended(int i) {

}

@Override
public void onConnectionFailed(ConnectionResult connectionResult) {
Log.d(TAG, "Connection failed: " + connectionResult.toString());
}

@Override
public void onLocationChanged(Location location) {
Log.d(TAG, "Firing onLocationChanged..............................................");
mCurrentLocation = location;
mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
updateUI();
}

private void updateUI() {
Log.d(TAG, "UI update initiated .............");
if (null != mCurrentLocation) {
	Toast.makeText(getApplicationContext(), "location updated", Toast.LENGTH_SHORT).show();
    String lat = String.valueOf(mCurrentLocation.getLatitude());
    String lng = String.valueOf(mCurrentLocation.getLongitude());
    tvLocation.setText(
            "Latitude: " + lat + "\n" +
            "Longitude: " + lng + "\n" 
            );
} else {
    Log.d(TAG, "location is null ...............");
}
}

@Override
protected void onPause() {
super.onPause();
if(isOnlineINET())
stopLocationUpdates();

}

protected void stopLocationUpdates() {
LocationServices.FusedLocationApi.removeLocationUpdates(
        mGoogleApiClient, this);
Log.d(TAG, "Location update stopped .......................");
}

@Override
public void onResume() {
super.onResume();



if(!isOnlineINET()){
	//Toast.makeText(getApplicationContext(), "false", 2000).show();

    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    builder.setTitle("Connection Error!");
    builder.setMessage("Check your internet connection");
    builder.setIcon(R.drawable.ic_launcher);
    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            //dialog.dismiss();
            finish();
          
        }
        
    }).show();

    return; 
}



LocationManager lm = null;
boolean gps_enabled = false,network_enabled = false;
int i = 0;
   if(lm==null)
       lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
   try{
   gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
   }catch(Exception ex){}
   try{
   network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
   }catch(Exception ex){}

   try {
	 i= Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
} catch (SettingNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

   if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
	     // only for kitkat  and newer versions
	   if(i!=3){
		   showSettingsAlert();
			  return;   
	   }
	}
 
   
  if(!gps_enabled && !network_enabled){
	  showSettingsAlert();
	  return;
  }


if(isOnlineINET()){
if (mGoogleApiClient.isConnected()) {
    startLocationUpdates();
    Log.d(TAG, "Location update resumed .....................");
}
}

}

public boolean isOnlineINET() {
	ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

	NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	if (wifiNetwork != null && wifiNetwork.isConnected()) {
		return true;
	}

	NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	if (mobileNetwork != null && mobileNetwork.isConnected()) {
		return true;
	}

	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	if (activeNetwork != null && activeNetwork.isConnected()) {
		return true;
	}

	return false;
}


 

public void showSettingsAlert(){
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);       
    // Setting Dialog Title
    alertDialog.setTitle("Location Disabled");
    alertDialog.setIcon(R.drawable.ic_launcher);
    // Setting Dialog Message
    alertDialog.setMessage("Please turn on location to high accuracy!");
    // On pressing Settings button
    alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog,int which) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    });
    // on pressing cancel button
    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        finish();
        
        
        }
    });
    // Showing Alert Message
    alertDialog.show();
}
}
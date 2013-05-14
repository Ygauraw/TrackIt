package com.example.trackit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class PushInfo extends FragmentActivity {
	private static LocationManager mLocationManager;
	private static Handler mHandler;
	private static JSONParser jParser = new JSONParser();
	private boolean mUseBoth;
	private GoogleMap mMap;
	private String mUidStr;
	private static final HashMap<String, Marker> mUserList = new HashMap<String, Marker>();
	JSONArray users = null;

	// Keys for maintaining UI states after rotation.
	private static final String KEY_BOTH = "use_both";
	private static final String KEY_UID = "auth_uid";

	// UI handler codes.
	private static final int RECORD_LOCATION = 1000;
	private static final int TWO_SECONDS = 2;
	private static final int TEN_METERS = 10;

	@SuppressLint({ "NewApi", "HandlerLeak" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push_map);
		Bundle myValues = getIntent().getExtras();
		mUidStr = Integer.toString(myValues.getInt(GPSDB.UID));

		if (savedInstanceState != null) {
			mUseBoth = savedInstanceState.getBoolean(KEY_BOTH);
			mUidStr = savedInstanceState.getString(KEY_UID);
		} else {
			mUseBoth = true;
		}
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case RECORD_LOCATION:
					Location curLoc = (Location) msg.obj;
					UpdateUILocation(curLoc.getLatitude(),
							curLoc.getLongitude(), curLoc.getBearing());
					break;
				default:
					break;
				}

			}
		};
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	// Restores UI states after rotation.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_BOTH, mUseBoth);
		outState.putString(KEY_UID, mUidStr);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setup();
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!gpsEnabled) {
			new EnableGpsDialogFragment().show(getSupportFragmentManager(),
					"enableGpsDialog");
			gpsEnabled = true;
		}
		setUpMapIfNeeded();
	}

	// Method to launch Settings
	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	// Stop receiving location updates whenever the Activity becomes invisible.
	@Override
	protected void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(listener);
		mHandler.removeMessages(RECORD_LOCATION);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(listener);
		mHandler.removeMessages(RECORD_LOCATION);
	}

	private void setUpMapIfNeeded() {
		if (mMap == null)
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		mMap.setMyLocationEnabled(true);
		// mMap.animateCamera(null);
	}

	// Set up fine or coarse location
	private void setup() {
		mLocationManager.removeUpdates(listener);
		requestUpdatesFromProvider(LocationManager.GPS_PROVIDER,R.string.not_support_gps);
		requestUpdatesFromProvider(LocationManager.NETWORK_PROVIDER,R.string.not_support_network);
		if (mUserList != null)
			mUserList.clear();
	}

	// Method to register location updates with a desired location provider.
	private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
		Location location = null;
		if (mLocationManager.isProviderEnabled(provider)) {
			mLocationManager.requestLocationUpdates(provider, TWO_SECONDS,TEN_METERS, listener);
			location = mLocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, "Error Occurred :" + errorResId, Toast.LENGTH_SHORT).show();
		}
		return location;
	}

	private void doRecordLocatioToDb(Location location) {
		(new RecordLocationTask(this)).execute(new Location[] { location });
	}

	private final void UpdateUILocation(Double lat, Double lng, float bearing) {
		try {

			LatLng tempLatLng = new LatLng(lat, lng);
			/*
			 * String strLatLong = lat.toString() + " , "+ lng.toString() +
			 * ", "+ Float.toString(bearing) + " , " +
			 * Integer.toString(mUserList.size()); Marker newMarker =
			 * mUserList.get(GPSDB.UMBC_35); if(newMarker == null) { newMarker =
			 * mMap.addMarker(new
			 * MarkerOptions().position(tempLatLng).title(GPSDB
			 * .UMBC_35).snippet(strLatLong)); mUserList.put(GPSDB.UMBC_35,
			 * newMarker); } newMarker.setSnippet(strLatLong);
			 * newMarker.setPosition(tempLatLng);
			 */
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tempLatLng, 17));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final LocationListener listener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			doRecordLocatioToDb(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
		if (currentBestLocation == null) {
			return newLocation;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_SECONDS;
		boolean isSignificantlyOlder = timeDelta < TWO_SECONDS;
		boolean isNewer = timeDelta > 0;

		if (isSignificantlyNewer) {
			return newLocation;
		} else if (isSignificantlyOlder) {
			return currentBestLocation;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return newLocation;
		} else if (isNewer && !isLessAccurate) {
			return newLocation;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return newLocation;
		}
		return currentBestLocation;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	private class RecordLocationTask extends AsyncTask<Location, Void, Void> {
		public RecordLocationTask(Context context) {
			super();
		}

		@Override
		protected Void doInBackground(Location... params) {
			Location loc = params[0];
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.UID, mUidStr));
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.TIMESTAMP, Long.toString(loc.getTime())));
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.LAT, Double.toString(loc.getLatitude())));
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.LNG, Double.toString(loc.getLongitude())));
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.BEARNING, Float.toString(loc.getBearing())));
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.SPEED, Float.toString(loc.getSpeed())));

			JSONObject json = null;
			try {
				json = jParser.makeHttpRequest(GPSDB.PUSH_URL, "POST", pairs);
				if (json.getInt(GPSDB.SUCCESS) > 0) {
					Message.obtain(mHandler, RECORD_LOCATION, loc).sendToTarget();
				}
				// Log.v("PUSHINFO","JSON Result: "+json.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// Dialog to prompt users to enable GPS on the device.

	@SuppressLint("ValidFragment")
	private class EnableGpsDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(R.string.enable_gps)
					.setMessage(R.string.enable_gps_dialog)
					.setPositiveButton(R.string.enable_gps,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									enableLocationSettings();
								}
							}).create();
		}
	}
}

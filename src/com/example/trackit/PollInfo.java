package com.example.trackit;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class PollInfo extends FragmentActivity {
	private static final String TAG = "PollInfo";
	private static JSONParser jParser = new JSONParser();
	private static GoogleMap mMap;
	private static String mUidStr;
	private static Handler mHandler;
	private Context mContext;
	private static Runnable mPollDbRunnable = null;
	private RouteOverlay mRoutePoints = null;
	private static boolean mActivePolling = false;
	
	// Keys for maintaining UI states after rotation.
	private static final String KEY_UID = "auth_uid";

	// UI handler codes.
	private static final int UPDATE_LOCATION = 1;

	@SuppressLint({ "NewApi", "HandlerLeak" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.poll_map);
		Bundle myValues = getIntent().getExtras();
		mUidStr = Integer.toString(myValues.getInt(GPSDB.UID));
		if (savedInstanceState != null) {
			mUidStr = savedInstanceState.getString(KEY_UID);
		}

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_LOCATION:
					UpdateVechileLocations((JSONObject) msg.obj);
					break;
				default:
					break;
				}

			}
		};

		mContext = getApplicationContext();
		mPollDbRunnable = new Runnable() {
			public void run() {
				doPollLocationFromDb(Integer.parseInt(mUidStr));
				if (mActivePolling)
					mHandler.postDelayed(this, GPSDB.REQUEST_INTERVAL);
			}
		};
		
		setUpMapIfNeeded();
	}

	// Restores UI states after rotation.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_UID, mUidStr);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG,"Resuming Service");
		setUpMapIfNeeded();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG,"Pause Service");
		mActivePolling = false;
		mHandler.removeMessages(UPDATE_LOCATION);
		mHandler.removeCallbacks(mPollDbRunnable);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	// Stop receiving location updates whenever the Activity becomes invisible.
	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG,"Stoping Service");
	}
	
	@Override
	public void finish() {
		super.finish();
		Log.e(TAG,"Finishing Service");
		mActivePolling = false;
		mRoutePoints.clearRouteOverlay(mMap);
		mMap = null;
		//Log.e(GPSDB.TAG,"onStop() -- Request (" + Long.toString(mRequestNo) + ")");
	}
	
	private void setUpMapIfNeeded() {
		mActivePolling = true;
		mContext = getApplicationContext();		
		if (mMap == null)
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mRoutePoints == null) {
			mRoutePoints = new RouteOverlay();
		}
		mMap.setMyLocationEnabled(true);
		mHandler.postDelayed(mPollDbRunnable, 1000);
	}

	private void doPollLocationFromDb(Integer uid) {
		(new PollLocationTask(this)).execute(new Integer[] { uid });
	}

	class PollLocationTask extends AsyncTask<Integer, Void, Void> {
		Context mContext;

		public PollLocationTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(Integer... params) {
			Integer uid = params[0];
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.UID, uid.toString()));
			if (mRoutePoints.getReqestNo() == 0)
				pairs.add((NameValuePair) new BasicNameValuePair(GPSDB.ROUTE_REQ, Integer.toString(1)));
			
			JSONObject json = null;
			try {
				json = jParser.makeHttpRequest(GPSDB.POLL_URL, "POST", pairs);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Message.obtain(mHandler, UPDATE_LOCATION, json).sendToTarget();
			return null;
		}
	}

	private final void UpdateVechileLocations(JSONObject json)
	{
		mRoutePoints.moveVechicalMarker(mContext, mMap, json);
	}
}

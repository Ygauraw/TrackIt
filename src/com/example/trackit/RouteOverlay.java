package com.example.trackit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public final class RouteOverlay {
	private static final String TAG = "RouteOverlay";
	private static Context mContext = null;
	private int mRequestNo = 0;

	private HashMap<String, double[]> mRoutesPoints = new HashMap<String, double[]>();
	private HashMap<String, Polyline> mRoutesPolyline = new HashMap<String, Polyline>();
	private HashMap<String, Marker> mVechicalMarkerList = new HashMap<String, Marker>();

	RouteOverlay() {
		mRequestNo = 0;
	}

	private void drawRouteOverlay(Context mContext, GoogleMap mMap, JSONObject json) {
		try {
			Log.e(TAG, "drawPolyLine Called ---------");
			if (json.getInt(GPSDB.SUCCESS) > 0) {
				JSONArray routes = json.getJSONArray(GPSDB.ROUTES);
				// Log.v(TAG,"routes.length [+"+Integer.toString(routes.length())+"]");
				for (int i = 0; i < routes.length(); i++) {
					JSONObject route = routes.getJSONObject(i);
					String uid_key = route.getString(GPSDB.UID);
					JSONArray route_points = route.getJSONArray(GPSDB.LATLNG_POINTS);
					double[] points = new double[route_points.length() * 3];
					for (int j = 0, k = 0; j < route_points.length(); j++, k += 3) {
						JSONObject point = route_points.getJSONObject(j);
						points[k] = point.getDouble(GPSDB.LAT1);
						points[k + 1] = point.getDouble(GPSDB.LNG1);
						// bearing place holder, would be used to show current vehicle directions
						points[k + 2] = 0.0;
					}
					mRoutesPoints.put(uid_key, points);

					PolylineOptions routeCoordinates = new PolylineOptions().color(Color.RED);
					routeCoordinates.width(4);
					int index = 0;
					for (index = 0; index < points.length; index += 3) {
						routeCoordinates.add(new LatLng(points[index],points[index + 1]));
					}
					index = points.length - 3;
					Polyline curPolyline = mMap.addPolyline(routeCoordinates);
					mRoutesPolyline.put(uid_key, curPolyline);
					Log.e("TEST","AddPolyLine to map ------------");

					LatLng endLatLng = new LatLng(points[index], points[index + 1]);
					//drawCustomMarker(mMap, endLatLng, mContext, points[index + 2]);

					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLatLng, 13));
					mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 100, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void moveVechicalMarker(Context context_in, GoogleMap mMap,JSONObject json) {
		try {

			if (json.getInt(GPSDB.SUCCESS) > 0) {

				clearVechicalMarker(mMap);

				if (mRequestNo == 0) {
					Log.e(GPSDB.TAG, "Num : "+ json.getString("num").toString()+": requesturl : "+ json.getString("requesturl").toString());
					mContext = context_in;
					drawRouteOverlay(mContext, mMap, json);
					//writeFileToInternalStorage();
					mRequestNo = 1;
				}

				JSONArray mVechicals = json.getJSONArray(GPSDB.VEHICALES);
				for (int i = 0; i < mVechicals.length(); i++) {
					drawCustomMarker(mMap, mVechicals.getJSONObject(i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getReqestNo() {
		return mRequestNo;
	}

	private void drawCustomMarker(GoogleMap mMap, JSONObject mVechical ) {
		try {
			String uid = mVechical.getString(GPSDB.UID);
			Double lat = mVechical.getDouble(GPSDB.LAT);
			Double lng = mVechical.getDouble(GPSDB.LNG);
			Double bearAngle = mVechical.getDouble(GPSDB.BEARNING);
			String registration = mVechical.getString(GPSDB.REGISTRATION);
			LatLng markLatLng = new LatLng(lat, lng);
			// Add Marker for Bus
			
			Marker vechicleMarker = mMap.addMarker(new MarkerOptions()
					.position(markLatLng).title(uid)
					.title(registration).snippet(markLatLng.toString())
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
			if (vechicleMarker != null) {
				vechicleMarker.showInfoWindow();
				mVechicalMarkerList.put(vechicleMarker.getId(), vechicleMarker);
			}
			
			// Add marker for direction
			Bitmap directionBmp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_tracks_direction);
			directionBmp = rotate(directionBmp, bearAngle);
			Marker directionMarker = mMap.addMarker(new MarkerOptions()
					.position(markLatLng)
					.icon(BitmapDescriptorFactory.fromBitmap(directionBmp))
					.anchor(0.5f, 0.5f));
			
			if (directionMarker != null)
				mVechicalMarkerList.put(directionMarker.getId(), directionMarker);
			if (directionMarker != null) {
				directionMarker.showInfoWindow();
				mVechicalMarkerList.put(directionMarker.getId(), directionMarker);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap rotate(Bitmap src, Double degree) {
	    // create new matrix
	    Matrix matrix = new Matrix();
	    // setup rotation degree
	    matrix.postRotate(Math.round(degree));
	    // return new bitmap rotated using matrix
	    return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
	}
/*
	public void writeFileToInternalStorage() {
		try {
			Bitmap directionBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_tracks_direction);
			Double rot = 322.0;
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		    File filePath = new File(path, "UpdatedDirectionsBitMap.png");
			directionBmp = rotate(directionBmp,rot);
			path.mkdirs();

			Log.e("TEST","File Path : "+filePath);
			FileOutputStream out = new FileOutputStream(filePath);
			directionBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/	
	private void clearRoutePloyLine(GoogleMap mMap) {

		for (Entry<String, Polyline> entry : mRoutesPolyline.entrySet()) {
			Polyline tempPolyline = entry.getValue();
			tempPolyline.remove();
		}

		if (mRoutesPolyline != null)
			mRoutesPolyline.clear();

		if (mMap != null)
			mMap.clear();
	}

	private void clearVechicalMarker(GoogleMap mMap) {
		for (Map.Entry<String, Marker> entry : mVechicalMarkerList.entrySet()) {
			Marker tempMarker = entry.getValue();
			tempMarker.remove();
		}

		if (mVechicalMarkerList != null)
			mVechicalMarkerList.clear();
	}

	public void clearRouteOverlay(GoogleMap mMap) {
		clearVechicalMarker(mMap);
		clearRoutePloyLine(mMap);
		mMap.clear();
	}
	

}

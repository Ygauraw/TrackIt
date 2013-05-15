package com.example.trackit;

public class GPSDB {
	public static final String DATABASE_NAME = "SATYA";
	public static final int DATABASE_VERSION = 1;
	public static final String TAG = "TRACKIT";
	public static final int REQUEST_INTERVAL = 2000;

	public static final String SERVER = "http://mpss.csce.uark.edu/~satya1";
	public static final String SCRIPT_LOCATION = "/trackit/scripts/php/";
	
	public static final String LOGIN_URL = SERVER + SCRIPT_LOCATION + "trackit_login.php";
	public static final String PUSH_URL = SERVER + SCRIPT_LOCATION +  "trackit_push.php";
	public static final String POLL_URL = SERVER + SCRIPT_LOCATION + "trackit_poll.php";	

	public static final String USER_TABLE = "route_";
	public static final String UID = "uid";
	public static final String TIMESTAMP = "timestamp";
	public static final String LAT = "latitude";
	public static final String LNG = "longitude";
	public static final String BEARNING = "bearing";
	public static final String SPEED = "speed";
	public static final String REGISTRATION = "registration";
	
	public static final String ROUTE_REQ = "route_req";
	public static final String ROUTES = "routes";
	public static final String LATLNG_POINTS = "points";
	public static final String LAT1 = "lat";
	public static final String LNG1 = "lng";
	
	public static final String SUCCESS = "success";
	public static final String USERS = "users";
	
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String FULLNAME = "userfullname";
	public static final String ADDR = "address";
	public static final String STATUS = "status";
	public static final String VEHICALES = "vehicales";
	
	
	public static final int EMPTY = 1;
	public static final int LIGHT = 2;
	public static final int CROWDED = 3;
	
	public static final String UMBC_35 = "UMBC_35";
	public static String[] ROUTES_LIST = new String[] { "Arbutus", "UMBC 35" };
	public static int[] ROUTES_VALUES = new int[] { 1,35 };
}
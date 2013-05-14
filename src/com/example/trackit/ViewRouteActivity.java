package com.example.trackit;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewRouteActivity extends ListActivity {
	private static final int POLL_INFO = 1;
	Intent myIntent;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, GPSDB.ROUTES_LIST);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " Selected", Toast.LENGTH_LONG).show();
		myIntent = new Intent(ViewRouteActivity.this, PollInfo.class);
		myIntent.putExtra(GPSDB.UID, GPSDB.ROUTES_VALUES[position]);
		startActivityForResult(myIntent, POLL_INFO);
	}
}
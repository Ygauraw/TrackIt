package com.example.trackit;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UpdateLocationActivity extends ListActivity {
	private static final int PUSH_INFO = 1;
	Intent myIntent;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, GPSDB.ROUTES_LIST);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		myIntent = new Intent(UpdateLocationActivity.this, PushInfo.class);
		myIntent.putExtra(GPSDB.UID, GPSDB.ROUTES_VALUES[position]);
		startActivityForResult(myIntent, PUSH_INFO);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			switch (requestCode) {
			case PUSH_INFO:
				if (resultCode == Activity.RESULT_OK) {
					Bundle myValues = data.getExtras();
		
					if(myValues.getInt(GPSDB.SUCCESS) == 1)
					{
						Toast.makeText(UpdateLocationActivity.this, "Successfull Login ---1---", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(UpdateLocationActivity.this, "Unsuccessfull Login ---2---", Toast.LENGTH_SHORT).show();
						//LoginActivity.this.startActivity(errorIntent);
					}
				}
				break;
			} 
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
}

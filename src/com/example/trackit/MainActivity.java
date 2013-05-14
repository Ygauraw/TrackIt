package com.example.trackit;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
    TabHost mTabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTabHost=getTabHost();
		
	    TabSpec viewRouteSpec = mTabHost.newTabSpec("View");
	    viewRouteSpec.setIndicator("View", getResources().getDrawable(R.drawable.sliver_marker));
	    Intent mapIntent = new Intent(this, ViewRouteActivity.class);
	    viewRouteSpec.setContent(mapIntent);
	    
	    TabSpec updateRouteSpec = mTabHost.newTabSpec("Update");
	    updateRouteSpec.setIndicator("Update", getResources().getDrawable(R.drawable.sliver_marker));
	    Intent updateIntent = new Intent(this, UpdateLocationActivity.class);
	    updateRouteSpec.setContent(updateIntent);
	    
	    mTabHost.addTab(viewRouteSpec);
	    mTabHost.addTab(updateRouteSpec);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

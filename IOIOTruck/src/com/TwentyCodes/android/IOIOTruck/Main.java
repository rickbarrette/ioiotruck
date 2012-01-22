package com.TwentyCodes.android.IOIOTruck;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.TwentyCodes.android.exception.ExceptionHandler;

/**
 * This is the main activity for this application
 * 
 * It will be a simple menu of buttons that will start specific activitys based on the users needs
 * @author ricky barrette
 */
public class Main extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.main);
        
        findViewById(R.id.test_activity_button).setOnClickListener(this);
        findViewById(R.id.nav_activity_button).setOnClickListener(this);
        
        /*
         * Version information textview
         */
        TextView version = (TextView) findViewById(R.id.version_textview);
		PackageManager pm = getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException eNnf) {
			//doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.versionName = "unknown";
			pi.versionCode = 1;
		}
		version.setText(getText(R.string.version)+" "+pi.versionName+" "+getString(R.string.build)+" "+pi.versionCode);
    }

    /**
     * Called when a view has been clicked
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.test_activity_button:
				startActivity(new Intent(this, TestActivity.class));
				break;
			case R.id.nav_activity_button:
				startActivity(new Intent(this, NavigationActivity.class));
				break;
		}
		
	}
}
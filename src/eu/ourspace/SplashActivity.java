package eu.ourspace;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import eu.ourspace.Structures.FbConnect;
import eu.ourspace.Structures.LoginResult;
import eu.ourspace.UI.HomeActivity;
import eu.ourspace.Utils.Utils;

public class SplashActivity extends Activity {
	
	private TimerTask timerTask;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
		
        // load previous username/password
		SharedPreferences settings = getSharedPreferences(Utils.prefsFileName, 0);
		int sessionType = settings.getInt(Utils.prefsSessionType, Utils.SESSION_TYPE_UNDEFINED);
		
		switch (sessionType) {
		case Utils.SESSION_TYPE_FACEBOOK:
    		FbConnect fbConnect = new FbConnect(this, null);
			// clear old session, if not still valid
    		if (!fbConnect.isSessionValid()) {
    			Utils.logoutSession(this);
    		}
			break;
		case Utils.SESSION_TYPE_BACKEND:
			String sessionId = settings.getString(Utils.prefsSessionId, "");
			long expire = settings.getLong(Utils.prefsSessionExpire, 1);
	        String username = settings.getString(Utils.prefsUsername, "");
	        String password = settings.getString(Utils.prefsPassword, "");
	        
	        if (sessionId.length() != 0 && expire < System.currentTimeMillis() 
	        		&& username.length() != 0 && password.length() != 0) {
				LoginResult login = new LoginResult();
				login.emailTxt = username;
				login.passwordTxt = password;
				
				// make the network request
				login.tryLogin(getApplicationContext(), null);
	        }
	        else {
	        	Utils.logoutSession(this);
	        }
			break;
		default:
			break;
		}
		

		
		// show splash screen for at least 1.5 second
		final Timer timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				// start main dashboard activity
		        //Intent i = new Intent(SplashActivity.this, MainTabActivity.class);
				Intent i = new Intent(SplashActivity.this, HomeActivity.class);
				startActivity(i);
				timerTask.cancel();
				timer.cancel();
				
		        // close splash screen - we don't want the user to go back to this view
				SplashActivity.this.finish();
			}
		};
		timer.schedule(timerTask, 1500);
		
    }
}
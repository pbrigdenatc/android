package eu.ourspace.Structures;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;



public class FbConnect extends Object {
	
//  public static final String APP_ID = "188920967801122"; // ATC demo app
	public static final String APP_ID = "177630498963174"; // ouspace app
	
    private Facebook mFacebook = null;
    private Activity mActivity = null;
    private UpdateListener mUpdateListener = null;
	
    
	public FbConnect(Activity activity, UpdateListener updateListener) {
		this.mActivity = activity;
		this.mUpdateListener = updateListener;
		mFacebook = new Facebook(APP_ID);
	}

	// check if previous session is still valid
    public boolean isSessionValid() {
		SharedPreferences settings = mActivity.getSharedPreferences(Utils.prefsFileName, 0);
        String fbToken = settings.getString(Utils.prefsSessionId, "");
        long fbExpire = settings.getLong(Utils.prefsSessionExpire, 1);
        mFacebook.setAccessExpires(fbExpire);
        mFacebook.setAccessToken(fbToken);
        
        return mFacebook.isSessionValid();
    }
    
    private String getUsername() {
    	 try {
    		 String json = mFacebook.request("me");
    		 JSONObject response = new JSONObject(json);
         	return response.isNull("first_name")?"":response.getString("first_name");
         }
         catch (Exception e) {
         	e.toString();
         }
    	 return "";
    }
    
	
    
    // try to login
    public void FacebookLogin() {
    	// if previous session still valid, we're done
        if (isSessionValid()) {
        	endLogin();
        	return;
        }
        
        // else
        // prompt for login
		new Thread() {
            public void run() {
            	Looper.prepare();
            	
				String[] permissions = {};
	       		mFacebook.authorize(mActivity, permissions,
	       				Facebook.FORCE_DIALOG_AUTH, new LoginListener());
		       		
		       	Looper.loop();
            }
		}.start();
    }
    
    // get token/expiration and save them
	private void saveLoginData() {
    	String fbToken = mFacebook.getAccessToken();
    	long fbExpire = mFacebook.getAccessExpires();    	
    	if (fbToken == null)
    		return;

		SharedPreferences settings = mActivity.getSharedPreferences(Utils.prefsFileName, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Utils.prefsSessionId, fbToken);
		editor.putLong(Utils.prefsSessionExpire, fbExpire);
		editor.putInt(Utils.prefsSessionType, Utils.SESSION_TYPE_FACEBOOK);
		editor.putString(Utils.prefsUsername, getUsername());		
		editor.commit();
    }
    
    // call our listener
    private void endLogin() {
		if (mUpdateListener != null) {
			mUpdateListener.updateCompleted(true);
		}
    	
    	mActivity = null;
    	mUpdateListener = null;
    }
    
    public void FacebookLogout() {
		new Thread() {
            public void run() {
            	Looper.prepare();
            	
            	try {
					mFacebook.logout(mActivity);
				} catch (MalformedURLException e) {
				} catch (IOException e) {
				}
				
		       	Looper.loop();
            }
		}.start();
    }
    
    
    public final class LoginListener implements DialogListener {
		@Override
		public void onFacebookError(FacebookError e) {
			Toast.makeText(mActivity, "Facebook: error login", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onError(DialogError e) {
			Toast.makeText(mActivity, "Facebook: error login", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onComplete(Bundle values) {
			saveLoginData();
			endLogin();
		}
		
		@Override
		public void onCancel() {
			Toast.makeText(mActivity, "Facebook: cancelled login", Toast.LENGTH_SHORT).show();
			mActivity.finish();
		}
    }
    

}

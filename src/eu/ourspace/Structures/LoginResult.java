package eu.ourspace.Structures;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import eu.ourspace.Utils.NetworkOperations;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.NetworkResponseListener;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;



public class LoginResult extends Object {
	public String emailTxt;
	public String passwordTxt;
	
	public String sessionId;
	
	public LoginResult() {
		emailTxt = "";
		passwordTxt = "";
		sessionId = "";
	}


	// login
	public void tryLogin(final Context ctx, final UpdateListener updateListener) {
		final Handler handler = new Handler(Looper.getMainLooper()) {
			public void handleMessage(Message msg) {
            	// notify listener, to update UI
            	if (updateListener != null)
            		updateListener.updateCompleted(msg.what != 0);
			}
		};
		
		NetworkResponseListener networkResponseListener = new NetworkResponseListener() {
			@Override
			public void handleNetworkResponse(String result) {
				boolean success = false;
         		// if exist, parse data from network result
        		if (result != null ) {
        			parse(result);
        			if (sessionId.length() > 0) {
        				success = true;
        			}
        			
        			// even when login fails,  store values in shared prefs (ie clear sessionId)
        			SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
        			SharedPreferences.Editor editor = settings.edit();
        			editor.putString(Utils.prefsSessionId, sessionId);
        			editor.putInt(Utils.prefsSessionType, Utils.SESSION_TYPE_BACKEND);
        			editor.putLong(Utils.prefsSessionExpire, System.currentTimeMillis() + 7*24*3600*1000); // TODO: may read from service, in the future
        			editor.putString(Utils.prefsUsername, emailTxt);
        			editor.putString(Utils.prefsPassword, passwordTxt);
        			editor.commit();
        		}
        		
        		handler.sendEmptyMessage(success ? 1 : 0);
			}
		};
		
		NetworkOperations.retrieveDataAsync(ctx, Utils.loginUrl,
				"user=" + emailTxt + "&pass=" + passwordTxt, networkResponseListener);
	}
	
	public void parse (String resultStr) {
		try {
			JSONObject object = new JSONObject(resultStr);
			if (object == null)
				return;
			
			JSONObject result = object.optJSONObject("result");
			if (result == null)
				return;
			
			sessionId = result.optString("SessionId", "");
			
		} catch (JSONException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
}

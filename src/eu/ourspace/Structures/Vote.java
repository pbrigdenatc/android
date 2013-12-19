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



public class Vote extends Object {
	public int postId;
	public boolean positiveNegative;
	public int phaseId;
	
	public boolean success;
	
	public Vote() {
		postId = -1;
		positiveNegative = true;
		phaseId = 0;
		
		success = false;
	}


	// login
	public void sendVote(final Context ctx, final UpdateListener updateListener) {
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
         		// if exist, parse data from network result
        		if (result != null ) {
        			parse(result);
        		}
        			
        		handler.sendEmptyMessage(success ? 1 : 0);
			}
		};
		
		SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
		String sessionId = settings.getString(Utils.prefsSessionId, "");
		int sessionType = settings.getInt(Utils.prefsSessionType, Utils.SESSION_TYPE_UNDEFINED);
		
		NetworkOperations.retrieveDataAsync(ctx, Utils.voteUrl,
				"postId=" + postId + "&sessionId=" + sessionId + "&sessionType=" + sessionType +
				"&type=" + (positiveNegative ? true : false) + "&phaseId=" + phaseId,
				networkResponseListener);
	}
	
	public void parse (String resultStr) {
		try {
			JSONObject object = new JSONObject(resultStr);
			if (object == null)
				return;
			
			JSONObject result = object.optJSONObject("result");
			if (result == null)
				return;
			
			success = result.optBoolean("Succeded", false);
			
		} catch (JSONException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
}

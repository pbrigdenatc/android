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



public class PostReply extends Object {
	public String body;
	public int threadId;
	public boolean isSolution;
	
	public boolean success;
	
	public PostReply() {
		body = "";
		threadId = -1;
		isSolution = false;
		
		success = false;
	}


	// login
	public void sendPost(final Context ctx, final UpdateListener updateListener) {
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
		
		NetworkOperations.retrieveDataAsync(ctx, Utils.postReplyUrl,
				"threadId=" + threadId + "&sessionId=" + java.net.URLEncoder.encode(sessionId) + 
				"&sessionType=" + sessionType +
				"&isSolution=" + (isSolution ? "true" : "false") + 
				"&body=" + java.net.URLEncoder.encode(body) + "&isproposal=false",
				networkResponseListener, false); // POST method
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

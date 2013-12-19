package eu.ourspace.Structures;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import eu.ourspace.GlobalApp;
import eu.ourspace.Databases.LocalDB;
import eu.ourspace.Utils.NetworkOperations;
import eu.ourspace.Utils.NetworkOperations.NetworkResponseListener;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;
import eu.ourspace.Utils.Utils;



public class ForumsList extends Object {
	public List<Forum> forums;
	
	public ForumsList() {
		forums = new ArrayList<Forum>();
	}
	
	
	// retrieve forums or get them from DB if offline
	public void getForums(final Context ctx, final UpdateListener updateListener) {
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
        			success = true;
        			
        			handler.sendEmptyMessage(success ? 1 : 0);
        			
        			LocalDB db = ((GlobalApp)(ctx.getApplicationContext())).getDb();
        			db.deleteAll(LocalDB.FORUMS_TABLE);
        			for (Forum forum : forums) {
        				db.insertForum(forum);
        			}
        			
        		}
        		else {
        			LocalDB db = ((GlobalApp)(ctx.getApplicationContext())).getDb();
        			forums = db.getForums();
        			
        			handler.sendEmptyMessage(success ? 1 : 0);
        		}
			}
		};
		NetworkOperations.retrieveDataAsync(ctx, Utils.forumsUrl, "lang=" + Utils.getLang(ctx), networkResponseListener);
	}
	
	public void parse (String resultStr) {
		try {
			JSONObject object = new JSONObject(resultStr);
			if (object == null)
				return;
			
			JSONObject result = object.optJSONObject("result");
			if (result == null)
				return;
			
			JSONArray array = result.optJSONArray("ForumList");
			if (array == null)
				return;

			for (int i=0; i<array.length(); i++) {
				forums.add(new Forum(array.optJSONObject(i)));
			}
			
		} catch (JSONException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
}

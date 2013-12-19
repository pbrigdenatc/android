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
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.NetworkResponseListener;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;



public class RecentActivityList extends Object {
	public List<RecentActivity> activities;
	
	public RecentActivityList() {
		activities = new ArrayList<RecentActivity>();
	}
	
	
	// retrieve topics or get them from DB if offline
	public void getActivity(final Context ctx, final UpdateListener updateListener) {
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
        			db.deleteAll(LocalDB.RECENTACTIVITY_TABLE);
        			for (RecentActivity activity : activities) {
        				db.insertRecentActivity(activity);
        			}
        			
        		}
        		else {
        			LocalDB db = ((GlobalApp)(ctx.getApplicationContext())).getDb();
        			activities = db.getRecentActivity();
        			
        			handler.sendEmptyMessage(success ? 1 : 0);
        		}
			}
		};
		NetworkOperations.retrieveDataAsync(ctx, Utils.recentActivityUrl, "lang=" + Utils.getLang(ctx), networkResponseListener);
	}
	
	public void parse (String resultStr) {
		try {
			JSONObject object = new JSONObject(resultStr);
			if (object == null)
				return;
			
			JSONObject result = object.optJSONObject("result");
			if (result == null)
				return;
			
			JSONArray array = result.optJSONArray("RecentActivity");
			if (array == null)
				return;

			for (int i=0; i<array.length(); i++) {
				activities.add(new RecentActivity(array.optJSONObject(i)));
			}
			
		} catch (JSONException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
}

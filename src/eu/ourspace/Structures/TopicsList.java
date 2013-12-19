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



public class TopicsList extends Object {
	public List<Topic> topics;
	
	public TopicsList() {
		topics = new ArrayList<Topic>();
	}
	
	
	// retrieve topics or get them from DB if offline
	public void getTopics(final Context ctx, final UpdateListener updateListener, final String table, final String condition) {
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
				String whereClause = null;
				if (condition != null) {
					whereClause = "category_id=" + condition;
				}
				
         		// if exist, parse data from network result
        		if (result != null ) {
        			parse(result);
        			success = true;
        			
        			handler.sendEmptyMessage(success ? 1 : 0);
        			
        			LocalDB db = ((GlobalApp)(ctx.getApplicationContext())).getDb();
        			db.deleteAll(table, whereClause);
        			for (Topic topic : topics) {
        				db.insertTopic(table, topic);
        			}
        			
        		}
        		else {
        			LocalDB db = ((GlobalApp)(ctx.getApplicationContext())).getDb();
        			topics = db.getTopics(table, whereClause);
        			
        			handler.sendEmptyMessage(success ? 1 : 0);
        		}
			}
		};

		if (table.equals(LocalDB.TOPTOPICS_TABLE))
			NetworkOperations.retrieveDataAsync(ctx, Utils.topTopicsUrl, "lang=any", networkResponseListener);
		else if (table.equals(LocalDB.OPENTOPICS_TABLE))
			NetworkOperations.retrieveDataAsync(ctx, Utils.threadsUrl, 
					// if forumId is set then only threads on join phase should be retrieved
					"lang=" + Utils.getLang(ctx) + ((condition == null)?"":"&forumId=" + condition + "&phaseId="+Utils.PHASE_OPEN), 
					networkResponseListener);
		else if (table.equals(LocalDB.PROPOSEDTOPICS_TABLE))
			NetworkOperations.retrieveDataAsync(ctx, Utils.threadsProposedUrl,
					"phaseId=" + Utils.PHASE_PROPOSED + "&lang=" + Utils.getLang(ctx), networkResponseListener);			
		else if (table.equals(LocalDB.SOLUTIONSTOPICS_TABLE))
			NetworkOperations.retrieveDataAsync(ctx, Utils.threadsProposedUrl,
					"phaseId=" + Utils.PHASE_SOLUTIONS + "&lang=" + Utils.getLang(ctx), networkResponseListener);
		else if (table.equals(LocalDB.RESULTSTOPICS_TABLE))
			NetworkOperations.retrieveDataAsync(ctx, Utils.threadsProposedUrl,
					"phaseId=" + Utils.PHASE_RESULT + "&lang=" + Utils.getLang(ctx), networkResponseListener);
	}
	
	public void parse (String resultStr) {
		try {
			JSONObject object = new JSONObject(resultStr);
			if (object == null)
				return;
			
			JSONObject result = object.optJSONObject("result");
			if (result == null)
				return;
			
			JSONArray array = result.optJSONArray("TopTopics");
			if (array == null)
				array = result.optJSONArray("ThreadList");
			if (array == null)
				array = result.optJSONArray("ThreadListPhase");
			if (array == null)
				return;

			for (int i=0; i<array.length(); i++) {
				topics.add(new Topic(array.optJSONObject(i)));
			}
			
		} catch (JSONException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
}

package eu.ourspace.Structures;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
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



public class TopicPostsList extends Object {
	public List<TopicPost> posts;
	public int prevPage;
	public int nextPage;
	
	public TopicPostsList() {
		posts = new ArrayList<TopicPost>();
		prevPage = 0;
		nextPage = 0;
	}
	
	
	// retrieve topics or get them from DB if offline
	public void getPosts(final Context ctx, final UpdateListener updateListener,
			final int topicId, final int phaseId, final boolean showAll,
			final int page, final boolean getLastPage) {
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
        			db.deleteAllPosts(topicId);
        			for (TopicPost post : posts) {
        				db.insertPost(topicId, post);
        			}
        			
        		}
        		else {
        			LocalDB db = ((GlobalApp)(ctx.getApplicationContext())).getDb();
        			posts = db.getTopicPosts(topicId);
        			
        			handler.sendEmptyMessage(success ? 1 : 0);
        		}
			}
		};
		
		SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
		String sessionId = settings.getString(Utils.prefsSessionId, "");
		int sessionType = settings.getInt(Utils.prefsSessionType, Utils.SESSION_TYPE_UNDEFINED);
		
		String args = "threadId=" + topicId + "&phaseId=" + phaseId + "&showAll=" + (showAll ? "true" : "false") +
						"&page=" + page + "&lastPage=" + (getLastPage ? "true" : "false");
		if (sessionType != Utils.SESSION_TYPE_UNDEFINED && sessionId.length() != 0)
			args += "&sessionId=" + sessionId + "&sessionType=" + sessionType;
		NetworkOperations.retrieveDataAsync(ctx, Utils.topicPostsUrl, args, networkResponseListener);
	}
	
	public void parse (String resultStr) {
		try {
			JSONObject object = new JSONObject(resultStr);
			if (object == null)
				return;
			
			JSONObject result = object.optJSONObject("result");
			if (result == null)
				return;
			
			prevPage = result.optInt("prevPage", 0);
			nextPage = result.optInt("nextPage", 0);
			
			JSONArray array = result.optJSONArray("Posts");
			if (array == null)
				return;

			for (int i=0; i<array.length(); i++) {
				posts.add(new TopicPost(array.optJSONObject(i)));
			}
			
		} catch (JSONException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
}

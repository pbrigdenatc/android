package eu.ourspace.UI;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import eu.ourspace.GlobalApp;
import eu.ourspace.R;
import eu.ourspace.Databases.LocalDB;
import eu.ourspace.Structures.RecentActivity;
import eu.ourspace.Structures.RecentActivityList;
import eu.ourspace.Structures.Topic;
import eu.ourspace.Structures.TopicsList;
import eu.ourspace.Utils.LoadImagesQueue;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.LoadImagesQueue.ImageLoadedListener;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class HomeActivity extends ListActivity {
	
	public static final int TYPE_HEADER = 0;
	public static final int TYPE_TOPIC = 1;
	public static final int TYPE_RECENT_ACTIVITY = 2;
	
	public int counter = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        final HomeListAdapter adapter = new HomeListAdapter(this, null, null); 
        setListAdapter(adapter);
        
        load(adapter);
    }
    
    
    
    @Override
	protected void onResume() {		
		super.onResume();
		TextView greeting = (TextView) findViewById(R.id.title_greeting);
		if(Utils.getsessionType(this) == Utils.SESSION_TYPE_UNDEFINED) {
			greeting.setText("");
		}
		else {
			// show user greeting
	        String username = Utils.getUsername(this);        
	        if(!username.equals("")) {
	        	
	        	greeting.setText(getString(R.string.hi) + " " + username);
	        }
		}
	}



	private void load(final HomeListAdapter adapter) {
        
        // Show progress dialog
        final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.updating));

        final TopicsList topics = new TopicsList();
        final RecentActivityList activity = new RecentActivityList();
        
		// prepare request listener
        final UpdateListener updateListenerRecentActivities = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				adapter.changeData(null, activity.activities);
				
				dialog.dismiss();
			}
        };
        
        UpdateListener updateListenerTopTopics = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				adapter.changeData(topics.topics, null);
				activity.getActivity(HomeActivity.this, updateListenerRecentActivities);
			}
        };
        
        // request new data
        topics.getTopics(this, updateListenerTopTopics, LocalDB.TOPTOPICS_TABLE, null);
    }
    
    
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Object item = ((HomeListAdapter)l.getAdapter()).getItem(position);
        if (item instanceof Topic) {
        	Topic topic = (Topic)item;
            Intent i = new Intent(this, TopicViewActivity.class);
            i.putExtra("phaseId", topic.phaseId);
            i.putExtra("showAll", topic.phaseId<3);
            i.putExtra("topicId", topic.topicId);
            i.putExtra("subject", topic.title);
            startActivity(i);
        }
        
        else if (item instanceof RecentActivity) {
        	RecentActivity act = (RecentActivity)item;
            Intent i = new Intent(this, TopicViewActivity.class);
            i.putExtra("phaseId", act.phaseId);
            i.putExtra("showAll", act.phaseId<3);
            i.putExtra("topicId", act.topicId);
            i.putExtra("subject", act.subject);
            if(act.phaseId<3)
            	i.putExtra("lastPage", true);
            startActivity(i);
        }
        
	}
	
	
    // class to hold pointers to view objects, to avoid running findViewById many times
    public static class ViewHolder{
    	public int type;
    	public TextView title;
    	public TextView category;
    	public ImageView langIcon;
    	public ImageView photo;
    }
	
	// adapter
	public class HomeListAdapter extends BaseAdapter {
	    private LayoutInflater inflater=null;
	    private List<Topic> topics;
	    private List<RecentActivity> activities;
	    
	    public HomeListAdapter(Activity a, List<Topic> topics, List<RecentActivity> activities) {
	    	inflater = (LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	    	// initialize with empty array
	    	if (topics != null)
	    		this.topics = topics;
	    	else
	    		this.topics = new ArrayList<Topic>();
	    	
	    	if (activities != null)
	    		this.activities = activities;
	    	else
	    		this.activities = new ArrayList<RecentActivity>();
	    }

	    
	    public void changeData(List<Topic> topics, List<RecentActivity> activities) {
	    	if (topics != null)
	    		this.topics = topics;
	    	if (activities != null)
	    		this.activities = activities;

	    	notifyDataSetChanged();
	    }
	    
	    
	    public int getCount() {
	    	return 2 + topics.size() + activities.size();
	    }

	    public Object getItem(int position) {
	    	if (position == 0 || position == topics.size()+1)
	    		return position;
	    	else if (position < topics.size()+1)
	    		return topics.get(position - 1);
	    	else
	    		return activities.get(position - topics.size() - 2);
	    }

	    public long getItemId(int position) {
	        return position;
	    }
	    

	    
		@SuppressWarnings("null")
		public View getView(int position, View convertView, ViewGroup parent) {
	    	View vi = convertView;
	    	ViewHolder holder = null;
	    	int currentType;
	    	
	    	if (position == 0 || position == topics.size()+1)
	    		currentType = TYPE_HEADER;
	    	else if (position < topics.size()+1)
	    		currentType = TYPE_TOPIC;
	    	else
	    		currentType = TYPE_RECENT_ACTIVITY;

	    	if (vi != null) {
	    		holder = (ViewHolder) vi.getTag();
	    		if (holder.type != currentType)
	    			vi = null;
	    	}
	    	
	    	if (vi == null) {
	    		holder = new ViewHolder();
	    		holder.type = currentType;
	    		
	    		if (currentType == TYPE_HEADER) {
	    			vi = inflater.inflate(R.layout.home_title_row, null);
	    			holder.title = (TextView) vi.findViewById(R.id.title);
	    		}
	    		else if (currentType == TYPE_TOPIC) {
	    			vi = inflater.inflate(R.layout.home_topic_row, null);
	    			holder.category = (TextView) vi.findViewById(R.id.category);
	    			holder.title = (TextView) vi.findViewById(R.id.title);
	    			holder.langIcon = (ImageView) vi.findViewById(R.id.lang_icon);
	    		}
	    		else {// if (currentType == TYPE_RECENT_ACTIVITY) {
	    			vi = inflater.inflate(R.layout.home_recent_activity_row, null);
	    			holder.title = (TextView) vi.findViewById(R.id.title);
	    			holder.langIcon = (ImageView) vi.findViewById(R.id.lang_icon);
	    			holder.photo = (ImageView) vi.findViewById(R.id.photo);
	    		}
	    		
	    		vi.setTag(holder);
	    	}


    		if (currentType == TYPE_HEADER) {
    			if (position ==  0)
    				holder.title.setText(getString(R.string.top_topics));
    			else
    				holder.title.setText(getString(R.string.recent_activity));
    		}
    		else if (currentType == TYPE_TOPIC) {
    			Topic topic = topics.get(position - 1);
    			holder.category.setText(topic.categoryName);
    			
    			String str = " (" + topic.postsCount + ")";
    			
    	        SpannableString spanStr = new SpannableString(topic.title + str);
    	        int color = getResources().getColor(R.color.lightBlue);
    	        spanStr.setSpan(new ForegroundColorSpan(color), topic.title.length(), topic.title.length() + str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    			
    			holder.title.setText(spanStr);
    			
    			int resId = 0;
    			try {
    				resId = getResources().getIdentifier("flag_" + topic.lang + "_sm" , "drawable", getPackageName());
    				holder.langIcon.setImageResource(resId);
    			} catch (NotFoundException e) {}
    			
    		}
    		else if (currentType == TYPE_RECENT_ACTIVITY) {
    			RecentActivity rAct = activities.get(position - topics.size() - 2);
    	        String str1 = rAct.name + " " + getString(R.string.in_place) + " " + rAct.location + ":\n" + getString(R.string.posted) + " ";
    	        String str2 = "\"" + rAct.subject + "\" " + getString(R.string.topic) + ":\n\"" + rAct.description + "\"";
    	        
    	        SpannableString spanStr = new SpannableString(str1 + str2);
    	        int color = getResources().getColor(R.color.lightBlue);
    	        spanStr.setSpan(new ForegroundColorSpan(color), 0, rAct.name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	        spanStr.setSpan(new ForegroundColorSpan(color), str1.length(), str1.length() + rAct.subject.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    			
    			holder.title.setText(spanStr);
    			if (rAct.imgUrl != null && rAct.imgUrl.length() != 0 && !rAct.imgUrl.equals("null")) {
        	    	// for image load, use background thread with queue and cache
        	    	GlobalApp global = (GlobalApp)getApplicationContext();
        	    	final LoadImagesQueue iq = global.getImagesQueue();

        	    	Bitmap cachedImage = null;
        	    	final ImageView imageView = holder.photo;
        	    	imageView.setTag(rAct.imgUrl);
            		cachedImage = iq.loadImage(false, rAct.imgUrl, new ImageLoadedListener() {
            			public void imageLoaded(Bitmap imageBitmap, String name) {
            				String oldImage = (String) imageView.getTag();
            				if (oldImage!=null && oldImage.equals(name)) {
            					imageView.setImageBitmap(imageBitmap);
            					notifyDataSetChanged();
            				}
            			}
            		});

        	    	if( cachedImage != null )
        	    		holder.photo.setImageBitmap(cachedImage);
        			else
        				holder.photo.setImageResource(R.drawable.no_avatar);
    			}
    			else
    				holder.photo.setImageResource(R.drawable.no_avatar);
    			
    			int resId = 0;
    			try {
    				resId = getResources().getIdentifier("flag_" + rAct.language + "_sm" , "drawable", getPackageName());
    				holder.langIcon.setImageResource(resId);
    			} catch (NotFoundException e) {}
    		}


	    	return vi;
	    }
	}
	
	
	


    // MENU
    //
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // move to onPrepare, to be updated when language is changed
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Utils.setAppLanguage(this);

		menu.removeItem(1);
        menu.add(0, 1, 1, R.string.refresh);
        
		menu.removeItem(2);
        menu.add(0, 2, 2, R.string.menu_settings);
        
		menu.removeItem(3);
		
		SharedPreferences settings = getSharedPreferences(Utils.prefsFileName, 0);
        String sessionId = settings.getString(Utils.prefsSessionId, "");
        int sessionType = settings.getInt(Utils.prefsSessionType, Utils.SESSION_TYPE_UNDEFINED);
        if (sessionId.length() > 0 && sessionType != Utils.SESSION_TYPE_UNDEFINED) {
        	menu.add(0, 3, 3, R.string.logout);
        }
        
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);
        switch(item.getItemId()) {
        case 1:
        	load((HomeListAdapter)getListAdapter());
        	break;
        case 2:
	    	Intent i = new Intent(this, GeneralPreferencesActivity.class);
	    	startActivity(i);
	    	break;
        case 3:
	    	Utils.logoutSession(HomeActivity.this);
	        break;
		default:
			break;
        }
        return true;
    }
	
	/** Handle "have your say" action. */
    public void onHaveYourSayClick(View v) {
        // Launch MainTabActivity
    	Intent intent = new Intent(this, MainTabActivity.class);
        startActivity(intent);
    }
	
}

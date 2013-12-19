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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import eu.ourspace.R;
import eu.ourspace.Databases.LocalDB;
import eu.ourspace.Structures.Topic;
import eu.ourspace.Structures.TopicsList;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class ProposeActivity extends ListActivity {
	
	private int phaseId = Utils.PHASE_PROPOSED;
	
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("phaseId", phaseId);
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
	
		
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
		if (icicle != null) {
			phaseId = icicle.getInt("phaseId", phaseId);
		} else {
			final Bundle extras = getIntent().getExtras();
			if (extras != null) {
				phaseId = extras.getInt("phaseId", phaseId);
			}
		}
		
        setContentView(R.layout.topic_view);
        
        
        final ProposeListAdapter adapter = new ProposeListAdapter(this, null); 
        setListAdapter(adapter);
        
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString( (phaseId == Utils.PHASE_PROPOSED ? R.string.propose_title :
			(phaseId == Utils.PHASE_RESULT ? R.string.results_title : R.string.vote_title) ) ));

        load(adapter);
    }
    
    

	private void load(final ProposeListAdapter adapter) {
        // Show progress dialog
        final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.updating));
        
        final TopicsList topics = new TopicsList();
        
		// prepare request listener
        final UpdateListener updateListenerPosts = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				adapter.changeData(topics.topics);
				
				dialog.dismiss();
			}
        };
        
        String table = (phaseId == Utils.PHASE_PROPOSED ? LocalDB.PROPOSEDTOPICS_TABLE :
        				(phaseId == Utils.PHASE_RESULT ? LocalDB.RESULTSTOPICS_TABLE :LocalDB.SOLUTIONSTOPICS_TABLE) );
        
        topics.getTopics(this, updateListenerPosts, table, null);
    }
    
    
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Object item = ((ProposeListAdapter)l.getAdapter()).getItem(position);
        if (item instanceof Topic) {
        	Topic topic = (Topic)item;
            Intent i = new Intent(this, TopicViewActivity.class);
            i.putExtra("phaseId", phaseId);
            i.putExtra("showAll", false);
            i.putExtra("topicId", topic.topicId);
            i.putExtra("subject", topic.title);
            startActivity(i);
        }
        
	}
	
	
	
    // class to hold pointers to view objects, to avoid running findViewById many times
    public static class ViewHolder{
    	public TextView title;
    	public TextView category;
    	public ImageView langIcon;
    }
	
	// adapter
	public class ProposeListAdapter extends BaseAdapter {
	    private LayoutInflater inflater=null;
	    private List<Topic> topics;
	    
	    public ProposeListAdapter(Activity a, List<Topic> topics) {
	    	inflater = (LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	    	// initialize with empty array
	    	if (topics != null)
	    		this.topics = topics;
	    	else
	    		this.topics = new ArrayList<Topic>();
	    }

	    
	    public void changeData(List<Topic> topics) {
	    	if (topics != null)
	    		this.topics = topics;

	    	notifyDataSetChanged();
	    }
	    
	    
	    public int getCount() {
	    	return topics.size();
	    }

	    public Object getItem(int position) {
	    	return topics.get(position);
	    }

	    public long getItemId(int position) {
	        return position;
	    }
	    

		public View getView(int position, View convertView, ViewGroup parent) {
	    	View vi = convertView;
	    	ViewHolder holder = null;
	    	
	    	if (vi != null) {
	    		holder = (ViewHolder) vi.getTag();
	    	}
	    	else {
	    		holder = new ViewHolder();
	    		
	    		vi = inflater.inflate(R.layout.home_topic_row, null);
	    		holder.category = (TextView) vi.findViewById(R.id.category);
	    		holder.title = (TextView) vi.findViewById(R.id.title);
	    		holder.langIcon = (ImageView) vi.findViewById(R.id.lang_icon);
	    		
	    		vi.setTag(holder);
	    	}

	    	Topic topic = topics.get(position);
			
//			String str = " (" + topic.postsCount + ")";
			
//	        SpannableString spanStr = new SpannableString(topic.title + str);
//	        int color = getResources().getColor(R.color.lightBlue);
//	        spanStr.setSpan(new ForegroundColorSpan(color), topic.title.length(), topic.title.length() + str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			
//			holder.title.setText(spanStr);
	    	holder.title.setText(topic.title);
	    	holder.category.setText(topic.categoryName);
			
			int resId = 0;
			try {
				resId = getResources().getIdentifier("flag_" + topic.lang + "_sm" , "drawable", getPackageName());
				holder.langIcon.setImageResource(resId);
			} catch (NotFoundException e) {}

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
        	load((ProposeListAdapter)getListAdapter());
        	break;
        case 2:
	    	Intent i = new Intent(this, GeneralPreferencesActivity.class);
	    	startActivity(i);
	    	break;  
        case 3:
	    	Utils.logoutSession(ProposeActivity.this);
	        break;
		default:
			break;
        }
        return true;
    }
	
	public void onHomeClick(View v){
		Utils.goHome(v.getContext());
	}
}

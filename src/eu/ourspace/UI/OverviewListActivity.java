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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
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

public class OverviewListActivity extends ListActivity {	
	
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("forumId", forumId);
        //outState.putString("name", forumName);
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
        /*
		if (icicle != null) {
			forumId = icicle.getInt("forumId");
			forumName = icicle.getString("name");
		} else {
			final Bundle extras = getIntent().getExtras();
			if (extras != null) {
				forumId = extras.getInt("forumId");
				forumName = extras.getString("name");
			}
		}
		
		// catch error case
		if (forumId == -1) {
			this.finish();
			return;
		}
		*/
        setContentView(R.layout.topic_view);
        
        
        final ThreadListAdapter adapter = new ThreadListAdapter(this, null); 
        setListAdapter(adapter);
        
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.have_your_say);   
        
        load(adapter);
    }
    
    private void load(final ThreadListAdapter adapter) {
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
        
        topics.getTopics(this, updateListenerPosts, LocalDB.OPENTOPICS_TABLE, null);
	}
    
    
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Object item = ((ThreadListAdapter)l.getAdapter()).getItem(position);
        if (item instanceof Topic) {
        	Topic topic = (Topic)item;
            Intent i = new Intent(this, TopicViewActivity.class);
            i.putExtra("phaseId", topic.phaseId);
            i.putExtra("showAll", topic.phaseId<3);
            i.putExtra("topicId", topic.topicId);
            i.putExtra("subject", topic.title);
            startActivity(i);
        }
        
	}
	

	  // class to hold pointers to view objects, to avoid running findViewById many times
    public static class ViewHolder{
    	public TextView title;
    	public TextView subtitle;
    	public TextView category;
    	public ImageView phaseIcon;
    	public TextView phase;
    }
	
	// adapter
	public class ThreadListAdapter extends BaseAdapter {
	    private LayoutInflater inflater=null;
	    private List<Topic> topics;
	    
	    public ThreadListAdapter(Activity a, List<Topic> topics) {
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

	    	if (vi != null)
	    		holder = (ViewHolder) vi.getTag();

	    	else {
	    		holder = new ViewHolder();
	    		
    			vi = inflater.inflate(R.layout.overview_item_row, null);
    			holder.category = (TextView) vi.findViewById(R.id.overview_category);
    			holder.title = (TextView) vi.findViewById(R.id.overview_title);
    			holder.subtitle = (TextView) vi.findViewById(R.id.overview_subtitle);
    			holder.phaseIcon = (ImageView) vi.findViewById(R.id.overview_phase_icon);
    			holder.phase = (TextView) vi.findViewById(R.id.overview_phase);
    				    		
	    		vi.setTag(holder);
	    	}

			Topic topic = topics.get(position);
			
			holder.title.setText(topic.title);
			
	        String subtitle = "  By " + topic.name + " " + " - Posts: " + topic.postsCount;
	        
			int resId = 0;
			try {
				resId = getResources().getIdentifier("flag_" + topic.lang + "_sm" , "drawable", getPackageName());
			} catch (NotFoundException e) {
				resId = R.drawable.flag_eu_sm;
			}		
			
            holder.subtitle.setText(subtitle); 
            holder.subtitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(resId), null, null, null);
            holder.category.setText(topic.categoryName);            
        
			// set phase icon
			switch (topic.phaseId){
			case Utils.PHASE_PROPOSED:
				holder.phaseIcon.setImageResource(R.drawable.propose_suggest_bw);
				holder.phase.setText(R.string.propose_tab_title);
				break;
			case Utils.PHASE_OPEN:
				holder.phaseIcon.setImageResource(R.drawable.propose_join_bw);
				holder.phase.setText(R.string.join_tab_title);
				break;
			case Utils.PHASE_SOLUTIONS:
				holder.phaseIcon.setImageResource(R.drawable.propose_vote_bw);
				holder.phase.setText(R.string.vote_tab_title);
				break;
			case Utils.PHASE_RESULT:
				holder.phaseIcon.setImageResource(R.drawable.propose_view_bw);
				holder.phase.setText(R.string.results_tab_title);
				break;
			default:
				holder.phaseIcon.setVisibility(View.GONE);
				holder.phase.setVisibility(View.GONE);
				break;
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
        	load((ThreadListAdapter)getListAdapter());
        	break;
        case 2:
	    	Intent i = new Intent(this, GeneralPreferencesActivity.class);
	    	startActivity(i);
	    	break;  
        case 3:
	    	Utils.logoutSession(OverviewListActivity.this);
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

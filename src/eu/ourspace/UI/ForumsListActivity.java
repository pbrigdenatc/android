package eu.ourspace.UI;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import eu.ourspace.GlobalApp;
import eu.ourspace.R;
import eu.ourspace.Structures.Forum;
import eu.ourspace.Structures.ForumsList;
import eu.ourspace.UI.HomeActivity.HomeListAdapter;
import eu.ourspace.UI.OverviewListActivity.ThreadListAdapter;
import eu.ourspace.Utils.LoadImagesQueue;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.LoadImagesQueue.ImageLoadedListener;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class ForumsListActivity extends ListActivity {
	
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
        
        setContentView(R.layout.topic_view);
        
        
        final ForumsListAdapter adapter = new ForumsListAdapter(this, null); 
        setListAdapter(adapter);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.join_title));      
        
        
        load(adapter);
    }
    
    private void load(final ForumsListAdapter adapter) {
        
        // Show progress dialog
        final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.updating));
        
        final ForumsList forums = new ForumsList();
        
		// prepare request listener
        final UpdateListener updateListenerPosts = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				adapter.changeData(forums.forums);
				
				dialog.dismiss();
			}
        };
        
        forums.getForums(this, updateListenerPosts);
	}

    
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Object item = ((ForumsListAdapter)l.getAdapter()).getItem(position);
        if (item instanceof Forum) {
        	Forum forum = (Forum)item;
            Intent i = new Intent(this, ThreadListActivity.class);
            i.putExtra("forumId", forum.forumId);
            i.putExtra("name", getString(R.string.forum_prefix) + " " + forum.name);
            startActivity(i);
        }
        
	}
	

    // class to hold pointers to view objects, to avoid running findViewById many times
    public static class ViewHolder {
    	public TextView title;
    	public ImageView photo;
    }
	
	// adapter
	public class ForumsListAdapter extends BaseAdapter {
	    private LayoutInflater inflater=null;
	    private List<Forum> forums;
	    
	    public ForumsListAdapter(Activity a, List<Forum> forums) {
	    	inflater = (LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	    	// initialize with empty array
	    	if (forums != null)
	    		this.forums = forums;
	    	else
	    		this.forums = new ArrayList<Forum>();
	    }

	    
	    public void changeData(List<Forum> forums) {
	    	if (forums != null)
	    		this.forums = forums;

	    	notifyDataSetChanged();
	    }
	    
	    
	    public int getCount() {
	    	return forums.size();
	    }

	    public Object getItem(int position) {
	    	return forums.get(position);
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
	    		
    			vi = inflater.inflate(R.layout.forum_row, null);
    			holder.title = (TextView) vi.findViewById(R.id.title);
    			holder.photo = (ImageView) vi.findViewById(R.id.photo);
	    		
	    		vi.setTag(holder);
	    	}

	    	Forum forum = forums.get(position);
	    	holder.title.setText(forum.name);
	    	
			if (forum.imgUrl != null && forum.imgUrl.length() != 0 && !forum.imgUrl.equals("null")) {
    	    	// for image load, use background thread with queue and cache
    	    	GlobalApp global = (GlobalApp)getApplicationContext();
    	    	final LoadImagesQueue iq = global.getImagesQueue();

    	    	Bitmap cachedImage = null;
    	    	final ImageView imageView = holder.photo;
    	    	imageView.setTag(forum.imgUrl);
        		cachedImage = iq.loadImage(false, forum.imgUrl, new ImageLoadedListener() {
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
    				holder.photo.setImageResource(R.drawable.ourspace_logo_android);
			}
			else
				holder.photo.setImageResource(R.drawable.ourspace_logo_android);

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
        	load((ForumsListAdapter)getListAdapter());
        	break;
        case 2:
	    	Intent i = new Intent(this, GeneralPreferencesActivity.class);
	    	startActivity(i);
	    	break;
        case 3:
	    	Utils.logoutSession(ForumsListActivity.this);
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

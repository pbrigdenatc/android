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
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import eu.ourspace.GlobalApp;
import eu.ourspace.R;
import eu.ourspace.Structures.TopicPost;
import eu.ourspace.Structures.TopicPostsList;
import eu.ourspace.Structures.Vote;
import eu.ourspace.Utils.LoadImagesQueue;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.LoadImagesQueue.ImageLoadedListener;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class TopicViewActivity extends ListActivity {
	
	private int phaseId = 0;
	private boolean showAll = true;
	private int topicId = -1;
	private String subject = "";
	private int postIdToVote = -1;
	private boolean valueToVote = false;
	private boolean replySolution = false; // reply post or solution
	private int page = 1;
	private boolean lastPage = false;

	
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("phaseId", phaseId);
        outState.putBoolean("showAll", showAll);
        outState.putInt("topicId", topicId);
        outState.putString("subject", subject);
        outState.putInt("postIdToVote", postIdToVote);
        outState.putBoolean("valueToVote", valueToVote);
        outState.putBoolean("replySolution", replySolution);
        outState.putInt("page", page);
        outState.putBoolean("lastPage", lastPage);
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
			phaseId = icicle.getInt("phaseId");
			showAll = icicle.getBoolean("showAll", true);
			topicId = icicle.getInt("topicId");
			subject = icicle.getString("subject");
			postIdToVote = icicle.getInt("postIdToVote");
			valueToVote = icicle.getBoolean("valueToVote", false);
			replySolution = icicle.getBoolean("replySolution", false);
			page = icicle.getInt("page", page);
			lastPage = icicle.getBoolean("lastPage", lastPage);
		} else {
			final Bundle extras = getIntent().getExtras();
			if (extras != null) {
				phaseId = extras.getInt("phaseId");
				showAll = extras.getBoolean("showAll", true);
				topicId = extras.getInt("topicId");
				subject = extras.getString("subject");
				postIdToVote = extras.getInt("postIdToVote");
				valueToVote = extras.getBoolean("valueToVote", false);
				replySolution = extras.getBoolean("replySolution", false);
				page = extras.getInt("page", page);
				lastPage = extras.getBoolean("lastPage", lastPage);
			}
		}
		
		// catch error case
		if (topicId == -1) {
			this.finish();
			return;
		}
        
        setContentView(R.layout.topic_view);
        
        final TopicViewAdapter adapter = new TopicViewAdapter(this, null); 
        setListAdapter(adapter);
        
        
        
        TextView title = (TextView) findViewById(R.id.title);
        String titleStr = "";
        
        //when flag showAll is true, we came here from Discussion list (Join)
        if (showAll) {
        	titleStr += getString(R.string.discuss_on) + " ";
        }
        else {
	        switch (phaseId) {
	        case Utils.PHASE_PROPOSED:
	        	titleStr += getString(R.string.propose_for) + " ";
	        	break;
	        case Utils.PHASE_OPEN:
	        	titleStr += getString(R.string.discuss_on) + " ";
	        	break;
	        case Utils.PHASE_SOLUTIONS:
	        	titleStr += getString(R.string.solutions_of) + " ";
	        	break;
	        case Utils.PHASE_RESULT:
	        	titleStr += getString(R.string.results_of) + " ";
	        	break;
	        default:
	        	break;
	        }
        }
        
        title.setText(titleStr + "'" + subject + "'");
        
        load(adapter, false, false, true);
    }
    
    private void load (final TopicViewAdapter adapter, final boolean atBegin, final boolean atEnd, final boolean resetPage) {
        // Show progress dialog
        final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.updating));
        
        final TopicPostsList posts = new TopicPostsList();
        
		// prepare request listener
        final UpdateListener updateListenerPosts = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				adapter.changeData(posts, atBegin, atEnd, resetPage);
				
				dialog.dismiss();
			}
        };
        
        posts.getPosts(this, updateListenerPosts, topicId, phaseId, showAll, page, lastPage);
	}
    
    
    
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != Activity.RESULT_OK || data == null)
			return;
		
		boolean success = false;
		switch (requestCode) {
		case Utils.REQ_LOGIN_TO_VOTE:
			success = data.getBooleanExtra("success", false);
			if (success)
				confirmVote();
			break;
		case Utils.REQ_LOGIN_TO_REPLY:
			success = data.getBooleanExtra("success", false);
			if (success) {
                Intent i = new Intent(this, PostReplyActivity.class);
                i.putExtra("topicId", topicId);
                i.putExtra("replySolution", replySolution);
                startActivityForResult(i, Utils.REPLIED);
			}
			break;
		case Utils.REPLIED:
			success = data.getBooleanExtra("success", false);
			if (success) {
				lastPage = true;
				load((TopicViewAdapter)getListAdapter(), false, false, true);
			}
			break;
		default:
			break;
		}
	}
	
	
	
	public void onThumbsUp(View view) {
		prepareVoting(view, true);
	}

	public void onThumbsDown(View view) {
		prepareVoting(view, false);
	}
	
	private void prepareVoting(View view, boolean value) {
		
		// when viewing old discussion user can't vote (should come here from solutions list)
		// also user can't vote in results phase
		if (phaseId == Utils.PHASE_RESULT || (phaseId > Utils.PHASE_OPEN && showAll) ) {
			Toast.makeText(this, getString(R.string.cant_vote), Toast.LENGTH_LONG).show();
			return;
		}
		
		int postId = 0;
		Object tag = view.getTag();
		if (tag != null && tag instanceof Integer)
			postId = (Integer)tag;
		else {
			Toast.makeText(this, getString(R.string.has_voted), Toast.LENGTH_LONG).show();
			return;
		}
		
		// prepare values and check for login session
		postIdToVote = postId;
		valueToVote = value;
		
		
		if (!Utils.isSessionValid(getApplicationContext())) {
            Intent i = new Intent(this, LoginOptionActivity.class);
            startActivityForResult(i, Utils.REQ_LOGIN_TO_VOTE);
        }
        else {
        	confirmVote();
        }
	}
	
	private void confirmVote() {
        final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == Utils.LEFT_BUTTON_CLICK) {
					sendVote();
				}
			}
        };
        
        String posNeg = " " + getString(valueToVote ? R.string.positive : R.string.negative) + " ";
        Utils.confirmationDialog(this, getString(R.string.vote),
        		getString(R.string.send) + posNeg + getString(R.string.vote_question),
        		getString(R.string.ok), getString(R.string.cancel), handler);
	}
	
	
	private void sendVote() {
		Vote vote = new Vote();
		vote.postId = postIdToVote;
		vote.positiveNegative = valueToVote;
		vote.phaseId = phaseId;

		// Show progress dialog
		final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.please_wait));

		// prepare request listener
        UpdateListener updateListener = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				dialog.dismiss();
				// if success, process the result
        		if (success) {
        			Toast.makeText(TopicViewActivity.this, TopicViewActivity.this.getString(R.string.vote_success), Toast.LENGTH_LONG).show();
        			((TopicViewAdapter)getListAdapter()).updateVotes(postIdToVote, valueToVote);
        		}
        		else {
        			Toast.makeText(TopicViewActivity.this, TopicViewActivity.this.getString(R.string.vote_fail), Toast.LENGTH_LONG).show();
        		}
			}
		};
		
		// make the network request
		vote.sendVote(getApplicationContext(), updateListener);
	}
	

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        TopicViewAdapter adapter = (TopicViewAdapter) l.getAdapter();
        Object item = adapter.getItem(position);
        if ( (item instanceof Integer) ) {
        	// load previous page
        	if (position == 0) {
        		page = (Integer) item;
        		lastPage = false;
        		load(adapter, true, false, false);
        	}
        	// load next page
        	else {
        		page++;
        		load(adapter, false, true, false);
        	}
        }
	}
	

    // class to hold pointers to view objects, to avoid running findViewById many times
    public static class ViewHolder {
    	public int type;
    	public ImageView photo;
    	public TextView user;
    	public TextView date;
    	public TextView body;
    	public TextView thumbsUp;
    	public TextView thumbsDown;
    	public View leftButton;
    	public View rightButton;
    	public RelativeLayout voting;
    	public ImageView thumbsUpImg;
    	public ImageView thumbsDownImg;
    }
    
    private static final int TYPE_POST = 1;
    private static final int TYPE_PREV_PAGE = 2;
    private static final int TYPE_NEXT_PAGE = 3;
	
	// adapter
	public class TopicViewAdapter extends BaseAdapter {
	    private LayoutInflater inflater=null;
	    private List<TopicPost> posts;
	    private int prevPage = -1;
	    private int nextPage = -1;
	    
	    public TopicViewAdapter(Activity a, List<TopicPost> posts) {
	    	inflater = (LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	    	// initialize with empty array
	    	if (posts != null)
	    		this.posts = posts;
	    	else
	    		this.posts = new ArrayList<TopicPost>();
	    }

	    
	    public void changeData(TopicPostsList posts, boolean atBegin, boolean atEnd, boolean resetPage) {
			// clear old paging
	    	if (resetPage) {
	    		prevPage = -1;
	    		nextPage = -1;
	    	}
	    	
	    	if (posts != null && posts.posts != null) {
	    		if (atBegin) {
	    			this.posts.addAll(0, posts.posts);
	    		}
	    		else if (atEnd) {
	    			this.posts.addAll(posts.posts);
	    		}
	    		else {
	    			this.posts = posts.posts;
	    		}
	    		
	    		// if we haven't reached the end (first or last page) in the past, change the value
	    		// otherwise we know we have all pages to the start/end
	    		if (prevPage != 0 )
	    			prevPage = posts.prevPage;
	    		if (nextPage != 0)
	    			nextPage = posts.nextPage;
	    	}

	    	notifyDataSetChanged();
	    }
	    
	    // update value after voting
	    public void updateVotes(int postId, boolean type) {
	    	for (TopicPost post : posts) {
	    		if (post.postId == postId) {
		    		if (type)
		    			post.thumbsUp++;
		    		else
		    			post.thumbsDown++;
		    		
		    		notifyDataSetChanged();
		    		
		    		break;
		    	}
	    	}
	    }
	    
	    public int getCount() {
	    	return posts.size() + (prevPage>0 ? 1 : 0) + (nextPage>0 ? 1 : 0);
	    }

	    public Object getItem(int position) {
	    	if (prevPage!=0 && position == 0)
	    		return prevPage;
	    	else if (nextPage!=0 && position == getCount()-1)
	    		return nextPage;
	    	else
	    		return posts.get(position - (prevPage!=0 ? 1 : 0));
	    }

	    public long getItemId(int position) {
	        return position;
	    }
	    

		public View getView(int position, View convertView, ViewGroup parent) {
	    	View vi = convertView;
	    	ViewHolder holder = null;
	    	int type = (prevPage!=0 && position == 0 ? TYPE_PREV_PAGE : 
	    				(nextPage!=0 && position == getCount()-1 ? TYPE_NEXT_PAGE : TYPE_POST) );
	    	
	    	if (vi != null) {
	    		holder = (ViewHolder) vi.getTag();

	    		if (holder.type != type) {
	    			vi = null;
	    		}
	    	}
	    	
	    	if (vi == null || holder == null) {
	    		holder = new ViewHolder();
	    		holder.type = type;
	    		
	    		if (type == TYPE_POST) {
	    			vi = inflater.inflate(R.layout.post_row, null);
	    			holder.user = (TextView) vi.findViewById(R.id.user);
	    			holder.date = (TextView) vi.findViewById(R.id.date);
	    			holder.photo = (ImageView) vi.findViewById(R.id.photo);
	    			holder.body = (TextView) vi.findViewById(R.id.webview);
	    			holder.thumbsUp = (TextView) vi.findViewById(R.id.thumbup_value);
	    			holder.thumbsDown = (TextView) vi.findViewById(R.id.thumbdown_value);
	    			holder.leftButton = (View) vi.findViewById(R.id.left);
	    			holder.rightButton = (View) vi.findViewById(R.id.right);
	    			holder.voting = (RelativeLayout) vi.findViewById(R.id.voting);
	    			holder.thumbsUpImg = (ImageView)vi.findViewById(R.id.thumbup);
	    			holder.thumbsDownImg = (ImageView)vi.findViewById(R.id.thumbdown);
	    		}
	    		else {
	    			vi = inflater.inflate(R.layout.prev_next_page_row, null);
	    			holder.body = (TextView) vi.findViewById(R.id.webview);
	    		}
	    		
	    		vi.setTag(holder);
	    	}

	    	if (type == TYPE_PREV_PAGE) {
	    		holder.body.setText(R.string.previous);
	    		vi.setBackgroundResource(R.drawable.grey_gradient);
	    		return vi;
	    	}
	    	
	    	else if (type == TYPE_NEXT_PAGE) {
	    		holder.body.setText(R.string.next);
	    		vi.setBackgroundResource(R.drawable.grey_gradient);
	    		return vi;
	    	}
	    	

	    	if (prevPage>0)
	    		position--;
	    	
	    	TopicPost post = posts.get(position);
	    	
	    	if (!post.hasVoted) {
	    		holder.leftButton.setTag(new Integer(post.postId));
	    		holder.rightButton.setTag(new Integer(post.postId));
	    	}
	    	
	    	holder.user.setText(post.user);
	    	holder.date.setText(post.date);
//	    	holder.body.loadDataWithBaseURL("file:///", post.body, "text/html", "utf-8", "");
	    	holder.body.setText(post.body);
	    	holder.thumbsUp.setText(Integer.toString(post.thumbsUp));
	    	holder.thumbsDown.setText(Integer.toString(post.thumbsDown));
	    	
	    	// for phases other than thread proposing, hide voting for 1st post
	    	// for phase RESULTS, hide voting for last post
	    	if ( (prevPage == 0 && position == 0 && 
	    			phaseId > Utils.PHASE_PROPOSED) ||
	    			(nextPage == 0 && position == getCount()-1 && phaseId == Utils.PHASE_RESULT) )
	    		holder.voting.setVisibility(View.GONE);	    		
	    	else {
	    		holder.voting.setVisibility(View.VISIBLE);
	    	}
	    	
	    	// change thumb icons to agree disagree
	    	// TODO: Change actions also..
	    	if(phaseId > 2) {
	    		holder.thumbsUpImg.setImageResource(R.drawable.agree);
	    		holder.thumbsDownImg.setImageResource(R.drawable.disagree);
	    		holder.thumbsUp.setText(getString(R.string.agree) +" | " + Integer.toString(post.thumbsUp));
		    	holder.thumbsDown.setText(getString(R.string.disagree) +" | " + Integer.toString(post.thumbsDown));
	    	}
	    	
	    	
			if (post.imgUrl != null && post.imgUrl.length() != 0 && !post.imgUrl.equals("null")) {
    	    	// for image load, use background thread with queue and cache
    	    	GlobalApp global = (GlobalApp)getApplicationContext();
    	    	final LoadImagesQueue iq = global.getImagesQueue();

    	    	Bitmap cachedImage = null;
    	    	final ImageView imageView = holder.photo;
    	    	imageView.setTag(post.imgUrl);
        		cachedImage = iq.loadImage(false, post.imgUrl, new ImageLoadedListener() {
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

			if (post.isSolution) {
				if(nextPage == 0 && 
					position == getCount()-1 && 
					phaseId == Utils.PHASE_RESULT) {
					vi.setBackgroundResource(R.color.resultBack);
				}
				else
					vi.setBackgroundResource(R.color.solutionBack);
			}
			else
				vi.setBackgroundResource(android.R.color.white);
			
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
		menu.removeItem(1);
        menu.add(0, 1, 1, R.string.refresh);
                
		menu.removeItem(2);
        menu.add(0, 2, 2, R.string.menu_settings);   
		
        
		menu.removeItem(3);
		menu.removeItem(4);
		
		if (phaseId == Utils.PHASE_OPEN) {
			menu.add(0, 3, 3, R.string.reply);
			menu.add(0, 4, 4, R.string.reply_solution);
		}
		
		menu.removeItem(5);		
		SharedPreferences settings = getSharedPreferences(Utils.prefsFileName, 0);
        String sessionId = settings.getString(Utils.prefsSessionId, "");
        int sessionType = settings.getInt(Utils.prefsSessionType, Utils.SESSION_TYPE_UNDEFINED);
        if (sessionId.length() > 0 && sessionType != Utils.SESSION_TYPE_UNDEFINED) {
        	menu.add(0, 5, 5, R.string.logout);
        }
        
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);
        switch(item.getItemId()) {
        case 1:
        	load((TopicViewAdapter)getListAdapter(), false, false, true);
        	break;
        case 2:
	    	Intent i = new Intent(this, GeneralPreferencesActivity.class);
	    	startActivity(i);
	    	break;        
        case 3:
        	replySolution = false;
        	replyPostOrSolution();
        	break;
        case 4:
        	replySolution = true;
        	replyPostOrSolution();
        	break;
        case 5:
	    	Utils.logoutSession(TopicViewActivity.this);
	        break;
		default:
			break;
        }
        return true;
    }
	
	
	private void replyPostOrSolution() {
    	if (!Utils.isSessionValid(getApplicationContext())) {
            Intent i = new Intent(this, LoginOptionActivity.class);
            startActivityForResult(i, Utils.REQ_LOGIN_TO_REPLY);
        }
        else {
            Intent i = new Intent(this, PostReplyActivity.class);
            i.putExtra("topicId", topicId);
            i.putExtra("replySolution", replySolution);
            startActivityForResult(i, Utils.REPLIED);
        }
	}
	
	public void onHomeClick(View v){
		Utils.goHome(v.getContext());
	}
	
	
}

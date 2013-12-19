package eu.ourspace.UI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import eu.ourspace.R;
import eu.ourspace.Structures.PostReply;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class PostReplyActivity extends Activity {

	private EditText comment;
	private int topicId = -1;
	private boolean replySolution = false; // reply post or solution
	
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("topicId", topicId);
        outState.putBoolean("replySolution", replySolution);
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
			topicId = icicle.getInt("topicId");
			replySolution = icicle.getBoolean("replySolution", false);
		} else {
			final Bundle extras = getIntent().getExtras();
			if (extras != null) {
				topicId = extras.getInt("topicId");
				replySolution = extras.getBoolean("replySolution", false);
			}
		}
		
		// catch error case
		if (topicId == -1) {
			this.finish();
			return;
		}
		
        setContentView(R.layout.reply);
        
        TextView label = (TextView)findViewById(R.id.label);
        label.setText(replySolution ? R.string.reply_solution : R.string.reply);
        
        comment = (EditText)findViewById(R.id.body);
    }
    
    

	public void onPostClick(View v) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null)
        	inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
		
		String body = comment.getText().toString();
		if (body.length() == 0 || body.length() == 0) {
//			Toast.makeText(this, R.string.login_label, Toast.LENGTH_SHORT).show();
			return;
		}
		
		PostReply postReply = new PostReply();
		postReply.body = body.replaceAll("\n", "{[LINE_BREAK]}");
		postReply.threadId = topicId;

		// Show progress dialog
		final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.please_wait));

		// prepare request listener
        UpdateListener updateListener = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				dialog.dismiss();
				// if success, process the result
        		if (!success) {
        			Toast.makeText(PostReplyActivity.this, R.string.reply_fail, Toast.LENGTH_LONG).show();
        		}
        		else {
        			Toast.makeText(PostReplyActivity.this, R.string.reply_success, Toast.LENGTH_LONG).show();
        			
                	Intent resultIntent = new Intent();
                	resultIntent.putExtra("success", true);
                	setResult(Activity.RESULT_OK, resultIntent);
                	PostReplyActivity.this.finish();
        		}
			}
		};
		
		// make the network request
		postReply.sendPost(getApplicationContext(), updateListener);
	}
	
	public void onHomeClick(View v){
		Utils.goHome(v.getContext());
	}
	
}

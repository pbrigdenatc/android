package eu.ourspace.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import eu.ourspace.R;
import eu.ourspace.Structures.FbConnect;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class LoginOptionActivity extends Activity {

	private AlertDialog alert;
	private boolean shouldTerminate = false;
	
	
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		
        setContentView(R.layout.splash);
        
		optionToLogin();
    }
    
    // 
    private void optionToLogin() {
    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.login_option, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.login_required));
		builder.setView(view);
		builder.setMessage(getString(R.string.login_select));
		alert = builder.create();
		alert.show();
    }
    
    
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (shouldTerminate)
			this.finish();
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != Activity.RESULT_OK || data == null)
			return;
		
		boolean success = data.getBooleanExtra("success", false);
		
    	Intent resultIntent = new Intent();
    	resultIntent.putExtra("success", success);
    	setResult(Activity.RESULT_OK, resultIntent);
    	LoginOptionActivity.this.finish();
	}
	
	
	private void loginCompletedSuccessfully() {
    	Intent resultIntent = new Intent();
    	resultIntent.putExtra("success", true);
    	setResult(Activity.RESULT_OK, resultIntent);
    	LoginOptionActivity.this.finish();
	}
	
	
	public void ourSpaceLogin(View v) {
    	if (alert != null)
    		alert.dismiss();
    	
        Intent i = new Intent(this, LoginActivity.class);
        startActivityForResult(i, Utils.LOGIN_OURSPACE);
        shouldTerminate = true;
	}
    
    public void facebookLogin(View v) {
    	if (alert != null)
    		alert.dismiss();
    		
    	FbConnect fbConnect = new FbConnect(this, new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				loginCompletedSuccessfully();
			}
		});
    	fbConnect.FacebookLogin();
    }
    
    public void linkedinLogin(View v) {
    	if (alert != null)
    		alert.dismiss();
    	
        Intent i = new Intent(this, LinkedinLoginActivity.class);
        startActivityForResult(i, Utils.LOGIN_LINKEDIN);
        shouldTerminate = true;
    }
  
    public void twitterLogin(View v) {
    	if (alert != null)
    		alert.dismiss();
    	
        Intent i = new Intent(this, TwitterLoginActivity.class);
        startActivityForResult(i, Utils.LOGIN_TWITTER);
        shouldTerminate = true;
    }
    
}

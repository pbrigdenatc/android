package eu.ourspace.UI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import eu.ourspace.R;
import eu.ourspace.Structures.LoginResult;
import eu.ourspace.Utils.Utils;
import eu.ourspace.Utils.NetworkOperations.UpdateListener;

public class LoginActivity extends Activity {

	private EditText usernameTxt;
	private EditText passwordTxt;
	
	
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		
        setContentView(R.layout.login);
        
    	usernameTxt = (EditText) findViewById(R.id.username);
        passwordTxt = (EditText) findViewById(R.id.password);
        
        // load previous username
		SharedPreferences settings = getSharedPreferences(Utils.prefsFileName, 0);
        String username = settings.getString(Utils.prefsUsername, "");
		
		usernameTxt.setText(username);
    }
    
	
	public void onUsernameClear(View v) {
		usernameTxt.setText("");
	}

	public void onPasswordClear(View v) {
		passwordTxt.setText("");
	}

	public void onConnectClick(View v) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null)
        	inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
		
		String email = usernameTxt.getText().toString();
		String password = passwordTxt.getText().toString();
		if (email.length() == 0 || password.length() == 0) {
			Toast.makeText(this, R.string.login_label, Toast.LENGTH_SHORT).show();
			return;
		}
		
		LoginResult login = new LoginResult();
		login.emailTxt = email;
		login.passwordTxt = password;

		// Show progress dialog
		final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.please_wait));

		// prepare request listener
        UpdateListener updateListener = new UpdateListener() {
			@Override
			public void updateCompleted(boolean success) {
				dialog.dismiss();
				// if success, process the result
        		if (!success)
        			Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
        		else {
        			loginCompletedSuccessfully();
        		}
			}
		};
		
		// make the network request
		login.tryLogin(getApplicationContext(), updateListener);
	}
	
	
	private void loginCompletedSuccessfully() {
    	Intent resultIntent = new Intent();
    	resultIntent.putExtra("success", true);
    	setResult(Activity.RESULT_OK, resultIntent);
    	LoginActivity.this.finish();
	}
	
}

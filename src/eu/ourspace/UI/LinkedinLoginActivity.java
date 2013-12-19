package eu.ourspace.UI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;
import eu.ourspace.R;

public class LinkedinLoginActivity extends Activity {
	
	private WebView webView;
	private static final String CONSUMER_KEY = "3s5m7pmcemdn";
	private static final String CONSUMER_SECRET = "M37z2U74lbNBa1f2";
	private static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
	private static final String OAUTH_CALLBACK_HOST = "callback";
	private static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
    
    private LinkedInApiClientFactory factory;
    private LinkedInOAuthService oAuthService;
    private LinkedInRequestToken liToken;
    private LinkedInApiClient client;
    
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_linkedin);
        
        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        
		// Show progress dialog
		final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.please_wait));
        
        webView.setWebChromeClient(new WebChromeClient() {
    		public void onProgressChanged(WebView view, int progress) {
    			if (progress == 100) {
    				if (dialog != null) {
    					try {
    						dialog.dismiss();
    					} catch (Exception e) {}
    				}
    			}
    		}
    	});
        
        oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(CONSUMER_KEY, CONSUMER_SECRET);
        factory = LinkedInApiClientFactory.newInstance(CONSUMER_KEY, CONSUMER_SECRET);
        
        Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
		        	liToken = oAuthService.getOAuthRequestToken(OAUTH_CALLBACK_URL);
//		        	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(liToken.getAuthorizationUrl()));
//		        	startActivity(i);
		        	webView.loadUrl(liToken.getAuthorizationUrl());
		        } catch (Exception e) {
    				if (dialog != null) {
    					try {
    						dialog.dismiss();
    						LinkedinLoginActivity.this.finish();
    					} catch (Exception ee) {}
    				}
		        }
			}
		});
        t.start();
    }
    
    
	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			webView.clearCache(true);
			webView.clearHistory();
		}
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
	    // Check if this is a callback from OAuth
	    Uri uri = intent.getData();
	    if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME)) {
	    	try {
				String verifier = intent.getData().getQueryParameter("oauth_verifier");
				LinkedInAccessToken accessToken = oAuthService.getOAuthAccessToken(liToken, verifier); 

				// TEST				
				client = factory.createLinkedInApiClient(accessToken);
				client.postNetworkUpdate("LinkedIn Android app test");
				Person profile = client.getProfileForCurrentUser();
				Toast.makeText(this, profile.getLastName() + "_" + accessToken.getExpirationTime().toGMTString(), Toast.LENGTH_LONG).show();
				
				// finish
				loginCompletedSuccessfully();
			
	    	} catch (Exception e) {
	    	}
	    }
	}
    
	
	private void loginCompletedSuccessfully() {
    	Intent resultIntent = new Intent();
    	resultIntent.putExtra("success", true);
    	setResult(Activity.RESULT_OK, resultIntent);
    	LinkedinLoginActivity.this.finish();
	}
}

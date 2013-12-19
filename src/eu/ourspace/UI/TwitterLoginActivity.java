package eu.ourspace.UI;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import eu.ourspace.R;
import eu.ourspace.Utils.Utils;

public class TwitterLoginActivity extends Activity {
	
	private WebView webView;
	private static final String CONSUMER_KEY = "qovA1aMDyxoUKv7iPjgA";
	private static final String CONSUMER_SECRET = "3GTcp8UHiAN6hwKfHBgok8tAziboy7I6QFiMB9EFtU";
	private static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-twitter";
	private static final String OAUTH_CALLBACK_HOST = "callback";
	private static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	private static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	private static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	private static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

	private CommonsHttpOAuthConsumer consumer;
	private CommonsHttpOAuthProvider provider;
    
    
	
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
        
        
        Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
		            consumer = new CommonsHttpOAuthConsumer(
		            		CONSUMER_KEY, CONSUMER_SECRET);
		            provider = new CommonsHttpOAuthProvider(
		            		REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
		            String authURL = provider.retrieveRequestToken(
							consumer, OAUTH_CALLBACK_URL);
			    	webView.loadUrl(authURL);
		        } catch (Exception e) {
    				if (dialog != null) {
    					try {
    						dialog.dismiss();
    						TwitterLoginActivity.this.finish();
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
	    		String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				provider.retrieveAccessToken(consumer, verifier);
				String key_token = consumer.getToken();
				String secret_token = consumer.getTokenSecret();

				// TEST
				Toast.makeText(this, key_token + "_" + secret_token, Toast.LENGTH_LONG).show();
				
				loginCompletedSuccessfully();
			
	    	} catch (Exception e) {
	    	}
	    }
	}
    
	private void loginCompletedSuccessfully() {
    	Intent resultIntent = new Intent();
    	resultIntent.putExtra("success", true);
    	setResult(Activity.RESULT_OK, resultIntent);
    	TwitterLoginActivity.this.finish();
	}
	
	public void onHomeClick(View v){
		Utils.goHome(v.getContext());
	}
}

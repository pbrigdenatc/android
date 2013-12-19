package eu.ourspace.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.util.Log;

public final class NetworkOperations {

	// hidden empty constructor
	private NetworkOperations() {
	}

	public interface UpdateListener {
		void updateCompleted(boolean success);
	}
	
	public interface NetworkResponseListener {
		void handleNetworkResponse(String result);
	}
	
	// general network operation - ASYNC
	public static void retrieveDataAsync(final Context ctx, final String serviceUrlString,
			final String args, final NetworkResponseListener networkResponseListener, final boolean getOrPost) {
        new Thread() {
            public void run() {
            	networkResponseListener.handleNetworkResponse(NetworkOperations.retrieveData(ctx, serviceUrlString, args, getOrPost));
            }
        }.start();
	}
	
	
	// use GET method as default
	public static void retrieveDataAsync(final Context ctx, final String serviceUrlString, final String args, final NetworkResponseListener networkResponseListener) {
        retrieveDataAsync(ctx, serviceUrlString, args, networkResponseListener, true);
	}

	@SuppressWarnings("all")
	// general network operation - SYNC
	public static String retrieveData(Context ctx, String serviceUrlString, String args, boolean getOrPost) {
		if (Utils.LOG) { Log.i("*** NetworkOperations ***", "Request URL: " + serviceUrlString + args); }
				
    	String urlString = serviceUrlString + (getOrPost ? args : "");
    	
		InputStream is = null;
		String res = null;
		byte[] resArray = null;
		OutputStreamWriter wr = null;
		
		try {
			URL url = new URL(urlString);
			final URLConnection conn = url.openConnection();
			
			// POST method request
			if (!getOrPost) {
			    conn.setDoOutput(true);
			    wr = new OutputStreamWriter(conn.getOutputStream());
			    wr.write(args);
			    wr.flush();
			}
			
			// setting these timeouts ensures the client does not deadlock indefinitely
			// when the server has problems.
			conn.setConnectTimeout(Utils.timeoutNetworkConnect);
			conn.setReadTimeout(Utils.timeoutNetworkRead);
			conn.connect();
			
			is = conn.getInputStream();
			res = new String(Utils.convertStreamToString(is));

		} catch (SocketTimeoutException e) {
			if (Utils.LOG) { Log.e("NetworkOperations" ,e.toString()); }
		} catch (MalformedURLException e) {
			if (Utils.LOG) { Log.e("NetworkOperations" ,e.toString()); }
		} catch (IOException e) {
			if (Utils.LOG) { Log.e("NetworkOperations" ,e.toString()); }
		} catch (Exception e) {
			if (Utils.LOG) { Log.e("NetworkOperations" ,e.toString()); }
		} finally {
			try {
				if( wr != null )
					wr.close();
				if( is != null )
					is.close();
			} catch (IOException e) {
				if (Utils.LOG) { Log.w("NetworkOperations" ,e.toString()); }
			}
		}
		
		if (Utils.LOG && res != null)
				Log.i("*** NetworkOperations ***", "response: " + res);
		
		return res;
	}

	

	
	
}

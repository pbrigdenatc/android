package eu.ourspace.Utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.android.Util;
import com.google.common.io.ByteStreams;
import eu.ourspace.R;
import eu.ourspace.Structures.FbConnect;
import eu.ourspace.UI.HomeActivity;

public final class Utils {
	public static final boolean LOG = true; // set it to true only for testing
	
	public static final String protocol = "http://";
	public static final String serverName = "ws.joinourspace.eu";
	//public static final String serverName = "10.1.1.236:8080";
	
	public static final String topTopicsUrl = protocol + serverName + "/TopTopics.aspx?";
	public static final String recentActivityUrl = protocol + serverName + "/RecentActivity.aspx?";
	public static final String topicPostsUrl = protocol + serverName + "/ThreadAndReplies.aspx?";
	public static final String forumsUrl = protocol + serverName + "/ForumList.aspx?";
	public static final String threadsUrl = protocol + serverName + "/ThreadList_ForumId.aspx?";
	public static final String threadsProposedUrl = protocol + serverName + "/ThreadList_PhaseId.aspx?";
	public static final String loginUrl = protocol + serverName + "/LogIn.aspx?";
	public static final String postReplyUrl = protocol + serverName + "/ForumPostAdd.aspx?";
	public static final String voteUrl = protocol + serverName + "/ForumPostThumbsUpdate.aspx?";
	

	
	// preferences strings
	public static final String prefsFileName = "eu.ourspace_preferences";

	
	public static final String prefsAppLanguage = "appLanguage";
	public static final String prefsContentLangAny = "contentLangAny";
	public static final String prefsUsername = "username";
	public static final String prefsPassword = "password";
	public static final String prefsSessionId = "sessionId";
	public static final String prefsSessionType = "sessionType";
	public static final String prefsSessionExpire = "sessionExpire";
	
	// enum for prefsSessionType
	public static final int SESSION_TYPE_UNDEFINED = -1;
	public static final int SESSION_TYPE_BACKEND = 0;
	public static final int SESSION_TYPE_FACEBOOK = 1;
	public static final int SESSION_TYPE_LINKEDIN = 2;
	public static final int SESSION_TYPE_TWITTER = 3;
	
	// phases
	public static final int PHASE_PROPOSED = 1;
	public static final int PHASE_OPEN = 2;
	public static final int PHASE_SOLUTIONS = 3;
	public static final int PHASE_RESULT = 4;
    
	
	public static final String charsetForXML = "utf-8";
	public static final Locale dbLocale = new Locale("el", "GR");
	// date in XML files from backend are in english language, so we need to parse them with that locale and not device's locale
	public static final Locale dateLocale = new Locale("en", "US"); 
	
    
	public static final String DateLongFormat = "EEE d MMM yyyy HH:mm";
	public static final String DateTimePickerFormat = "d MMM  HH:mm";
	public static final String DateShortFormat = "EEE d MMM yyy";
	public static final String DateRetrievedFormat = "yyyyMMdd HHmm"; // 'T' isn't allowed here, so replace with ' '
	public static final String DateSaveFormat = "yyyy-MM-dd HH:mm:ss"; //  iso8601 format
	public static final String DateSimpleFormat = "dd/MM/yyyy";
	
	// SimpleDateFormat constructor is very expensive to use.
	// Initiate once and use it all over
	// Beware, SimpleDateFormat isn't thread safe
	public static final SimpleDateFormat SimpleDateLongFormat = new SimpleDateFormat(Utils.DateLongFormat, Locale.getDefault());
	public static final SimpleDateFormat SimpleDateTimePickerFormat = new SimpleDateFormat(Utils.DateTimePickerFormat, Locale.getDefault());
	public static final SimpleDateFormat SimpleDateShortFormat = new SimpleDateFormat(Utils.DateShortFormat, Locale.getDefault());
	public static final SimpleDateFormat SimpleDateRetrievedFormat = new SimpleDateFormat(Utils.DateRetrievedFormat);
	public static final SimpleDateFormat SimpleDateSaveFormat = new SimpleDateFormat(Utils.DateSaveFormat, dbLocale);
	public static final SimpleDateFormat SimpleDateSimpleFormat = new SimpleDateFormat(Utils.DateSimpleFormat, dbLocale);
	

	// network timeout values
	public static final int timeoutNetworkConnect = 10000; // 10 seconds
	public static final int timeoutNetworkRead = 20000;
	
	
	// req. codes for startActivityForResult
	public static final int REQ_LOGIN_TO_VOTE = 1;
	public static final int REQ_LOGIN_TO_REPLY = 2;
	public static final int REPLIED = 3;
	public static final int LOGIN_OURSPACE = 4;
	public static final int LOGIN_LINKEDIN = 5;
	public static final int LOGIN_TWITTER = 6;
	
	
	// hidden empty constructor
	private Utils() {
	}
    
    public static String convertStreamToString(InputStream is) {
    	return convertStreamToString(is, "UTF-8");
    }
    
    public static String convertStreamToString(InputStream is, String encoding) {
        StringBuilder sb = new StringBuilder();

        try {
        	final char[] buffer = new char[0x10000];
        	Reader in = new InputStreamReader(is, encoding);
        	int read;
        	do {
        	  read = in.read(buffer, 0, buffer.length);
        	  if (read>0) {
        	    sb.append(buffer, 0, read);
        	  }
        	} while (read>=0);
        	
        	in.close();
        } catch(IOException e){
        	if (Utils.LOG) { Log.e("Utils", e.toString()); }
        } catch(Exception e){
        	if (Utils.LOG) { Log.e("Utils", e.toString()); }
        }
        
        return sb.toString();
    }
    
    
    public static byte[] convertStreamToByteArray(final InputStream is) {
    	try {
			return ByteStreams.toByteArray(is);
		} catch (IOException e) {
			if (Utils.LOG) { Log.e("Utils", e.toString()); }
			return new byte[] {};
		}
    }
    
    
    
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        Adapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem != null) {
            	listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            	totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    
   
	
	// distinguish left/right button click on handler of below method
	public static final int LEFT_BUTTON_CLICK = 0;
	public static final int RIGHT_BUTTON_CLICK = 1;

	// confirmation dialog to delete all saved articles
	public static void confirmationDialog(Context ctx, String title, String message, 
			String leftButton, String rightButton, final Handler h) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		if (title != null)
			builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(leftButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
		    	dialog.dismiss(); // dismiss dialog
		    	if (h != null)
		    		h.sendEmptyMessage(LEFT_BUTTON_CLICK);
			}
		});
		builder.setNegativeButton(rightButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
		    	dialog.dismiss(); // dismiss dialog
		    	if (h != null)
		    		h.sendEmptyMessage(RIGHT_BUTTON_CLICK);
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}
	

	// clear sessionId
	public static void logoutSession(Activity activity) {
		SharedPreferences settings = activity.getSharedPreferences(Utils.prefsFileName, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Utils.prefsSessionId, "");
		editor.putInt(Utils.prefsSessionType, SESSION_TYPE_UNDEFINED);
		editor.putLong(Utils.prefsSessionExpire, 1);
		editor.commit();		
		
		// remove user greeting
    	TextView greeting = (TextView) activity.findViewById(R.id.title_greeting);
    	greeting.setText("");
		
		FbConnect fbConnect = new FbConnect(activity, null);
		fbConnect.FacebookLogout();
	}
	
	public static boolean isSessionValid(Context ctx) {
		SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
        String sessionId = settings.getString(Utils.prefsSessionId, "");
        int sessionType = settings.getInt(Utils.prefsSessionType, Utils.SESSION_TYPE_UNDEFINED);
        long expire = settings.getLong(Utils.prefsSessionExpire, 1);
        if (sessionType == Utils.SESSION_TYPE_UNDEFINED || 
        		sessionId.length() == 0 || expire < System.currentTimeMillis())
        	return false;
        else
        	return true;
	}
	
	public static String getUsername(Context ctx)  {
		SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
		if(settings.getInt(Utils.prefsSessionType, -1) == SESSION_TYPE_UNDEFINED) return "";		
        return settings.getString(Utils.prefsUsername, "");        
	}
	
	public static int getsessionType(Context ctx) {
		SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
		return settings.getInt(Utils.prefsSessionType, -1);
	}

    
    // set application language
	public static void setAppLanguage(final Context ctx) {
		final String defaultLang = ctx.getResources().getConfiguration().locale.getLanguage();
		final SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
		final String lang = settings.getString(Utils.prefsAppLanguage, defaultLang);
		
		final Configuration config = ctx.getResources().getConfiguration();
		
//        if (lang.length() != 0 && !config.locale.getLanguage().equals(lang)) {
        	// clear already read db
//        	final GlobalApp global = ((GlobalApp) ctx.getApplicationContext());
//        	global.clear();
//        }
        
//        Log.e("***", config.locale.getLanguage() + " -> " + lang);

    	// set new locale
        final Locale locale = new Locale(lang);
//        Locale.setDefault(locale);
        config.locale = locale;
//        ctx.getResources().updateConfiguration(config, ctx.getResources().getDisplayMetrics());
        ctx.getResources().updateConfiguration(config, null);
	}
    
	// get language, for content ... can be ANY if we need all content
    public static String getLang(final Context ctx) {
//    	String lang = Locale.getDefault().getLanguage();
    	
		final String defaultLang = ctx.getResources().getConfiguration().locale.getLanguage();
		final SharedPreferences settings = ctx.getSharedPreferences(Utils.prefsFileName, 0);
		boolean contentLangAny = settings.getBoolean(Utils.prefsContentLangAny, false);
		
		if (contentLangAny)
			return "any";
		
		String lang = settings.getString(Utils.prefsAppLanguage, defaultLang);
    	if (!lang.equals("el") && !lang.equals("de") && !lang.equals("cs"))
    		lang = "en"; // default language for any other case
    	
    	return lang;
    }
	
    // language for the application (default is from device locale)
    public static String getAppLang(final Context ctx) {
		String lang = ctx.getResources().getConfiguration().locale.getLanguage();
		
    	if (!lang.equals("el") && !lang.equals("de") && !lang.equals("cs"))
    		lang = "en"; // default language for any other case
    	
    	return lang;
    }
    
	
	// listener when selecting an option from general selection
	public interface SelectionListener {
		void selectOption(int position);
	}
	
	/**
     * Invoke "home" action, returning to {@link HomeActivity}.
     */
    public static void goHome(Context context) {
        final Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
	
	

}

package eu.ourspace.UI;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import eu.ourspace.GlobalApp;
import eu.ourspace.R;
import eu.ourspace.Databases.LocalDB;
import eu.ourspace.Utils.Utils;


public class GeneralPreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private ListPreference mListPreferenceLanguage;
    private CheckBoxPreference mCheckPreferenceAnyLanguage;
    private Preference mPreferenceCache;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.general_preferences);

        // Get a reference to the preferences
        mListPreferenceLanguage = (ListPreference)getPreferenceScreen().findPreference(Utils.prefsAppLanguage);
        mCheckPreferenceAnyLanguage = (CheckBoxPreference)getPreferenceScreen().findPreference(Utils.prefsContentLangAny);
        mPreferenceCache = getPreferenceScreen().findPreference("clearCache");

        mListPreferenceLanguage.setDefaultValue(Utils.getAppLang(this));
        
        mCheckPreferenceAnyLanguage.setDefaultValue(true);
        
        mPreferenceCache.setOnPreferenceClickListener(new DialogPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				confirmClearCache();
				return false;
			}
		});
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Utils.setAppLanguage(getApplicationContext());
	}
    
    
    private void confirmClearCache() {
        final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == Utils.LEFT_BUTTON_CLICK) {
					clearCache();
				}
			}
        };
        
        Utils.confirmationDialog(this, getString(R.string.clear_cache), getString(R.string.clear_cache_verify),
        		getString(R.string.ok), getString(R.string.cancel), handler);
    }
    
    private void clearCache() {
    	GlobalApp global = (GlobalApp) getApplicationContext();
    	final LocalDB db = global.getDb();
    	
    	// delete all data in database
    	if (db!=null) {
    		
    		// Show progress dialog
            final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.please_wait));
            
            Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
		    		db.deleteAll(LocalDB.FORUMS_TABLE);
		    		db.deleteAll(LocalDB.OPENTOPICS_TABLE);
		    		db.deleteAll(LocalDB.POSTS_TABLE);
		    		db.deleteAll(LocalDB.PROPOSEDTOPICS_TABLE);
		    		db.deleteAll(LocalDB.RECENTACTIVITY_TABLE);
		    		db.deleteAll(LocalDB.SOLUTIONSTOPICS_TABLE);
		    		db.deleteAll(LocalDB.RESULTSTOPICS_TABLE);
		    		db.deleteAll(LocalDB.TOPTOPICS_TABLE);
		    		dialog.dismiss();
				}
			});
            t.start();
    	}
    }
    

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        String valueIn, valueOut;
        String[] arrayValues, arrayEntries;
        
        valueIn = getPreferenceScreen().getSharedPreferences().getString(Utils.prefsAppLanguage, Utils.getAppLang(this));
        arrayValues = getResources().getStringArray(R.array.entryvalues_preference_languages);
        arrayEntries = getResources().getStringArray(R.array.entries_preference_languages);
        valueOut = getEntriesForEntryValue(valueIn, arrayValues, arrayEntries);
        mListPreferenceLanguage.setSummary(valueOut);
        mListPreferenceLanguage.setValue(valueIn);
        
        mCheckPreferenceAnyLanguage.setChecked(getPreferenceScreen().getSharedPreferences().getBoolean(Utils.prefsContentLangAny, true));
        
        
        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        
        super.onPause();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Preference pref = findPreference(key);

	    if (pref instanceof ListPreference) {
	        ListPreference listPref = (ListPreference) pref;
	        pref.setSummary(listPref.getEntry());
	    }
    }
    
    public String getEntriesForEntryValue(String valueIn, String[] arrayValues, String[] arrayEntries) {
    	if (valueIn == null)
    		return "";
    	
    	String valueOut = "";
    	
    	int i = 0;
        for (i=0; i<arrayValues.length; i++) {
        	if (arrayValues[i].equals(valueIn))
        		break;
    	}
        
        if (i < arrayEntries.length)
        	valueOut = arrayEntries[i];
        
        return valueOut;
    }
}
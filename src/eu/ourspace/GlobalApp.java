package eu.ourspace;



import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import eu.ourspace.Databases.LocalDB;
import eu.ourspace.Utils.LoadImagesQueue;
import eu.ourspace.Utils.Utils;

@ReportsCrashes(formKey = "dGFXT1ZleTlrQV82T09mNXhDTFYyNGc6MQ") 


public class GlobalApp extends Application {
	
	private LoadImagesQueue iq = null;
	private LocalDB localDb = null;

	public LoadImagesQueue getImagesQueue() {
		if (iq == null)
			iq = new LoadImagesQueue();
		return iq;
	}
	
	public LocalDB getDb() {
		if (localDb == null)
			localDb = new LocalDB(this);
		
		return localDb;
	}

	
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		
		Utils.setAppLanguage(this);
		
		super.onCreate();
		// should be clean, so app doesn't delay when starting
	}
	
	@Override
	public void onLowMemory() {
		if (iq != null)
			iq.clear();
	}
	
}


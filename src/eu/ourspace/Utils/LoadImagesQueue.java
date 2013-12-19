package eu.ourspace.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

public class LoadImagesQueue {
	private static final String TAG = "LoadImagesQueue";
		
	// Global cache of images.
	// Using SoftReference to allow garbage collector to clean cache if needed
	private final HashMap<String, WeakReference<Bitmap>> Cache = new HashMap<String,  WeakReference<Bitmap>>();

	private static final class QueueItem {
		public String name = "";
		public ImageLoadedListener listener;
	}
	private final ArrayList<QueueItem> Queue = new ArrayList<QueueItem>();

	private final Handler handler = new Handler();	// Assumes that this is started from the main (UI) thread
	private Thread thread;
	private QueueRunner runner = new QueueRunner();

	/** Creates a new instance of the ImageThreadLoader */
	public LoadImagesQueue() {
		thread = new Thread(runner);
//		thread.start();
		thread.setPriority(Thread.NORM_PRIORITY-1);
	}

	/**
	 * Defines an interface for a callback that will handle
	 * responses from the thread loader when an image is done
	 * being loaded.
	 */
	public interface ImageLoadedListener {
		void imageLoaded(Bitmap imageBitmap, String name);
	}

	/**
	 * Provides a Runnable class to handle loading
	 * the image from the URL and settings the
	 * ImageView on the UI thread.
	 */
	private class QueueRunner implements Runnable {
		public void run() {
			synchronized(this) {
//			  while (true) {
				while(Queue.size() > 0) {
				  final QueueItem item = Queue.remove(0);

				  if (item!=null && item.name!=null) {
					// if cache is cleaned, bitmap will be empty
					// in that case, remove object from cache in order to download image again
					if( Cache.containsKey(item.name) && Cache.get(item.name) != null) {
						final WeakReference<Bitmap> myref = Cache.get(item.name);
						if( myref == null || myref.get() == null) {
							Cache.remove(item.name);
						}
					}
					
					// If in the cache, return that copy and be done
					if( Cache.containsKey(item.name) && Cache.get(item.name) != null) {
						// Use a handler to get back onto the UI thread for the update
						handler.post(new Runnable() {
							public void run() {
								if( item.listener != null ) {
									// NB: There's a potential race condition here where the cache item could get
									//     garbage collected between when we post the runnable and it's executed.
									//     Ideally we would load re-run the network load or something.
									final WeakReference<Bitmap> ref = Cache.get(item.name);
									if( ref != null ) {
										item.listener.imageLoaded(ref.get(), item.name);
									}
								}
							}
						});
					} else {
						
						// retrieve image from network
						final Bitmap bmp = readBitmapFromNetwork(item.name);
						if( bmp != null ) {
							Cache.put(item.name, new WeakReference<Bitmap>(bmp));

							// Use a handler to get back onto the UI thread for the update
							handler.post(new Runnable() {
								public void run() {
									if( item.listener != null ) {
										item.listener.imageLoaded(bmp, item.name);
									}
								}
							});
							
							// find queue entries for item that was just download,
							// and show them too in their view
							int index = 0;
							for (index=Queue.size()-1; index>=0; index--) {
								if ( !(Queue.get(index) instanceof QueueItem))
									continue;
								QueueItem myItem = Queue.get(index);
								if( myItem!=null && myItem.name!=null &&
										item!=null && item.name!=null &&
										myItem.name.equals(item.name) ) {
									final QueueItem qItem = Queue.remove(index);
									handler.post(new Runnable() {
										public void run() {
											if( qItem.listener != null ) {
												qItem.listener.imageLoaded(bmp, qItem.name);
											}
										}
									});
								}
							}
						}

					}
				  }

				}
//			  } // while (true)
			}
		}
	}

	/**
	 * Queues up a URI to load an image from for a given image view.
	 *
	 * @param uri	The URI source of the image
	 * @param callback	The listener class to call when the image is loaded
	 * @throws MalformedURLException If the provided uri cannot be parsed
	 * @return A Bitmap image if the image is in the cache, else null.
	 */
	public Bitmap loadImage(final boolean addToTop, final String name, final ImageLoadedListener listener) {
		if (Cache.size() > 150)
			Cache.clear();
				
		// If it's in the cache, just get it and quit it
		if( Cache.containsKey(name) && Cache.get(name) != null) {
			final WeakReference<Bitmap> ref = Cache.get(name);
			if( ref != null && ref.get() != null) {
				return ref.get();
			}
		}

		QueueItem item = new QueueItem();
		item.name = name;
		item.listener = listener;
		// a request with addToTop should be placed on the top of queue for immediate response (like single article view)
//		if (addToTop)
			Queue.add(0, item);
//		else
//			Queue.add(item);

		// start the thread if needed
		if( thread.getState() == State.NEW) {
			thread.start();
		} else if( thread.getState() == State.TERMINATED) {
			thread = new Thread(runner);
			thread.start();
		}
		return null;
	}
	

	public Bitmap loadImageOnlyIfCached(final String name) {
		// If it's in the cache, just get it and quit it
		if( Cache.containsKey(name) && Cache.get(name) != null) {
			final WeakReference<Bitmap> ref = Cache.get(name);
			if( ref != null && ref.get() != null) {
				return ref.get();
			}
		}

		return null;
	}
	

	

	/**
	 * Convenience method to retrieve a bitmap image from
	 * a URL over the network. The built-in methods do
	 * not seem to work, as they return a FileNotFound
	 * exception.
	 *
	 * Note that this does not perform any threading --
	 * it blocks the call while retrieving the data.
	 *
	 * @param url The URL to read the bitmap from.
	 * @return A Bitmap image or null if an error occurs.
	 */
	public static Bitmap readBitmapFromNetwork(String urlString) {
		if (urlString == null)
			return null;
		
		InputStream is = null;
		BufferedInputStream bis = null;
		Bitmap bmp = null;
		
		if (Utils.LOG)
			Log.i("* NetworkOperation *", urlString);
		
		try {
			final URL url = new URL(urlString);
			final URLConnection conn = url.openConnection();
			// setting these timeouts ensures the client does not deadlock indefinitely
			// when the server has problems.
			conn.setConnectTimeout(Utils.timeoutNetworkConnect);
			conn.connect();
			
			conn.setReadTimeout(Utils.timeoutNetworkRead);
			
			is = conn.getInputStream();
			bis = new BufferedInputStream(is);
			bmp = BitmapFactory.decodeStream(bis);
		} catch (MalformedURLException e) {
			if (Utils.LOG) { Log.e(TAG, "Bad ad URL", e); }
		} catch (IOException e) {
			if (Utils.LOG) { Log.e(TAG, "Could not get remote ad image", e); }
		} finally {
			try {
				if( bis != null )
					bis.close();
				if( is != null )
					is.close();
			} catch (IOException e) {
				if (Utils.LOG) { Log.w(TAG, "Error closing stream."); }
			}
		}
		return bmp;
	}
	
	
	// Clear cache
	public void clear() {
		Cache.clear();
		synchronized (this) {
			Queue.clear();
		}
	}

}

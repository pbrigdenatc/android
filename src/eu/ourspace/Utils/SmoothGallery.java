package eu.ourspace.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.Gallery;


public class SmoothGallery extends Gallery {

	private int velocityLimit = 600;
    
    public SmoothGallery(Context context) {
        super(context);
        calcVelocityLimit();
    }

    public SmoothGallery(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.galleryStyle);
    }

    public SmoothGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        calcVelocityLimit();
    }
    
    private void calcVelocityLimit() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
		velocityLimit = (int) (600 * metrics.density);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//    	if (Utils.LOG) { Log.e("**", Float.toString(velocityX)); }
    	float newVelocityX;
    	if (Math.abs(velocityX) > velocityLimit) {
    		// max positive
    		if (velocityX > 0)
    			newVelocityX = velocityLimit;
    		// max negative
    		else
    			newVelocityX = -velocityLimit;
    	}
    	// or no change
    	else
    		newVelocityX = velocityX;
    	
        return super.onFling(e1, e2, newVelocityX, velocityY);
    }
    
}

package eu.ourspace.UI;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import eu.ourspace.R;
import eu.ourspace.Utils.Utils;

public class MainTabActivity extends TabActivity {
	
	
	
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		
        Utils.setAppLanguage(this);
        
        setContentView(R.layout.main_tabs);
		
        
        final TabHost tabHost = getTabHost();

        tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

        Intent i1, i2, i3, i4, i5;
        i1 = new Intent(this, OverviewListActivity.class);
        i2 = new Intent(this, ProposeActivity.class);
        i2.putExtra("phaseId", Utils.PHASE_PROPOSED);
        i3 = new Intent(this, ForumsListActivity.class);
        i4 = new Intent(this, ProposeActivity.class);
        i4.putExtra("phaseId", Utils.PHASE_SOLUTIONS);
        i5 = new Intent(this, ProposeActivity.class);
        i5.putExtra("phaseId", Utils.PHASE_RESULT);
        
        tabHost.addTab(tabHost.newTabSpec("tab1")
        		.setIndicator(createIndicatorView(getString(R.string.overview_title), getResources().getDrawable(R.drawable.ic_tabs_home)))
                .setContent(i1) );

        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(createIndicatorView(getString(R.string.propose_tab_title), getResources().getDrawable(R.drawable.ic_tabs_post)))
                .setContent(i2) );

        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator(createIndicatorView(getString(R.string.join_tab_title), getResources().getDrawable(R.drawable.ic_tabs_view)))
                .setContent(i3) );
        
        tabHost.addTab(tabHost.newTabSpec("tab4")
                .setIndicator(createIndicatorView(getString(R.string.vote_tab_title), getResources().getDrawable(R.drawable.ic_tabs_vote)))
                .setContent(i4) );
        
        tabHost.addTab(tabHost.newTabSpec("tab5")
                .setIndicator(createIndicatorView(getString(R.string.results_tab_title), getResources().getDrawable(R.drawable.ic_tabs_results)))
                .setContent(i5) );
        
        tabHost.setCurrentTab(0);
        
    }

    @Override
	protected void onResume() {
		super.onResume();
		
		Utils.setAppLanguage(this);
	}
    

    // Workaround
    // Creating my own IndicatorView, forces android not to draw tabStrip (the API option was added later)
    // In this way we may also customize the tabWidget appearance
    public View createIndicatorView(CharSequence label, Drawable icon) {
        final CharSequence mLabel = label;
        final Drawable mIcon = icon;
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tabIndicator = inflater.inflate(R.layout.tab_indicator, (TabWidget) findViewById(android.R.id.tabs), false);

        final TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
        tv.setText(mLabel);

        final ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.icon);
        iconView.setImageDrawable(mIcon);

        return tabIndicator;
    }
    

}

package jp.campus_ar.campusar.page;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.webkit.WebView;

import jp.campus_ar.campusar.R;

public class    HelpPage extends ActionBarActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_help);

		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle("ヘルプ");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		WebView view = (WebView) findViewById(R.id.webView);
		view.loadUrl("http://campus-ar.jp/help");
		view.getSettings().setLoadWithOverviewMode(true);
		view.getSettings().setUseWideViewPort(true);
		view.setInitialScale(1);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

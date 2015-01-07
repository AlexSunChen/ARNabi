package jp.campus_ar.campusar.page;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.layer.SearchResultCategoryLayer;
import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.SearchResultCategory;
import jp.campus_ar.campusar.util.OpenURI;

public class SearchResultPage extends FragmentActivity implements SearchResultCategoryLayer.OnEntryItemSelectedListener {

	private class SearchResultAdapter extends FragmentStatePagerAdapter {

		public SearchResultAdapter(FragmentManager fm) {
			super(fm);
		}

		public Fragment getItem(int i) {
			SearchResultCategoryLayer frag = new SearchResultCategoryLayer();
			frag.registEntries(SearchResultPage.this, results[i].entries);
			frag.setOnEntryItemSelectedListener(SearchResultPage.this);
			return frag;
		}

		public int getCount() {
			return results.length;
		}

		public CharSequence getPageTitle(int position) {
			return results[position].name;
		}
	}

	private String query;
	private ViewPager viewPager;
	private SearchResultCategory[] results;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_search_result);
		viewPager = (ViewPager) findViewById(R.id.pager);
		query = getIntent().getStringExtra("query");
		int facilityIndex = getIntent().getIntExtra("facility", 1);

		getActionBar().setTitle("検索中");
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		doSearch(query, facilityIndex);
	}

	private void doSearch(String q, int facilityIndex) {
		HashMap<String, String> params = new HashMap<>();
		params.put("q", q);
		params.put("facility_id", facilityIndex + "");

		OpenURI.open(this, "/api/entries/search", "GET", params, (success, data) -> {
            if (!success) {
                Toast.makeText(SearchResultPage.this, "検索に失敗しました", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            results = new Gson().fromJson(data, SearchResultCategory[].class);
            buildResult();
        });
	}

	public void onEntryItemSelected(Entry entry) {
		Intent i = new Intent();
		i.putExtra("entry", entry);
		setResult(RESULT_OK, i);
		finish();
	}

	private void buildResult() {
		getActionBar().setTitle(query + " の検索結果");
		viewPager.setAdapter(new SearchResultAdapter(getSupportFragmentManager()));
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

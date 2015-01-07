package jp.campus_ar.campusar.page;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.model.Entry;
import jp.campus_ar.campusar.model.io.ArrayCallbackAdapter;
import jp.campus_ar.campusar.util.BookmarkUtil;

public class BookmarkPage extends ActionBarActivity {

	private ArrayCallbackAdapter<Entry> adapter;
	private ListView listView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_facility);

		int category = getIntent().getIntExtra("category", Entry.TYPE_FAVORITE);
		String title = getIntent().getStringExtra("title");

		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.listView);
		adapter = new ArrayCallbackAdapter<>(this, R.layout.listitem_entry);
		BookmarkUtil bu = new BookmarkUtil(this);
		adapter.set(category == Entry.TYPE_FAVORITE ? bu.getFavorite() : bu.getHistory());
		adapter.setOnViewCreateListener((view, row) -> {
            Entry e = (Entry) row;
            TextView nameView = (TextView) view.findViewById(R.id.nameView);
            TextView detailView = (TextView) view.findViewById(R.id.detailView);
            nameView.setText(e.name);
            detailView.setText(e.detail);
            return view;
        });
		listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Entry entry = adapter.getItem(i);
            Intent intent = new Intent();

            intent.putExtra("entry", entry);
            setResult(RESULT_OK, intent);
            finish();
        });
		listView.setAdapter(adapter);
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

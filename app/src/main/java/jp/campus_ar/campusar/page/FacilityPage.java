package jp.campus_ar.campusar.page;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.model.Facility;
import jp.campus_ar.campusar.model.io.ArrayCallbackAdapter;
import jp.campus_ar.campusar.util.FacilityUtil;

public class FacilityPage extends ActionBarActivity {

	private ArrayCallbackAdapter<Facility> adapter;
	private ListView listView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_facility);

		final int facilityIndex = getIntent().getIntExtra("facility_index", 1);

		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle("施設を選択");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.listView);

		adapter = new ArrayCallbackAdapter<>(this, R.layout.listitem_checkable);
		for (int i = 0, ii = FacilityUtil.FACILITIES.length; i < ii; i++) {
			adapter.add(FacilityUtil.FACILITIES[i]);
		}
		adapter.setOnViewCreateListener((view, row) -> {
            Facility facility = (Facility) row;
            TextView textView = (TextView) view.findViewById(R.id.textView);
            ImageView checkView = (ImageView) view.findViewById(R.id.check);
            checkView.setVisibility(facility.id == facilityIndex ? View.VISIBLE : View.GONE);
            textView.setText(facility.name);
            return view;
        });
		listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Facility facility = adapter.getItem(i);
            Intent intent = new Intent();
            intent.putExtra("id", facility.id);
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

package jp.campus_ar.campusar.layer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.model.io.ArrayCallbackAdapter;
import jp.campus_ar.campusar.model.Entry;

public class SearchResultCategoryLayer extends Fragment {

	public static interface OnEntryItemSelectedListener {
		public void onEntryItemSelected(Entry entry);
	}

	private ListView listView;
	private ArrayCallbackAdapter<Entry> adapter;
	private OnEntryItemSelectedListener listener;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.layer_search_result_category, container, false);

		listView = (ListView) v.findViewById(R.id.listView);
		listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Entry selectedEntry = adapter.getItem(i);
            listener.onEntryItemSelected(selectedEntry);
        });
		applyAdapter();

		return v;
	}

	public void setOnEntryItemSelectedListener(OnEntryItemSelectedListener listener) {
		this.listener = listener;
	}

	public void registEntries(Context context, Entry[] entries) {
		adapter = new ArrayCallbackAdapter<Entry>(context, R.layout.listitem_entry);
		for (int i = 0, ii = entries.length; i < ii; i++) {
			adapter.add(entries[i]);
		}

		adapter.setOnViewCreateListener((view, row) -> {
            Entry e = (Entry) row;
            TextView nameView = (TextView) view.findViewById(R.id.nameView);
            TextView detailView = (TextView) view.findViewById(R.id.detailView);
            nameView.setText(e.name);
            detailView.setText(e.detail);
            return view;
        });
		applyAdapter();
	}

	private void applyAdapter() {
		if (listView != null && adapter != null) {
			listView.setAdapter(adapter);
		}
	}

}

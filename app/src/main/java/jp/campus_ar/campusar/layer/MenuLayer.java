package jp.campus_ar.campusar.layer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.model.io.ArrayCallbackAdapter;

public class MenuLayer extends Fragment {

	final public static int FAVORITE = 1;
	final public static int HISTORY = 2;
	final public static int FACILITY = 3;
	final public static int HELP = 4;

	public interface OnMenuItemClickListener {
		public void onMenuItemClick(String name, int value);
	}

	private class Item {
		String name;
		int value;

		private Item(String name, int value) {
			this.name = name;
			this.value = value;
		}
	}

	private ArrayCallbackAdapter<Item> adapter;
	private ListView listView;
	private OnMenuItemClickListener listener;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.layer_menu, container, false);

		listView = (ListView) v.findViewById(R.id.listView);

		adapter = new ArrayCallbackAdapter<Item>(getActivity(), R.layout.listitem_menu);
		adapter.add(new Item("施設", FACILITY));
		adapter.add(new Item("お気に入り", FAVORITE));
		adapter.add(new Item("履歴", HISTORY));
		adapter.add(new Item("ヘルプ", HELP));
		adapter.setOnViewCreateListener((view, row) -> {
            Item item = (Item) row;
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(item.name);
            return view;
        });
		listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Item item = adapter.getItem(i);
            if (listener != null) listener.onMenuItemClick(item.name, item.value);
        });
		listView.setAdapter(adapter);

		return v;
	}

	public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
		this.listener = listener;
	}

}

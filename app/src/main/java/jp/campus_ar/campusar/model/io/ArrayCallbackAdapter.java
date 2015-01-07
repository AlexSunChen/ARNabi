package jp.campus_ar.campusar.model.io;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class ArrayCallbackAdapter<T> extends BaseAdapter {

	public interface OnViewCreateListener {
		abstract View onViewCreate(View view, Object row);
	}

	private int surfaceId;
	private List<T> data = new ArrayList<T>();
	private LayoutInflater inflater;
	private OnViewCreateListener onViewCreateListener = (view, row) -> view;

	public ArrayCallbackAdapter(Context context, int itemId) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.surfaceId = itemId;
	}

	public void add(T data) {
		this.data.add(data);
	}

	public void set(List<T> data) {
		this.data = data;
	}

	public void setOnViewCreateListener(OnViewCreateListener listener) {
		if (listener != null) this.onViewCreateListener = listener;
	}

	public int getCount() {
		return this.data.size();
	}

	public T getItem(int index) {
		return this.data.get(index);
	}

	public long getItemId(int position) {
		return position;
	}

	private View createViewFromResource(View view, ViewGroup parent, int layout) {
		if (view == null) {
			return this.inflater.inflate(layout, parent, false);
		}
		return view;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		T row = this.getItem(position);
		View view = createViewFromResource(convertView, parent, surfaceId);
		view = this.onViewCreateListener.onViewCreate(view, row);
		return view;
	}

}
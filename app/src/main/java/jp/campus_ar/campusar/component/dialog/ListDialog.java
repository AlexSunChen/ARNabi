package jp.campus_ar.campusar.component.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import jp.campus_ar.campusar.R;
import jp.campus_ar.campusar.model.io.ArrayCallbackAdapter;

public class ListDialog extends Dialog {

	private class Item {
		public String name;
		public int value;

		private Item(String name, int value) {
			this.name = name;
			this.value = value;
		}
	}

	public interface OnListDialogItemSelectedListener {
        public void onListDialogItemSelected(ListDialog dialog, String name, int value);
        public void onEditTitleEntered(EditText editText);
	}

	private ArrayCallbackAdapter<Item> adapter;
	private OnListDialogItemSelectedListener listener;
	private TextView titleView;
    private EditText editTitleView;
	private ListView listView;
    private String placeName;
	private int value;

	public ListDialog(final Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_list);

		titleView = (TextView) findViewById(R.id.titleView);
        editTitleView = (EditText) findViewById(R.id.editTitleView);
		listView = (ListView) findViewById(R.id.listView);

		titleView.setText("Choose");

		adapter = new ArrayCallbackAdapter<>(context, R.layout.listitem_checkable);
		adapter.setOnViewCreateListener((view, row) -> {
            Item item = (Item) row;
            TextView textView = (TextView) view.findViewById(R.id.textView);
            ImageView check = (ImageView) view.findViewById(R.id.check);
            check.setVisibility(value == item.value ? View.VISIBLE : View.GONE);
            textView.setText(item.name);
            return view;
        });
		listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Item item = adapter.getItem(i);
            if (listener != null) listener.onListDialogItemSelected(ListDialog.this, item.name, item.value);
        });
		listView.setAdapter(adapter);

        editTitleView.setImeOptions(EditorInfo.IME_ACTION_GO);
        editTitleView.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        editTitleView.setOnEditorActionListener((tv, id, event) -> {
            if ((event == null && id == 6) || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                tv.clearFocus();
                listener.onEditTitleEntered(editTitleView);
                return true;
            }
            listener.onEditTitleEntered(editTitleView);
            return false;
        });

        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                placeName = s.toString();
            }
        };

        editTitleView.addTextChangedListener(mTextWatcher);

        editTitleView.setText("");
        editTitleView.setVisibility(View.GONE);
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setOnListDialogItemSelectedListener(OnListDialogItemSelectedListener listener) {
		this.listener = listener;
	}

	public void setTitle(String title) {
		titleView.setText(title);
	}

	public void add(String name, int value) {
		adapter.add(new Item(name, value));
	}

	public void show() {
		super.show();
		adapter.notifyDataSetChanged();
	}

    public void toggleMainTitleView() {
        if (titleView.getVisibility() == View.VISIBLE) {
            titleView.setVisibility(View.GONE);
            editTitleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.VISIBLE);
            editTitleView.setVisibility(View.GONE);
        }
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setEditTitleView(String string) {
        editTitleView.setHint(string);
    }
}

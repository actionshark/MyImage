package kk.myimage.adapter;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.graphics.BitmapFactory.Options;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.util.ImageUtil;

public class DetailGridAdapter extends BaseAdapter {
	static class DataItem {
		public int name;
		public String value;

		public DataItem(int name, Object value) {
			this.name = name;
			this.value = String.valueOf(value);
		}
	}

	static class ViewHolder {
		public TextView name;
		public TextView value;
	}

	private final List<DataItem> mData = new ArrayList<DataItem>();

	public DetailGridAdapter(String path) {
		File file = new File(path);
		Options options = ImageUtil.getOptions(path);

		mData.add(new DataItem(R.string.detail_name, file.getName()));
		mData.add(new DataItem(R.string.detail_path, file.getParent()));

		String num = String.valueOf(file.length());
		StringBuilder sb = new StringBuilder();
		int len = num.length();
		for (int i = 0; i < len; i++) {
			sb.append(num.charAt(i));

			if (i + 1 != len && (len - i) % 3 == 1) {
				sb.append(',');
			}
		}
		mData.add(new DataItem(R.string.detail_size, sb + " B"));

		mData.add(new DataItem(R.string.detail_width, options.outWidth));
		mData.add(new DataItem(R.string.detail_height, options.outHeight));

		Date date = new Date(file.lastModified());
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss",
				Locale.ENGLISH);
		mData.add(new DataItem(R.string.detail_modify, df.format(date)));
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int index) {
		return null;
	}

	@Override
	public long getItemId(int index) {
		return 0;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		final ViewHolder vh;

		if (view == null) {
			view = BaseActivity.ca().getLayoutInflater()
					.inflate(R.layout.grid_grid_detail, null);

			vh = new ViewHolder();
			vh.name = (TextView) view.findViewById(R.id.tv_name);
			vh.value = (TextView) view.findViewById(R.id.tv_value);

			view.setTag(vh);
		} else {
			vh = (ViewHolder) view.getTag();
		}

		DataItem data = mData.get(index);

		vh.name.setText(data.name);
		vh.value.setText(data.value);

		return view;
	}
}

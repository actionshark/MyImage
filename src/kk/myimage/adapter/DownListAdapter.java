package kk.myimage.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.ui.DownList;
import kk.myimage.ui.IDialogClickListener;

public class DownListAdapter extends BaseAdapter {
	public static class DataItem {
		public int icon;
		public int text;
		public IDialogClickListener listener;

		public DataItem() {
		}

		public DataItem(int icon, int text, IDialogClickListener listener) {
			this.icon = icon;
			this.text = text;
			this.listener = listener;
		}
	}

	private final DownList mDownList;
	private List<DataItem> mData;

	public DownListAdapter(DownList downList) {
		mDownList = downList;
	}

	public void setDataList(List<DataItem> data) {
		mData = data;
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
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
					.inflate(R.layout.grid_list_down, null);

			vh = new ViewHolder();
			vh.icon = (ImageView) view.findViewById(R.id.iv_icon);
			vh.text = (TextView) view.findViewById(R.id.tv_text);

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					DataItem data = mData.get(vh.index);
					data.listener.onClick(mDownList, vh.index);
					mDownList.dismiss();
				}
			});

			view.setTag(vh);
		} else {
			vh = (ViewHolder) view.getTag();
		}

		DataItem data = mData.get(index);
		vh.icon.setImageResource(data.icon);
		vh.text.setText(data.text);

		vh.index = index;

		return view;
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView text;

		public int index;
	}
}

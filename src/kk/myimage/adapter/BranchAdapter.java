package kk.myimage.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.activity.LeafActivity;
import kk.myimage.tree.BranchData;
import kk.myimage.ui.UiMode;

public class BranchAdapter extends BaseAdapter implements UiMode.ICallee {
	private UiMode.Mode mMode = UiMode.Mode.Normal;
	private final UiMode.ICaller mModeCaller;

	private List<BranchData> mList;
	private final SparseBooleanArray mSelect = new SparseBooleanArray();

	private final Set<Integer> mVisible = new HashSet<Integer>();
	private boolean mNeedUpdate = false;

	public BranchAdapter(UiMode.ICaller caller) {
		mModeCaller = caller;
	}

	public void setDataList(List<BranchData> list) {
		if (mNeedUpdate) {

		} else if (mList == null) {
			mNeedUpdate = true;
		} else {
			int last = -1;

			for (Integer index : mVisible) {
				if (mList.get(index).equals(list.get(index)) == false) {
					mNeedUpdate = true;
					break;
				}

				if (index > last) {
					last = index;
				}
			}

			if (mNeedUpdate == false) {
				if (mList.size() == last + 1 && list.size() > last + 1
						|| last + 1 > list.size()) {

					mNeedUpdate = true;
				}
			}
		}

		mList = list;
	}

	@Override
	public void onModeChange(UiMode.Mode mode) {
		mMode = mode;

		if (mMode == UiMode.Mode.Normal) {
			mSelect.clear();
		}
	}

	@Override
	public void onModeUpdate(boolean force) {
		if (force || mNeedUpdate) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void notifyDataSetChanged() {
		mVisible.clear();
		super.notifyDataSetChanged();
		mNeedUpdate = false;
	}

	public boolean isVisible(String path) {
		for (Integer index : mVisible) {
			BranchData bd = mList.get(index);

			if (bd != null && bd.getThumPath().equals(path)) {
				return true;
			}
		}

		return false;
	}

	public int getSelectCount() {
		int count = 0;
		int len = getCount();

		for (int i = 0; i < len; i++) {
			if (mSelect.get(i)) {
				count++;
			}
		}

		return count;
	}

	public List<BranchData> getSelectList() {
		List<BranchData> list = new ArrayList<BranchData>();

		int len = getCount();
		for (int i = 0; i < len; i++) {
			if (mSelect.get(i)) {
				list.add(mList.get(i));
			}
		}

		return list;
	}

	public void selectAll(boolean enable) {
		if (enable) {
			int len = getCount();

			for (int i = 0; i < len; i++) {
				mSelect.put(i, true);
			}
		} else {
			mSelect.clear();
		}
	}

	public void reverse() {
		int len = getCount();

		for (int i = 0; i < len; i++) {
			if (mSelect.get(i)) {
				mSelect.delete(i);
			} else {
				mSelect.put(i, true);
			}
		}
	}

	@Override
	public int getCount() {
		return mList == null ? 0 : mList.size();
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
					.inflate(R.layout.grid_branch, null);

			vh = new ViewHolder();
			vh.thum = (ImageView) view.findViewById(R.id.iv_thum);
			vh.name = (TextView) view.findViewById(R.id.tv_name);
			vh.num = (TextView) view.findViewById(R.id.tv_num);
			vh.sort = (ImageView) view.findViewById(R.id.iv_sort);
			vh.mask = (Button) view.findViewById(R.id.btn_mask);
			vh.select = (ImageView) view.findViewById(R.id.iv_select);

			vh.mask.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mMode == UiMode.Mode.Normal) {
						Intent intent = new Intent(BaseActivity.ca(),
								LeafActivity.class);
						intent.putExtra(BaseActivity.KEY_PATH,
								vh.data.getPath());
						BaseActivity.ca().startActivity(intent);
					} else if (mMode == UiMode.Mode.Select) {
						int key = vh.index;
						Boolean value = mSelect.get(key);

						if (value == Boolean.TRUE) {
							value = false;
						} else {
							value = true;
						}

						mSelect.put(key, value);

						mModeCaller.updateMode(true);
					}
				}
			});

			vh.mask.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if (mMode == UiMode.Mode.Normal) {
						mSelect.put(vh.index, true);
						mModeCaller.changeMode(UiMode.Mode.Select);
					} else if (mMode == UiMode.Mode.Select) {
						int key = vh.index;
						Boolean value = mSelect.get(key);

						if (value == Boolean.TRUE) {
							value = false;
						} else {
							value = true;
						}

						mSelect.put(key, value);

						mModeCaller.updateMode(true);
					}

					return true;
				}
			});

			view.setTag(vh);
		} else {
			vh = (ViewHolder) view.getTag();
		}

		BranchData data = mList.get(index);

		vh.name.setText(data.getName());
		vh.num.setText(data.getChildNum() + "");

		int sortIndex = data.getSortIndex();
		if (sortIndex < 0) {
			vh.sort.setImageResource(R.drawable.arrow_up);
		} else if (sortIndex > 0) {
			vh.sort.setImageResource(R.drawable.arrow_down);
		} else {
			vh.sort.setImageResource(0);
		}

		Bitmap thum = data.getThum();
		if (thum != null) {
			vh.thum.setImageBitmap(thum);
		} else if (vh.index != index) {
			vh.thum.setImageBitmap(null);
		}

		if (mMode == UiMode.Mode.Normal) {
			vh.select.setVisibility(View.GONE);
		} else {
			vh.select
					.setImageResource(mSelect.get(index) ? R.drawable.select_enable
							: R.drawable.select_disable);
			vh.select.setVisibility(View.VISIBLE);
		}

		vh.index = index;
		vh.data = data;

		mVisible.add(index);

		return view;
	}

	static class ViewHolder {
		public ImageView thum;
		public ImageView select;
		public TextView name;
		public TextView num;
		public ImageView sort;
		public Button mask;

		public int index;
		public BranchData data;
	}
}

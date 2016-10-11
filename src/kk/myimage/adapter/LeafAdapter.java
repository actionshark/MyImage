package kk.myimage.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.activity.DetailActivity;
import kk.myimage.tree.BranchData;
import kk.myimage.tree.LeafData;
import kk.myimage.ui.UiMode;
import kk.myimage.util.ImageUtil;
import kk.myimage.util.ImageUtil.IImageListenner;

public class LeafAdapter extends BaseAdapter implements UiMode.ICallee {
	private UiMode.Mode mMode = UiMode.Mode.Normal;
	private final UiMode.ICaller mModeCaller;

	private BranchData mData;
	private final SparseBooleanArray mSelect = new SparseBooleanArray();

	private final Set<Integer> mVisible = new HashSet<Integer>();
	private boolean mNeedUpdate = false;

	public LeafAdapter(UiMode.ICaller caller) {
		mModeCaller = caller;
	}

	public void setDataList(BranchData data) {
		if (mNeedUpdate) {

		} else if (mData == null) {
			mNeedUpdate = true;
		} else {
			List<LeafData> la = mData.getChildren(true);
			List<LeafData> lb = data.getChildren(true);
			int last = -1;

			for (Integer index : mVisible) {
				if (la.get(index).equals(lb.get(index)) == false) {
					mNeedUpdate = true;
					break;
				}

				if (index > last) {
					last = index;
				}
			}

			if (mNeedUpdate == false) {
				if (la.size() == last + 1 && lb.size() > last + 1
						|| last + 1 > lb.size()) {

					mNeedUpdate = true;
				}
			}
		}

		mData = data;
	}

	public BranchData getDataList() {
		return mData;
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
		List<LeafData> list = mData.getChildren(true);

		for (Integer index : mVisible) {
			LeafData ld = list.get(index);

			if (ld != null && ld.getPath().equals(path)) {
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

	public List<LeafData> getSelectList() {
		List<LeafData> list = mData.getChildren(true);
		List<LeafData> select = new ArrayList<LeafData>();

		int len = list.size();

		for (int i = 0; i < len; i++) {
			if (mSelect.get(i)) {
				select.add(list.get(i));
			}
		}

		return select;
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
		return mData == null ? 0 : mData.getChildNum();
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
					.inflate(R.layout.grid_leaf, null);

			vh = new ViewHolder();
			vh.thum = (ImageView) view.findViewById(R.id.iv_thum);
			vh.tag = view.findViewById(R.id.ll_tag);
			vh.name = (TextView) vh.tag.findViewById(R.id.tv_name);
			vh.size = (TextView) vh.tag.findViewById(R.id.tv_size);
			vh.mask = (Button) view.findViewById(R.id.btn_mask);
			vh.select = (ImageView) view.findViewById(R.id.iv_select);

			vh.mask.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mMode == UiMode.Mode.Normal) {
						Intent intent = new Intent(BaseActivity.ca(),
								DetailActivity.class);
						intent.putExtra(BaseActivity.KEY_PATH, mData.getPath());
						intent.putExtra(BaseActivity.KEY_INDEX, vh.index);
						BaseActivity.ca().startActivity(intent);
					} else {
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
						int key = vh.index;
						mSelect.put(key, true);
						mModeCaller.changeMode(UiMode.Mode.Select);
					} else {
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

		final LeafData data = mData.getChildren(true).get(index);

		if (data.equals(vh.data) == false || vh.hasImg == false) {
			vh.hasImg = false;
			vh.thum.setImageDrawable(null);
			
			ImageUtil.getThum(data.getPath(), vh.thum.getWidth(), vh.thum.getHeight(), new IImageListenner() {
				@Override
				public void onImageGot(Bitmap bmp) {
					if (data.equals(vh.data)) {
						vh.hasImg = true;
						vh.thum.setImageBitmap(bmp);
					}
				}
			});
		}

		if (mMode == UiMode.Mode.Normal) {
			vh.tag.setVisibility(View.GONE);
			vh.select.setVisibility(View.GONE);
		} else {
			vh.name.setText(data.getName());

			Options options = ImageUtil.getSize(data.getPath());
			vh.size.setText(String.format("%d x %d", options.outWidth,
					options.outHeight));

			vh.tag.setVisibility(View.VISIBLE);

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
		public View tag;
		public TextView name;
		public TextView size;
		public Button mask;
		public ImageView select;

		public int index;
		public LeafData data;
		public boolean hasImg;
	}
}

package kk.myimage.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import kk.myimage.R;

import kk.myimage.activity.BaseActivity;
import kk.myimage.tree.LeafData;
import kk.myimage.ui.DetailView;
import kk.myimage.ui.UiMode;

public class DetailAdapter extends PagerAdapter implements UiMode.ICallee {
	static class ViewHolder {
		public DetailView image;
		public ListView list;

		public LeafData data;
		public int position;
	}

	private List<LeafData> mDataList;
	private final List<View> mViewList = new ArrayList<View>();

	private UiMode.Mode mMode = UiMode.Mode.Normal;

	private OnClickListener mClickListener;
	private OnLongClickListener mLongClickListener;
	
	private final SparseArray<Matrix> mChanges = new SparseArray<Matrix>();

	public DetailAdapter() {
		for (int i = 0; i < 5; i++) {
			View root = BaseActivity.ca().getLayoutInflater()
					.inflate(R.layout.grid_detail, null);
			ViewHolder holder = new ViewHolder();

			holder.image = (DetailView) root.findViewById(R.id.dv_image);
			holder.image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mClickListener != null) {
						mClickListener.onClick(view);
					}
				}
			});
			holder.image.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if (mLongClickListener != null) {
						return mLongClickListener.onLongClick(view);
					}

					return false;
				}
			});

			holder.list = (ListView) root.findViewById(R.id.lv_list);

			root.setTag(holder);
			mViewList.add(root);
		}
	}

	public void setDataList(List<LeafData> list) {
		mDataList = list;
	}

	public List<LeafData> getDataList() {
		return mDataList;
	}

	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}

	public void setOnLongClickListener(OnLongClickListener listener) {
		mLongClickListener = listener;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	protected int getViewPosition(int position) {
		return position % mViewList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mViewList.get(getViewPosition(position)));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View root = mViewList.get(getViewPosition(position));
		ViewHolder vh = (ViewHolder) root.getTag();
		
		LeafData data = mDataList.get(position);
		if (vh.data != data) {
			vh.data = data;
			updateView(root);
		}
		vh.position = position;

		if (root.getParent() == null) {
			container.addView(root, 0);
		}

		return root;
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == obj;
	}

	private void updateView(View root) {
		ViewHolder vh = (ViewHolder) root.getTag();
		LeafData data = vh.data;

		if (mMode == UiMode.Mode.Detail) {
			vh.image.setBitmap(data.getThum());

			DetailGridAdapter adapter = (DetailGridAdapter) vh.list
					.getAdapter();
			if (adapter == null) {
				adapter = new DetailGridAdapter(data.getPath());
				vh.list.setAdapter(adapter);
			}
			adapter.notifyDataSetChanged();
			vh.list.setVisibility(View.VISIBLE);
		} else {
			vh.image.setBitmap(data.getImage());
			
			Matrix matrix = mChanges.get(vh.position);
			if (matrix != null) {
				mChanges.remove(vh.position);
				vh.image.setChange(matrix);
			}

			vh.list.setVisibility(View.GONE);
		}
	}

	@Override
	public void onModeChange(UiMode.Mode mode) {
		mMode = mode;
	}

	@Override
	public void onModeUpdate(boolean force) {
		notifyDataSetChanged();

		for (View root : mViewList) {
			if (root.getParent() != null) {
				updateView(root);
			}
		}
	}
	
	public Matrix getChange(int position) {
		View root = mViewList.get(getViewPosition(position));
		ViewHolder vh = (ViewHolder) root.getTag();
		return vh.image.getChange();
	}
	
	public void setChange(int position, Matrix matrix) {
		mChanges.put(position, matrix);
	}
}

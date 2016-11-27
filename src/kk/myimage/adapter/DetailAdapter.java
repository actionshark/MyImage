package kk.myimage.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.activity.DetailActivity;
import kk.myimage.tree.LeafData;
import kk.myimage.ui.DetailView;
import kk.myimage.ui.UiMode;
import kk.myimage.util.ImageUtil;
import kk.myimage.util.ImageUtil.IImageListenner;

public class DetailAdapter extends PagerAdapter implements UiMode.ICallee {
	static class ViewHolder {
		public DetailView image;
		public ListView list;

		public LeafData data;
		public int position;
	}
	
	private final DetailActivity mActivity;
	private UiMode.Mode mMode = UiMode.Mode.Normal;
	private List<LeafData> mDataList;
	
	private final List<View> mViewList = new ArrayList<View>();
	private final SparseArray<Matrix> mChanges = new SparseArray<Matrix>();

	public DetailAdapter(DetailActivity activity) {
		mActivity = activity;
		
		for (int i = 0; i < 5; i++) {
			View root = BaseActivity.ca().getLayoutInflater()
					.inflate(R.layout.grid_detail, null);
			ViewHolder holder = new ViewHolder();

			holder.image = (DetailView) root.findViewById(R.id.dv_image);
			holder.image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mMode == UiMode.Mode.Normal) {
						mActivity.changeMode(UiMode.Mode.Detail);
					} else {
						mActivity.changeMode(UiMode.Mode.Normal);
					}
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
		final ViewHolder vh = (ViewHolder) root.getTag();
		final LeafData data = vh.data;

		if (mMode == UiMode.Mode.Detail) {
			vh.image.setVisibility(View.INVISIBLE);
			ImageUtil.getImage(data.getPath(), new IImageListenner() {
				@Override
				public void onImageGot(Bitmap bmp) {
					if (data.equals(vh.data)) {
						vh.image.setBitmap(bmp);
						vh.image.setVisibility(View.VISIBLE);
					}
				}
			});

			DetailGridAdapter adapter = new DetailGridAdapter(data.getPath());
			vh.list.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			vh.list.setVisibility(View.VISIBLE);
		} else {
			vh.image.setVisibility(View.INVISIBLE);
			ImageUtil.getImage(data.getPath(), new IImageListenner() {
				@Override
				public void onImageGot(Bitmap bmp) {
					if (data.equals(vh.data)) {
						vh.image.setBitmap(bmp);
						vh.image.setVisibility(View.VISIBLE);
					}
				}
			});
			
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
		for (View root : mViewList) {
			if (root.getParent() != null) {
				updateView(root);
			}
		}
		
		notifyDataSetChanged();
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

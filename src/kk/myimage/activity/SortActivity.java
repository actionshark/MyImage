package kk.myimage.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import kk.myimage.R;
import kk.myimage.tree.Ant;
import kk.myimage.tree.Squi;
import kk.myimage.tree.Squi.SortFactor;
import kk.myimage.util.AppUtil;

@SuppressWarnings("deprecation")
public class SortActivity extends BaseActivity {
	public static final int MODE_BRANCH = 1;
	public static final int MODE_LEAF = 2;

	private int mMode;
	private List<SortFactor> mFactor;

	private TextView mTvTitle;

	private AbsoluteLayout mAlLayout;
	private View[] mViewGrids;
	private int mGridHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMode = getIntent().getIntExtra(KEY_MODE, MODE_BRANCH);
		if (mMode == MODE_BRANCH) {
			mFactor = Squi.getBranchFactors(false);
		} else if (mMode == MODE_LEAF) {
			mFactor = Squi.getLeafFactors();
		} else {
			finish();
			return;
		}

		setContentView(R.layout.activity_sort);

		View title = findViewById(R.id.rl_title);

		ImageView back = (ImageView) title.findViewById(R.id.iv_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		mTvTitle = (TextView) title.findViewById(R.id.tv_title);
		if (mMode == MODE_BRANCH) {
			mTvTitle.setText(R.string.sort_title_branch);
		} else if (mMode == MODE_LEAF) {
			mTvTitle.setText(R.string.sort_title_leaf);
		}

		mAlLayout = (AbsoluteLayout) findViewById(R.id.al_layout);
		mViewGrids = new View[mFactor.size()];
		mGridHeight = AppUtil.getDimenInt(R.dimen.sort_grid_height);

		for (int i = 0; i < mViewGrids.length; i++) {
			final View grid = getLayoutInflater().inflate(R.layout.grid_sort,
					null);
			LayoutParams lp = new LayoutParams(AppUtil.getScreenWidth(true),
					mGridHeight, 0, mGridHeight * i);
			mAlLayout.addView(grid, lp);
			mViewGrids[i] = grid;

			final ViewHolder vh = new ViewHolder();
			grid.setTag(vh);
			grid.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					int action = event.getAction();

					if (action == MotionEvent.ACTION_DOWN) {
						vh.dy = event.getY();

						mAlLayout.removeView(grid);
						mAlLayout.addView(grid);
					} else if (action == MotionEvent.ACTION_MOVE
							|| action == MotionEvent.ACTION_UP) {

						grid.setY(grid.getY() + event.getY() - vh.dy);

						int ai = vh.index;
						int bi = Math.round(grid.getY() / mGridHeight);
						if (bi < 0) {
							bi = 0;
						} else if (bi >= mViewGrids.length) {
							bi = mViewGrids.length - 1;
						}

						while (ai != bi) {
							int next = bi > ai ? ai + 1 : ai - 1;

							mViewGrids[ai] = mViewGrids[next];
							((ViewHolder) mViewGrids[ai].getTag()).index = ai;
							mViewGrids[ai].setY(mGridHeight * ai);

							ai = next;
						}

						mViewGrids[bi] = grid;
						vh.index = bi;
						if (action == MotionEvent.ACTION_UP) {
							grid.setY(mGridHeight * bi);
						}
					}

					return true;
				}
			});

			vh.index = i;
			vh.sf = mFactor.get(i).clone();

			vh.text = (TextView) grid.findViewById(R.id.tv_text);
			vh.text.setText(vh.sf.text);

			vh.direct = (ImageView) grid.findViewById(R.id.iv_direct);
			vh.direct.setImageResource(vh.sf.up ? R.drawable.arrow_up
					: R.drawable.arrow_down);
			vh.direct.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					vh.sf.up = !vh.sf.up;

					vh.direct.setImageResource(vh.sf.up ? R.drawable.arrow_up
							: R.drawable.arrow_down);
				}
			});
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void finish() {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				boolean diff = false;
				for (int i = 0; i < mViewGrids.length; i++) {
					View grid = mViewGrids[i];
					ViewHolder vh = (ViewHolder) grid.getTag();

					if (vh.sf.equals(mFactor.get(i)) == false) {
						diff = true;
						break;
					}
				}

				if (!diff) {
					return;
				}

				List<SortFactor> list = new ArrayList<SortFactor>();
				for (View grid : mViewGrids) {
					ViewHolder vh = (ViewHolder) grid.getTag();

					list.add(vh.sf);
				}

				if (mMode == MODE_BRANCH) {
					Squi.setBranchFactors(list);
					Ant.sortBranch();
				} else if (mMode == MODE_LEAF) {
					Squi.setLeafFactors(list);
					Ant.sortLeaf();
				}
			}
		});

		super.finish();
	}

	static class ViewHolder {
		public TextView text;
		public ImageView direct;

		public int index;
		public SortFactor sf;
		public float dy;
	}
}

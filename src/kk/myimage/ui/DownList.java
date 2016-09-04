package kk.myimage.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;

import kk.myimage.R;
import kk.myimage.adapter.DownListAdapter;
import kk.myimage.util.AppUtil;

public class DownList extends Dialog {
	private ListView mLvList;
	private DownListAdapter mAdapter;

	private boolean mShowStatusBar = true;
	private boolean mShowTitleBar = true;

	public DownList(Context context) {
		this(context, true, true);
	}

	public DownList(Context context, boolean showStatusBar, boolean showTitleBar) {
		super(context, R.style.down_list);
		init(showStatusBar, showTitleBar);
	}

	protected void init(boolean showStatusBar, boolean showTitleBar) {
		setContentView(R.layout.list_down);

		mLvList = (ListView) findViewById(R.id.lv_list);
		mAdapter = new DownListAdapter(this);
		mLvList.setAdapter(mAdapter);

		mShowStatusBar = showStatusBar;
		mShowTitleBar = showTitleBar;
	}

	public DownListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void show() {
		Window window = getWindow();
		LayoutParams lp = window.getAttributes();

		int titleHeight = (mShowTitleBar ? AppUtil
				.getDimenInt(R.dimen.title_height) : 0);
		int gridHeight = AppUtil.getDimenInt(R.dimen.downlist_grid_height);
		int divider = AppUtil.getDimenInt(R.dimen.downlist_grid_divider);

		lp.width = AppUtil.getDimenInt(R.dimen.downlist_grid_width);
		lp.x = (AppUtil.getScreenWidth(!mShowStatusBar && mShowTitleBar) - lp.width) / 2;

		lp.height = (gridHeight + divider) * (mAdapter.getCount() + 1)
				- gridHeight / 2 - divider * 2;
		if (titleHeight + lp.height > AppUtil.getScreenHeight(false)) {
			lp.height = AppUtil.getScreenHeight(false) - titleHeight;
		}
		lp.y = -(AppUtil.getScreenHeight(!mShowStatusBar && mShowTitleBar) - lp.height)
				/ 2 + titleHeight;

		window.setAttributes(lp);

		super.show();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			dismiss();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}
}

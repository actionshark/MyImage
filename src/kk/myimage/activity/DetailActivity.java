package kk.myimage.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;

import kk.myimage.R;
import kk.myimage.adapter.DetailAdapter;
import kk.myimage.adapter.DownListAdapter;
import kk.myimage.adapter.DownListAdapter.DataItem;
import kk.myimage.tree.Ant;
import kk.myimage.tree.BranchData;
import kk.myimage.tree.LeafData;
import kk.myimage.tree.Spider;
import kk.myimage.tree.Spider.SpiderNode;
import kk.myimage.tree.Worm;
import kk.myimage.tree.Worm.IFinishcallback;
import kk.myimage.ui.DownList;
import kk.myimage.ui.IDialogClickListener;
import kk.myimage.ui.SimpleDialog;
import kk.myimage.ui.UiMode;
import kk.myimage.util.AppUtil;
import kk.myimage.util.IntentUtil;
import kk.myimage.util.Logger;
import kk.myimage.util.Setting;

public class DetailActivity extends BaseActivity implements UiMode.ICaller {
	public static final String KEY_CHANGE = "key_change";
	
	private UiMode.Mode mMode = UiMode.Mode.Normal;
	private DetailAdapter mAdapter;

	private String mDir;
	private String mPath;
	private int mIndex;

	private ViewGroup mVgTitle;
	private TextView mTvTitle;
	private ViewPager mVpImage;
	private Matrix mMatrix;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Setting.getShowStatus() == false) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			if (mStatusBar != null) {
				mStatusBar.setVisibility(View.GONE);
			}
		}

		setContentView(R.layout.activity_detail);

		mVgTitle = (ViewGroup) findViewById(R.id.rl_title);
		mVgTitle.setVisibility(Setting.getShowTitle() ? View.VISIBLE
				: View.GONE);
		mVgTitle.findViewById(R.id.iv_back).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						finish();
					}
				});

		mTvTitle = (TextView) mVgTitle.findViewById(R.id.tv_title);
		mVgTitle.findViewById(R.id.iv_menu).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						showMenu();
					}
				});

		mAdapter = new DetailAdapter(this);
		mVpImage = (ViewPager) findViewById(R.id.vp_list);
		mVpImage.setAdapter(mAdapter);
		mVpImage.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mIndex = position;

				mTvTitle.setText(String.format("%d/%d", position + 1,
						mAdapter.getCount()));
			}

			@Override
			public void onPageScrolled(int position, float percent, int offset) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		if (checkData() == false) {
			finish();
			return;
		}
		
		int mode = getIntent().getIntExtra(KEY_MODE, UiMode.Mode.Normal.ordinal());
		mMode = UiMode.Mode.values()[mode];
		changeMode(mMode);
	}

	@Override
	public void changeMode(UiMode.Mode mode) {
		mMode = mode;

		mAdapter.onModeChange(mode);

		updateMode(true);
	}

	@Override
	public void updateMode(boolean force) {
		mAdapter.onModeUpdate(force);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenu();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	private void showMenu() {
		DownList dl = new DownList(DetailActivity.this,
				Setting.getShowStatus(), Setting.getShowTitle());
		DownListAdapter adapter = dl.getAdapter();
		List<DataItem> dataList = new ArrayList<DataItem>();

		dataList.add(new DataItem(R.drawable.share, R.string.share,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						List<LeafData> list = mAdapter.getDataList();
						IntentUtil.share(list.get(mIndex));
					}
				}));

		dataList.add(new DataItem(R.drawable.edit, R.string.edit,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						List<LeafData> list = mAdapter.getDataList();
						IntentUtil.edit(list.get(mIndex));
					}
				}));

		dataList.add(new DataItem(R.drawable.delete, R.string.delete,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						List<LeafData> list = mAdapter.getDataList();

						final List<String> leafList = new ArrayList<String>();
						leafList.add(list.get(mIndex).getPath());

						SimpleDialog confirm = new SimpleDialog(
								DetailActivity.this);
						confirm.setContent(AppUtil.getString(
								R.string.msg_delete_confirm, leafList.size()));
						confirm.setButtons(new int[] { R.string.cancel,
								R.string.delete });
						confirm.setClickListener(new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index) {
								if (index == 1) {
									Worm.deleteLeaf(leafList,
											new IFinishcallback() {
												@Override
												public void onFinish() {
													Ant.addUpdate(mDir);
												}
											});

									finish();
								}

								dialog.dismiss();
							}
						});
						confirm.show();
					}
				}));

		SpiderNode sn = Spider.getNode(mDir);
		String name = mAdapter.getDataList().get(mIndex).getName();
		dataList.add(new DataItem(R.drawable.cover, sn != null
				&& name.equals(sn.thum) ? R.string.not_as_cover
				: R.string.set_as_cover, new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				SpiderNode sn = Spider.getNode(mDir);
				if (sn == null) {
					sn = new SpiderNode();
				}

				String name = mAdapter.getDataList().get(mIndex).getName();
				if (name.equals(sn.thum)) {
					sn.thum = null;
				} else {
					sn.thum = name;
				}

				Spider.setNode(mDir, sn);
			}
		}));

		dataList.add(new DataItem(R.drawable.see,
				mMode == UiMode.Mode.Detail ? R.string.hide_detail
						: R.string.show_detail, new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						if (mMode == UiMode.Mode.Normal) {
							changeMode(UiMode.Mode.Detail);
						} else {
							changeMode(UiMode.Mode.Normal);
						}
					}
				}));

		dataList.add(new DataItem(R.drawable.status_bar, Setting
				.getShowStatus() ? R.string.hide_status : R.string.show_status,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						boolean show = !Setting.getShowStatus();
						Setting.setShowStatus(show);

						restart();
					}
				}));

		dataList.add(new DataItem(R.drawable.title_bar,
				Setting.getShowTitle() ? R.string.hide_title
						: R.string.show_title, new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						boolean show = !Setting.getShowTitle();
						Setting.setShowTitle(show);

						restart();
					}
				}));

		dataList.add(new DataItem(R.drawable.lock,
				Setting.getLockTransform() ? R.string.unlock_transform
						: R.string.lock_transform, new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						boolean lock = !Setting.getLockTransform();
						Setting.setLockTransform(lock);
					}
				}));

		adapter.setDataList(dataList);
		dl.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mMatrix != null) {
			mAdapter.setChange(mIndex, mMatrix);
		}
		
		mTvTitle.setText(String.format("%d/%d", mIndex + 1,
				mAdapter.getCount()));
		
		updateMode(true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mMatrix = mAdapter.getChange(mIndex);
	}

	private void restart() {
		Intent intent = new Intent(BaseActivity.ca(), DetailActivity.class);
		intent.putExtra(KEY_PATH, mDir);
		intent.putExtra(KEY_INDEX, mIndex);
		intent.putExtra(KEY_MODE, mMode.ordinal());
		
		Matrix matrix = mAdapter.getChange(mIndex);
		float[] values = new float[9];
		matrix.getValues(values);
		intent.putExtra(KEY_CHANGE, values);
		
		BaseActivity.ca().startActivity(intent);

		finish();
	}
	
	private boolean checkData() {
		Intent intent = getIntent();
		final Uri uri = intent.getData();

		if (uri != null) {
			try {
				File file = new File(uri.getPath());
				mPath = file.getAbsolutePath();
				mDir = file.isDirectory() ? mPath : file.getParent();
				mIndex = 0;
				
				List<LeafData> list = new ArrayList<LeafData>();
				list.add(new LeafData(file));
				mAdapter.setDataList(list);
				mVpImage.setCurrentItem(mIndex);
			} catch (Exception e) {
				Logger.print(null, e);

				return false;
			}
			
			return true;
		} else {
			mDir = intent.getStringExtra(KEY_PATH);
			mIndex = intent.getIntExtra(KEY_INDEX, 0);
			
			float[] values = intent.getFloatArrayExtra(KEY_CHANGE);
			if (values != null) {
				mMatrix = new Matrix();
				mMatrix.setValues(values);
			}

			BranchData bd = Ant.getData(mDir);
			if (bd == null) {
				return false;
			}
			
			mAdapter.setDataList(bd.getChildren(true));
			mVpImage.setCurrentItem(mIndex);
			
			return true;
		}
	}
}

package kk.myimage.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import kk.myimage.R;
import kk.myimage.adapter.BranchAdapter;
import kk.myimage.adapter.DownListAdapter;
import kk.myimage.adapter.DownListAdapter.DataItem;
import kk.myimage.tree.Ant;
import kk.myimage.tree.BranchData;
import kk.myimage.tree.Hole;
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
import kk.myimage.util.Broadcast;
import kk.myimage.util.Setting;

public class BranchActivity extends BaseActivity implements UiMode.ICaller, UiMode.ICallee {

	private UiMode.Mode mMode = UiMode.Mode.Normal;

	private final BranchAdapter mAdapter = new BranchAdapter(this);

	private TextView mTvTitle;
	private ImageView mIvSelect;

	private ImageView mIvMenu;
	private DownList mDlMenu;

	private ViewGroup mVgMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_branch);

		// 标题栏
		View title = findViewById(R.id.rl_title);

		title.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mMode == UiMode.Mode.Select) {
					changeMode(UiMode.Mode.Normal);
				} else {
					finish();
				}
			}
		});

		mTvTitle = (TextView) title.findViewById(R.id.tv_title);

		mIvSelect = (ImageView) title.findViewById(R.id.iv_select);
		mIvSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int select = mAdapter.getSelectCount();
				int total = mAdapter.getCount();

				mAdapter.selectAll(select < total);

				updateMode(true);
			}
		});

		mIvMenu = (ImageView) title.findViewById(R.id.iv_menu);
		mIvMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showMenu();
			}
		});

		// 列表
		GridView gv = (GridView) findViewById(R.id.gv_grid);
		gv.setAdapter(mAdapter);

		// 菜单
		mVgMenu = (ViewGroup) findViewById(R.id.ll_menu);

		mVgMenu.findViewById(R.id.iv_copy).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				List<BranchData> branchList = mAdapter.getSelectList();
				List<String> leafList = new ArrayList<String>();

				for (BranchData dd : branchList) {
					for (LeafData id : dd.getChildren(true)) {
						leafList.add(id.getPath());
					}
				}

				if (leafList.size() < 1) {
					Toast.makeText(BranchActivity.this.getApplicationContext(),
						R.string.msg_nothing_selected,
						Toast.LENGTH_SHORT).show();
					return;
				}

				Hole.copy(leafList);
				changeMode(UiMode.Mode.Normal);

				Toast.makeText(BranchActivity.this.getApplicationContext(),
					R.string.msg_clip, Toast.LENGTH_SHORT).show();
			}
		});

		mVgMenu.findViewById(R.id.iv_cut).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				List<BranchData> branchList = mAdapter.getSelectList();
				List<String> leafList = new ArrayList<String>();

				for (BranchData dd : branchList) {
					for (LeafData id : dd.getChildren(true)) {
						leafList.add(id.getPath());
					}
				}

				if (leafList.size() < 1) {
					Toast.makeText(BranchActivity.this.getApplicationContext(),
						R.string.msg_nothing_selected,
						Toast.LENGTH_SHORT).show();
					return;
				}

				Hole.cut(leafList);
				changeMode(UiMode.Mode.Normal);

				Toast.makeText(BranchActivity.this.getApplicationContext(),
					R.string.msg_clip, Toast.LENGTH_SHORT).show();
			}
		});

		mVgMenu.findViewById(R.id.iv_delete).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final List<BranchData> branchList = mAdapter.getSelectList();
				final List<String> leafList = new ArrayList<String>();

				for (BranchData dd : branchList) {
					for (LeafData id : dd.getChildren(true)) {
						leafList.add(id.getPath());
					}
				}

				if (leafList.size() < 1) {
					Toast.makeText(BranchActivity.this.getApplicationContext(),
						R.string.msg_nothing_selected,
						Toast.LENGTH_SHORT).show();
					return;
				}

				SimpleDialog confirm = new SimpleDialog(BranchActivity.this);
				confirm.setContent(AppUtil.getString(R.string.msg_delete_confirm, leafList.size()));
				confirm.setButtons(new int[] {
					R.string.cancel, R.string.delete
				});
				confirm.setClickListener(new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						if (index == 1) {
							Worm.deleteLeaf(leafList, new IFinishcallback() {
								@Override
								public void onFinish() {
									for (BranchData bd : branchList) {
										Ant.addUpdate(bd.getPath());
									}
								}
							});

							changeMode(UiMode.Mode.Normal);
						}

						dialog.dismiss();
					}
				});
				confirm.show();
			}
		});

		mVgMenu.findViewById(R.id.iv_top).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final List<BranchData> branchList = mAdapter.getSelectList();
				if (branchList.size() < 1) {
					Toast.makeText(BranchActivity.this.getApplicationContext(),
						R.string.msg_nothing_selected,
						Toast.LENGTH_SHORT).show();
					return;
				}

				for (BranchData bd : branchList) {
					String path = bd.getPath();
					SpiderNode sn = Spider.getNode(path);
					if (sn == null) {
						sn = new SpiderNode();
					}

					if (sn.sortIndex > 0) {
						sn.sortIndex = 0;
					} else if (sn.sortIndex == 0) {
						sn.sortIndex = -1;
					}

					Spider.setNode(path, sn);
				}

				Ant.sortBranch();
				changeMode(UiMode.Mode.Normal);
			}
		});

		mVgMenu.findViewById(R.id.iv_bottom).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final List<BranchData> branchList = mAdapter.getSelectList();
				if (branchList.size() < 1) {
					Toast.makeText(BranchActivity.this.getApplicationContext(),
						R.string.msg_nothing_selected,
						Toast.LENGTH_SHORT).show();
					return;
				}

				for (BranchData bd : branchList) {
					String path = bd.getPath();
					SpiderNode sn = Spider.getNode(path);
					if (sn == null) {
						sn = new SpiderNode();
					}

					if (sn.sortIndex < 0) {
						sn.sortIndex = 0;
					} else if (sn.sortIndex == 0) {
						sn.sortIndex = 1;
					}

					Spider.setNode(path, sn);
				}

				Ant.sortBranch();
				changeMode(UiMode.Mode.Normal);
			}
		});

		mVgMenu.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				changeMode(UiMode.Mode.Normal);
			}
		});

		// 开始
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				Map<String, SpiderNode> map = Spider.getAllNode();
				boolean showHide = Setting.getShowHide();

				for (Entry<String, SpiderNode> entry : map.entrySet()) {
					if (showHide || !entry.getValue().hidden) {
						Ant.addUpdate(entry.getKey(), entry.getValue());
					}
				}

				Ant.refresh(true, true);
			}
		});
	}

	@Override
	public void changeMode(UiMode.Mode mode) {
		onModeChange(mode);
		mAdapter.onModeChange(mode);

		updateMode(true);
	}

	@Override
	public void updateMode(boolean force) {
		onModeUpdate(force);
		mAdapter.onModeUpdate(force);
	}

	@Override
	public void onModeChange(UiMode.Mode mode) {
		mMode = mode;
	}

	@Override
	public void onModeUpdate(boolean force) {
		mAdapter.setDataList(Ant.getData(false));
		int total = mAdapter.getCount();

		if (mMode == UiMode.Mode.Select) {
			int select = mAdapter.getSelectCount();

			mTvTitle.setText(String.format("%d/%d", select, total));

			mIvSelect.setImageResource(select < total ? R.drawable.select_enable
				: R.drawable.select_disable);
			mIvSelect.setVisibility(View.VISIBLE);

			mIvMenu.setVisibility(View.GONE);

			mVgMenu.setVisibility(View.VISIBLE);
		} else {
			// mTvTitle.setText(String.format("%d%s", total, Ant.isRunning() ?
			// "..." : ""));
			mTvTitle.setText(String.format("%d", total));

			mIvSelect.setVisibility(View.GONE);

			mIvMenu.setVisibility(View.VISIBLE);

			mVgMenu.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMode == UiMode.Mode.Select) {
				changeMode(UiMode.Mode.Normal);
			} else {
				finish();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenu();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	private void showMenu() {
		mDlMenu = new DownList(BranchActivity.this);
		DownListAdapter adapter = mDlMenu.getAdapter();
		List<DataItem> dataList = new ArrayList<DataItem>();

		dataList.add(new DataItem(R.drawable.arrow_up_down, R.string.sort,
			new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Intent intent = new Intent(BranchActivity.this, SortActivity.class);
					intent.putExtra(KEY_MODE, SortActivity.MODE_BRANCH);
					startActivity(intent);
				}
			}));

		dataList.add(new DataItem(Setting.getShowHide() ? R.drawable.hide : R.drawable.show,
			Setting.getShowHide() ? R.string.hide_hide : R.string.show_hide,
			new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					boolean show = Setting.getShowHide();
					Setting.setShowHide(!show);
					Ant.refresh(false, true);
				}
			}));

		dataList.add(new DataItem(R.drawable.refresh, R.string.refresh, new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				Ant.refresh(false, true);
			}
		}));

		adapter.setDataList(dataList);
		mDlMenu.show();
	}

	private Broadcast.IListener mBroadcast = new Broadcast.IListener() {
		@Override
		public void onReceive(String name, Object data) {
			if (Ant.BRO_UPDATE.equals(name)) {
				updateMode(false);
			} else if (Ant.BRO_FINISH.equals(name)) {
				updateMode(false);
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		updateMode(true);

		Broadcast.addListener(mBroadcast, Ant.BRO_UPDATE, true);
		Broadcast.addListener(mBroadcast, Ant.BRO_FINISH, true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		Broadcast.removeLsitener(mBroadcast, Ant.BRO_UPDATE);
		Broadcast.removeLsitener(mBroadcast, Ant.BRO_FINISH);
	}
}

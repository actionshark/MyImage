package kk.myimage.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import kk.myimage.R;
import kk.myimage.adapter.DownListAdapter;
import kk.myimage.adapter.LeafAdapter;
import kk.myimage.adapter.DownListAdapter.DataItem;
import kk.myimage.tree.Ant;
import kk.myimage.tree.BranchData;
import kk.myimage.tree.Hole;
import kk.myimage.tree.LeafData;
import kk.myimage.tree.Spider;
import kk.myimage.tree.Worm;
import kk.myimage.tree.Spider.SpiderNode;
import kk.myimage.tree.Worm.IFinishcallback;
import kk.myimage.ui.DownList;
import kk.myimage.ui.IDialogClickListener;
import kk.myimage.ui.SimpleDialog;
import kk.myimage.ui.UiMode;
import kk.myimage.util.AppUtil;
import kk.myimage.util.Broadcast;
import kk.myimage.util.IntentUtil;

public class LeafActivity extends BaseActivity implements UiMode.ICaller,
		UiMode.ICallee {

	public static final int RST_EDIT = 1;

	private UiMode.Mode mMode = UiMode.Mode.Normal;

	private final LeafAdapter mAdapter = new LeafAdapter(this);
	private BranchData mBranchData;
	private String mPath;
	private boolean mIsSpacial = false;

	private TextView mTvTitle;
	private ImageView mIvPaste;
	private ImageView mIvSelect;
	private ImageView mIvMenu;

	private ViewGroup mVgMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 数据
		String path = getIntent().getStringExtra(KEY_PATH);
		mPath = new File(path).getAbsolutePath();
		mIsSpacial = mPath.equals(Ant.RECENT_FILE.getAbsolutePath());
		mBranchData = Ant.getData(mPath);
		if (mBranchData == null) {
			finish();
			return;
		}

		mAdapter.setDataList(mBranchData);

		setContentView(R.layout.activity_leaf);

		// 标题栏
		View title = findViewById(R.id.rl_title);

		title.findViewById(R.id.iv_back).setOnClickListener(
				new OnClickListener() {
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

		mIvPaste = (ImageView) title.findViewById(R.id.iv_paste);
		mIvPaste.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Hole.paste(mPath);
			}
		});

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

		mVgMenu.findViewById(R.id.iv_share).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						List<LeafData> list = mAdapter.getSelectList();
						if (list.size() < 1) {
							Toast.makeText(LeafActivity.this.getApplicationContext(),
									R.string.msg_nothing_selected,
									Toast.LENGTH_SHORT).show();
							return;
						}

						IntentUtil.share(list);

						changeMode(UiMode.Mode.Normal);
					}
				});

		mVgMenu.findViewById(R.id.iv_edit).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						List<LeafData> list = mAdapter.getSelectList();
						if (list.size() < 1) {
							Toast.makeText(LeafActivity.this.getApplicationContext(),
									R.string.msg_nothing_selected,
									Toast.LENGTH_SHORT).show();
							return;
						}

						IntentUtil.edit(list.get(0));

						changeMode(UiMode.Mode.Normal);
					}
				});

		mVgMenu.findViewById(R.id.iv_copy).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						List<LeafData> list = mAdapter.getSelectList();
						if (list.size() < 1) {
							Toast.makeText(LeafActivity.this.getApplicationContext(),
									R.string.msg_nothing_selected,
									Toast.LENGTH_SHORT).show();
							return;
						}

						List<String> leafList = new ArrayList<String>();
						for (LeafData id : list) {
							leafList.add(id.getPath());
						}

						Hole.copy(leafList);

						changeMode(UiMode.Mode.Normal);

						Toast.makeText(LeafActivity.this.getApplicationContext(),
							R.string.msg_clip, Toast.LENGTH_SHORT).show();
					}
				});

		mVgMenu.findViewById(R.id.iv_cut).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						List<LeafData> list = mAdapter.getSelectList();
						if (list.size() < 1) {
							Toast.makeText(LeafActivity.this.getApplicationContext(),
									R.string.msg_nothing_selected,
									Toast.LENGTH_SHORT).show();
							return;
						}

						List<String> leafList = new ArrayList<String>();
						for (LeafData id : list) {
							leafList.add(id.getPath());
						}

						Hole.cut(leafList);

						changeMode(UiMode.Mode.Normal);

						Toast.makeText(LeafActivity.this.getApplicationContext(),
							R.string.msg_clip, Toast.LENGTH_SHORT).show();
					}
				});

		mVgMenu.findViewById(R.id.iv_delete).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						List<LeafData> list = mAdapter.getSelectList();
						if (list.size() < 1) {
							Toast.makeText(LeafActivity.this.getApplicationContext(),
									R.string.msg_nothing_selected,
									Toast.LENGTH_SHORT).show();
							return;
						}

						final List<String> leafList = new ArrayList<String>();
						for (LeafData id : list) {
							leafList.add(id.getPath());
						}

						SimpleDialog confirm = new SimpleDialog(
								LeafActivity.this);
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
													Ant.addUpdate(mPath);
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

		mVgMenu.findViewById(R.id.iv_cancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						changeMode(UiMode.Mode.Normal);
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
		BranchData bd = Ant.getData(mPath);
		if (bd == null) {
			finish();
			return;
		}

		mAdapter.setDataList(bd);

		if (mMode == UiMode.Mode.Select) {
			int total = mAdapter.getCount();
			int select = mAdapter.getSelectCount();

			mTvTitle.setText(String.format("%d/%d", select, total));

			mIvPaste.setVisibility(View.GONE);

			mIvSelect
					.setImageResource(select < total ? R.drawable.select_enable
							: R.drawable.select_disable);
			mIvSelect.setVisibility(View.VISIBLE);

			mIvMenu.setVisibility(View.GONE);

			mVgMenu.setVisibility(View.VISIBLE);
		} else {
			mTvTitle.setText(String.format("%s", mAdapter.getDataList()
					.getName()));

			mIvPaste.setVisibility(Hole.isEmpty() ? View.GONE : View.VISIBLE);

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
		DownList dl = new DownList(LeafActivity.this);
		DownListAdapter adapter = dl.getAdapter();
		List<DataItem> dataList = new ArrayList<DataItem>();

		dataList.add(new DataItem(R.drawable.arrow_up_down, R.string.sort,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						Intent intent = new Intent(LeafActivity.this,
								SortActivity.class);
						intent.putExtra(KEY_MODE, SortActivity.MODE_LEAF);
						startActivity(intent);
					}
				}));

		final int sortIndex = mBranchData.getSortIndex();

		dataList.add(new DataItem(R.drawable.arrow_up,
				sortIndex >= 0 ? R.string.set_top : R.string.cancel_top,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						SpiderNode sn = Spider.getNode(mPath);
						if (sn == null) {
							sn = new SpiderNode();
						}

						if (sortIndex < 0) {
							sn.sortIndex = 0;
						} else {
							sn.sortIndex = -1;
						}

						Spider.setNode(mPath, sn);
						Ant.sortBranch();
					}
				}));

		dataList.add(new DataItem(R.drawable.arrow_down,
				sortIndex <= 0 ? R.string.set_bottom : R.string.cancel_bottom,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						SpiderNode sn = Spider.getNode(mPath);
						if (sn == null) {
							sn = new SpiderNode();
						}

						if (sortIndex > 0) {
							sn.sortIndex = 0;
						} else {
							sn.sortIndex = 1;
						}

						Spider.setNode(mPath, sn);
						Ant.sortBranch();
					}
				}));

		adapter.setDataList(dataList);
		dl.show();
	}

	private Broadcast.IListener mBroadcast = new Broadcast.IListener() {
		@Override
		public void onReceive(String name, Object data) {
			if (Ant.BRO_UPDATE.equals(name)) {
				updateMode(false);
			} else if (Ant.BRO_FINISH.equals(name)) {
				updateMode(false);
			} else if (Ant.BRO_RECENT_UPDATE.equals(name)) {
				updateMode(false);
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RST_EDIT) {
			Ant.refresh(false, true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateMode(true);

		Broadcast.addListener(mBroadcast, Ant.BRO_UPDATE, true);
		Broadcast.addListener(mBroadcast, Ant.BRO_FINISH, true);

		if (mIsSpacial) {
			Broadcast.addListener(mBroadcast, Ant.BRO_RECENT_UPDATE, true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		Broadcast.removeLsitener(mBroadcast, Ant.BRO_UPDATE);
		Broadcast.removeLsitener(mBroadcast, Ant.BRO_FINISH);
		Broadcast.removeLsitener(mBroadcast, Ant.BRO_RECENT_UPDATE);
	}
}

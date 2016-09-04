package kk.myimage.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Environment;

import kk.myimage.R;
import kk.myimage.tree.Spider.SpiderNode;
import kk.myimage.tree.Squi.SortFactor;
import kk.myimage.util.AppUtil;
import kk.myimage.util.Broadcast;
import kk.myimage.util.ImageUtil;
import kk.myimage.util.Logger;
import kk.myimage.util.Setting;

public class Ant {
	public static final String BRO_UPDATE = "ant_update";
	public static final String BRO_FINISH = "ant_finish";
	public static final String BRO_RECENT_UPDATE = "ant_recent_update";

	private static final List<BranchData> sList = new ArrayList<BranchData>();

	private static final Map<String, SpiderNode> sUpdate = new HashMap<String, SpiderNode>();

	private static Boolean sIsRunning = false;
	private static boolean sNeedRefreshUpdate = false;
	private static boolean sNeedRefreshAll = false;

	private static final String NO_MEDIA = ".nomedia";

	public static final int RECENT_NUM = 100;
	public static final File RECENT_FILE = new File(
			AppUtil.getString(R.string.recent));

	public static void refresh(final boolean update, final boolean all) {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				synchronized (sIsRunning) {
					sNeedRefreshUpdate |= update;
					sNeedRefreshAll |= all;
				}

				sRun.run();
			}
		});
	}

	private static final Runnable sRun = new Runnable() {
		@Override
		public void run() {
			synchronized (sIsRunning) {
				if (sIsRunning) {
					return;
				}
			}

			boolean showHide = Setting.getShowHide();
			List<SortFactor> bf = Squi.getBranchFactors(true);
			List<SortFactor> lf = Squi.getLeafFactors();

			synchronized (sIsRunning) {
				if (sNeedRefreshUpdate) {
					synchronized (sUpdate) {
						Map<String, SpiderNode> map = new HashMap<String, SpiderNode>(
								sUpdate);

						for (Entry<String, SpiderNode> entry : map.entrySet()) {
							visitDir(new File(entry.getKey()), showHide,
									entry.getValue().hidden, bf, lf, false);

							sUpdate.remove(entry.getKey());
						}
					}

					sNeedRefreshUpdate = false;
				}
			}

			synchronized (sIsRunning) {
				if (sNeedRefreshAll) {
					String path = Environment.getExternalStorageDirectory()
							.getPath();

					visitDir(new File(path), showHide, false, bf, lf, true);

					Map<String, SpiderNode> map = new HashMap<String, SpiderNode>();

					synchronized (sList) {
						int len = sList.size();
						
						for (int i = 1; i < len;) {
							BranchData bd = sList.get(i);
							Boolean isNew = (Boolean) bd.getTag();

							if (isNew != null && isNew) {
								SpiderNode sn = Spider.getNode(bd.getPath());
								if (sn == null) {
									sn = new SpiderNode();
								}
								map.put(bd.getPath(), sn);

								bd.setTag(false);
								
								i++;
							} else {
								sList.remove(i);
							}
						}
					}

					Spider.setAllNode(map);

					sNeedRefreshAll = false;
				}
			}

			recheckRecent();

			sortBranch();
			sortLeaf();

			synchronized (sIsRunning) {
				if (sNeedRefreshUpdate || sNeedRefreshAll) {
					refresh(sNeedRefreshUpdate, sNeedRefreshAll);
				}
			}
		}
	};

	private static void visitDir(File file, boolean showHide, boolean isHidden,
			List<SortFactor> bf, List<SortFactor> lf, boolean rec) {

		try {
			String path = file.getAbsolutePath();
			if (!file.exists() || !file.canRead() || !file.isDirectory()) {
				Spider.removeNode(path);
				return;
			}

			isHidden = isHidden || file.isHidden();
			if (!showHide && isHidden) {
				Spider.removeNode(path);
				return;
			}

			BranchData bd = new BranchData(file);

			File[] children = file.listFiles();
			for (File child : children) {
				if (child.isFile()) {
					isHidden = isHidden
							|| child.getName().toLowerCase(Locale.ENGLISH)
									.equals(NO_MEDIA);
					if (!showHide && isHidden) {
						Spider.removeNode(path);
						return;
					}

					if (!child.canRead() || !showHide && child.isHidden()) {
						continue;
					}

					if (ImageUtil.isImage(child)) {
						LeafData ld = new LeafData(child);
						synchronized (bd) {
							Squi.insert(bd.getChildren(false), ld, lf);
						}

						BranchData recent = getData(
								RECENT_FILE.getAbsolutePath(), false);
						if (recent == null) {
							recent = new RecentBranchData();

							synchronized (sList) {
								Squi.insert(sList, recent, bf);
							}
						}

						synchronized (recent) {
							List<LeafData> list = recent.getChildren(false);
							int len = Math.min(list.size(), RECENT_NUM);
							long lm = child.lastModified();
							String ap = child.getAbsolutePath();
							boolean needAdd = true;
							boolean needBro = false;

							for (int i = 0; i < len; i++) {
								long lt = list.get(i).getFile().lastModified();

								if (lm > lt) {
									list.add(i, ld);
									needAdd = false;
									needBro = true;
									break;
								} else if (lm == lt) {
									if (list.get(i).getPath().equals(ap)) {
										needAdd = false;
										break;
									}
								}
							}

							if (needAdd) {
								list.add(ld);
								needBro = true;
							}

							if (list.size() > RECENT_NUM) {
								list.remove(RECENT_NUM);
							}

							if (needBro) {
								Broadcast.send(BRO_RECENT_UPDATE, recent);
							}
						}
					}
				}
			}

			if (bd.getChildNum() > 0) {
				bd.setTag(rec);

				SpiderNode sn = Spider.getNode(path);
				if (sn == null || sn.hidden != isHidden) {
					sn = new SpiderNode(sn);
					sn.hidden = isHidden;
					Spider.setNode(path, sn);
				}

				synchronized (sList) {
					boolean found = false;
					int len = sList.size();

					for (int i = 0; i < len; i++) {
						BranchData tmp = sList.get(i);

						if (tmp.getPath().equals(path)) {
							found = true;
							sList.set(i, bd);
							break;
						}
					}

					if (found == false) {
						Squi.insert(sList, bd, bf);
					}
				}

				Broadcast.send(BRO_UPDATE, bd);
			} else {
				Spider.removeNode(path);

				synchronized (sList) {
					int len = sList.size();

					for (int i = 0; i < len; i++) {
						BranchData tmp = sList.get(i);

						if (tmp.getPath().equals(path)) {
							sList.remove(i);
							Broadcast.send(BRO_UPDATE, bd);
							break;
						}
					}
				}
			}

			if (rec) {
				for (File child : children) {
					visitDir(child, showHide, isHidden, bf, lf, rec);
				}
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	private static void recheckRecent() {
		try {
			synchronized (sList) {
				BranchData recent = getData(RECENT_FILE.getAbsolutePath(),
						false);
				if (recent == null) {
					recent = new RecentBranchData();

					sList.add(0, recent);
				}

				synchronized (recent) {
					List<LeafData> children = recent.getChildren(false);

					for (int i = children.size() - 1; i >= 0; i--) {
						File file = children.get(i).getFile();
						if (!file.exists() || !file.canRead()
								|| !ImageUtil.isImage(file)) {
							children.remove(i);
							continue;
						}

						String parent = file.getParent();
						if (Spider.getNode(parent) == null) {
							children.remove(i);
							continue;
						}
					}
				}

				Broadcast.send(BRO_RECENT_UPDATE, recent);
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static boolean isRunning() {
		return sIsRunning;
	}

	public static List<BranchData> getData(boolean clone) {
		List<BranchData> list = new ArrayList<BranchData>();

		synchronized (sList) {
			for (BranchData bd : sList) {
				list.add(clone ? bd.clone() : bd);
			}
		}

		return list;
	}

	public static BranchData getData(String path) {
		return getData(path, true);
	}

	private static BranchData getData(String path, boolean clone) {
		synchronized (sList) {
			for (BranchData bd : sList) {
				if (bd.getPath().equals(path)) {
					return clone ? bd.clone() : bd;
				}
			}
		}

		return null;
	}

	public static void sortBranch() {
		List<SortFactor> sf = Squi.getBranchFactors(true);

		synchronized (sList) {
			Squi.sort(sList, sf);
		}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
		Broadcast.send(BRO_FINISH, null);
	}

	public static void sortLeaf() {
		List<SortFactor> sf = Squi.getLeafFactors();

		synchronized (sList) {
			for (BranchData bd : sList) {
				synchronized (bd) {
					Squi.sort(bd.getChildren(false), sf);
				}

				Broadcast.send(BRO_UPDATE, null);
			}
		}

		Broadcast.send(BRO_FINISH, null);
	}

	public static void addUpdate(String path) {
		if (path == null) {
			return;
		}

		synchronized (sUpdate) {
			if (!sUpdate.containsKey(path)) {
				sUpdate.put(path, new SpiderNode());
			}
		}
	}

	public static void addUpdate(String path, SpiderNode sn) {
		if (path == null) {
			return;
		}

		synchronized (sUpdate) {
			sUpdate.put(path, sn);
		}
	}
}

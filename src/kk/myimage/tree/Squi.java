package kk.myimage.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import kk.myimage.util.AppUtil;
import kk.myimage.util.Logger;
import kk.myimage.util.Setting;

public class Squi {
	public static enum SortType {
		Index, Name, Num, Path, Size, Time,
	}

	public static abstract class SortFactor implements Comparator<TreeData>,
			Cloneable {

		public final SortType type;
		public final String text;
		public boolean up;

		public SortFactor(SortType type) {
			this.type = type;

			this.text = AppUtil.getString("sort_"
					+ type.name().toLowerCase(Locale.ENGLISH));
		}

		@Override
		public int compare(TreeData a, TreeData b) {
			int ret = cmp(a, b);

			if (up) {
				return ret;
			} else {
				return -ret;
			}
		}

		abstract protected int cmp(TreeData a, TreeData b);

		@Override
		public SortFactor clone() {
			SortFactor temp = getFactor(type.name());
			temp.up = up;

			return temp;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SortFactor) {
				SortFactor temp = (SortFactor) obj;

				return type == temp.type && up == temp.up;
			}

			return false;
		}
	}

	public static class SortFactorIndex extends SortFactor {
		public SortFactorIndex() {
			super(SortType.Index);
			up = true;
		}

		@Override
		public int cmp(TreeData a, TreeData b) {
			int ai = a.getSortIndex();
			int bi = b.getSortIndex();

			return ai - bi;
		}
	}

	public static class SortFactorPath extends SortFactor {
		public SortFactorPath() {
			super(SortType.Path);
		}

		@Override
		public int cmp(TreeData a, TreeData b) {
			String ap = a.getPath().toLowerCase(Locale.ENGLISH);
			String bp = b.getPath().toLowerCase(Locale.ENGLISH);

			return ap.compareTo(bp);
		}
	}

	public static class SortFactorName extends SortFactor {
		public SortFactorName() {
			super(SortType.Name);
		}

		@Override
		public int cmp(TreeData a, TreeData b) {
			String an = a.getName().toLowerCase(Locale.ENGLISH);
			String bn = b.getName().toLowerCase(Locale.ENGLISH);

			return an.compareTo(bn);
		}
	}

	public static class SortFactorTime extends SortFactor {
		public SortFactorTime() {
			super(SortType.Time);
		}

		@Override
		public int cmp(TreeData a, TreeData b) {
			long at = a.getFile().lastModified();
			long bt = b.getFile().lastModified();

			if (at < bt) {
				return -1;
			} else if (at > bt) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public static class SortFactorNum extends SortFactor {
		public SortFactorNum() {
			super(SortType.Num);
		}

		@Override
		public int cmp(TreeData a, TreeData b) {
			BranchData ad = (BranchData) a;
			BranchData bd = (BranchData) b;

			return ad.getChildNum() - bd.getChildNum();
		}
	}

	public static class SortFactorSize extends SortFactor {
		public SortFactorSize() {
			super(SortType.Size);
		}

		@Override
		public int cmp(TreeData a, TreeData b) {
			long al = a.getFile().length();
			long bl = b.getFile().length();

			if (al < bl) {
				return -1;
			} else if (al > bl) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private static List<SortFactor> sBranchFactors;
	private static List<SortFactor> sLeafFactors;

	private static SortFactor getFactor(String type) {
		SortFactor factor = null;

		try {
			factor = (SortFactor) Class.forName(
					"kk.myimage.tree.Squi$SortFactor" + type).newInstance();
		} catch (Exception e) {
			Logger.print(null, e, type);
		}

		if (factor == null) {
			factor = new SortFactorPath();
		}

		return factor;
	}

	private static List<SortFactor> cloneFactors(List<SortFactor> factors) {
		List<SortFactor> list = new ArrayList<SortFactor>(factors.size());

		for (SortFactor factor : factors) {
			SortFactor temp = factor.clone();

			list.add(temp);
		}

		return list;
	}

	public static synchronized void setBranchFactors(List<SortFactor> factors) {
		sBranchFactors = cloneFactors(factors);

		try {
			JSONArray ja = new JSONArray();

			int len = factors.size();
			for (int i = 0; i < len; i++) {
				SortFactor factor = factors.get(i);
				JSONObject jo = new JSONObject();

				jo.put("type", factor.type.name());
				jo.put("up", factor.up);

				ja.put(i, jo);
			}

			Setting.setSortBranch(ja.toString());
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static synchronized void setLeafFactors(List<SortFactor> factors) {
		sLeafFactors = cloneFactors(factors);

		try {
			JSONArray ja = new JSONArray();

			int len = factors.size();
			for (int i = 0; i < len; i++) {
				SortFactor factor = factors.get(i);
				JSONObject jo = new JSONObject();

				jo.put("type", factor.type.name());
				jo.put("up", factor.up);

				ja.put(i, jo);
			}

			Setting.setSortLeaf(ja.toString());
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static synchronized List<SortFactor> getBranchFactors(boolean all) {
		if (sBranchFactors == null) {
			sBranchFactors = new ArrayList<SortFactor>();

			SortFactor[] array = new SortFactor[] { new SortFactorPath(),
					new SortFactorName(), new SortFactorNum(),
					new SortFactorTime(), };

			try {
				String str = Setting.getSortBranch();
				JSONArray ja = new JSONArray(str);

				int len = ja.length();
				for (int i = 0; i < len; i++) {
					JSONObject jo = ja.getJSONObject(i);

					String type = jo.getString("type");
					boolean up = jo.getBoolean("up");

					SortFactor factor = getFactor(type);
					factor.up = up;

					for (int j = 0; j < array.length; j++) {
						if (array[j] != null && array[j].type == factor.type) {
							sBranchFactors.add(factor);
							array[j] = null;
						}
					}
				}
			} catch (Exception e) {
				Logger.print(null, e);
			}

			for (int j = 0; j < array.length; j++) {
				if (array[j] != null) {
					sBranchFactors.add(array[j]);
				}
			}
		}

		List<SortFactor> list = cloneFactors(sBranchFactors);

		if (all) {
			list.add(0, new SortFactorIndex());
		}

		return list;
	}

	public static synchronized List<SortFactor> getLeafFactors() {
		if (sLeafFactors == null) {
			sLeafFactors = new ArrayList<SortFactor>();

			SortFactor[] array = new SortFactor[] { new SortFactorPath(),
					new SortFactorName(), new SortFactorTime(),
					new SortFactorSize(), };

			try {
				String str = Setting.getSortLeaf();
				JSONArray ja = new JSONArray(str);

				int len = ja.length();
				for (int i = 0; i < len; i++) {
					JSONObject jo = ja.getJSONObject(i);

					String type = jo.getString("type");
					boolean up = jo.getBoolean("up");

					SortFactor factor = getFactor(type);
					factor.up = up;

					for (int j = 0; j < array.length; j++) {
						if (array[j] != null && array[j].type == factor.type) {
							sLeafFactors.add(factor);
							array[j] = null;
						}
					}
				}
			} catch (Exception e) {
				Logger.print(null, e);
			}

			for (int j = 0; j < array.length; j++) {
				if (array[j] != null) {
					sLeafFactors.add(array[j]);
				}
			}
		}

		return cloneFactors(sLeafFactors);
	}

	private static int compare(List<SortFactor> factors, TreeData a, TreeData b) {
		for (SortFactor factor : factors) {
			int ret = factor.compare(a, b);
			if (ret != 0) {
				return ret;
			}
		}

		return 0;
	}

	public static void sort(List<? extends TreeData> list,
			final List<SortFactor> factors) {

		Collections.sort(list, new Comparator<TreeData>() {
			@Override
			public int compare(TreeData a, TreeData b) {
				return Squi.compare(factors, a, b);
			}
		});
	}

	public static <T extends TreeData> void insert(List<T> list, T data,
			List<SortFactor> factors) {

		int len = list.size();

		for (int i = 0; i < len; i++) {
			if (compare(factors, data, list.get(i)) < 0) {
				list.add(i, data);
				return;
			}
		}

		list.add(data);
	}
}

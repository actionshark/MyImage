package kk.myimage.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import kk.myimage.activity.BaseActivity;

public class Setting {
	public static final String KEY_SHOW_HIDE = "show_hide";

	public static final String KEY_SORT_BRANCH = "sort_brach";
	public static final String KEY_SORT_LEAF = "sort_leaf";

	public static final String KEY_LOCK_TRANSFORM = "lock_transform";
	public static final String KEY_SHOW_STATUS = "show_status";
	public static final String KEY_SHOW_TITLE = "show_title";

	private static final SharedPreferences sPrefer;

	static {
		sPrefer = BaseActivity.ca().getSharedPreferences("setting",
				Context.MODE_PRIVATE);
	}

	public static void setShowHide(boolean show) {
		Editor editor = sPrefer.edit();
		editor.putBoolean(KEY_SHOW_HIDE, show);
		editor.commit();
	}

	public static boolean getShowHide() {
		return sPrefer.getBoolean(KEY_SHOW_HIDE, false);
	}

	public static void setSortBranch(String value) {
		Editor editor = sPrefer.edit();
		editor.putString(KEY_SORT_BRANCH, value);
		editor.commit();
	}

	public static String getSortBranch() {
		return sPrefer.getString(KEY_SORT_BRANCH, null);
	}

	public static void setSortLeaf(String value) {
		Editor editor = sPrefer.edit();
		editor.putString(KEY_SORT_LEAF, value);
		editor.commit();
	}

	public static String getSortLeaf() {
		return sPrefer.getString(KEY_SORT_LEAF, null);
	}

	public static void setLockTransform(boolean lock) {
		Editor editor = sPrefer.edit();
		editor.putBoolean(KEY_LOCK_TRANSFORM, lock);
		editor.commit();
	}

	public static boolean getLockTransform() {
		return sPrefer.getBoolean(KEY_LOCK_TRANSFORM, true);
	}

	public static void setShowStatus(boolean show) {
		Editor editor = sPrefer.edit();
		editor.putBoolean(KEY_SHOW_STATUS, show);
		editor.commit();
	}

	public static boolean getShowStatus() {
		return sPrefer.getBoolean(KEY_SHOW_STATUS, false);
	}

	public static void setShowTitle(boolean show) {
		Editor editor = sPrefer.edit();
		editor.putBoolean(KEY_SHOW_TITLE, show);
		editor.commit();
	}

	public static boolean getShowTitle() {
		return sPrefer.getBoolean(KEY_SHOW_TITLE, true);
	}
}

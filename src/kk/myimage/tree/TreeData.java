package kk.myimage.tree;

import java.io.File;

import android.graphics.Bitmap;

import kk.myimage.tree.Spider.SpiderNode;

public abstract class TreeData implements Cloneable {
	protected final String mPath;
	protected final String mName;

	private Object mTag;

	public TreeData(File file) {
		mPath = file.getAbsolutePath();
		mName = file.getName();
	}

	public TreeData(String path, String name) {
		mPath = path;
		mName = name;
	}

	@Override
	public abstract TreeData clone();

	public void setTag(Object tag) {
		mTag = tag;
	}

	public Object getTag() {
		return mTag;
	}

	public String getPath() {
		return mPath;
	}

	public String getName() {
		return mName;
	}

	public File getFile() {
		return new File(mPath);
	}

	public abstract Bitmap getThum();

	public int getSortIndex() {
		SpiderNode sn = Spider.getNode(mPath);
		if (sn != null) {
			return sn.sortIndex;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TreeData) {
			TreeData td = (TreeData) obj;

			return mPath.equals(td.mPath);
		}

		return false;
	}
}

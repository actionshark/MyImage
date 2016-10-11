package kk.myimage.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myimage.tree.Spider.SpiderNode;
import kk.myimage.util.ImageUtil;

public class BranchData extends TreeData {
	private final List<LeafData> mChildren = new ArrayList<LeafData>();

	public BranchData(File file) {
		super(file);
	}

	public BranchData(String path, String name) {
		super(path, name);
	}

	@Override
	public synchronized BranchData clone() {
		BranchData bd = new BranchData(mPath, mName);

		for (LeafData ld : mChildren) {
			bd.mChildren.add(ld.clone());
		}

		return bd;
	}

	public synchronized List<LeafData> getChildren(boolean clone) {
		if (clone) {
			return new ArrayList<LeafData>(mChildren);
		} else {
			return mChildren;
		}
	}

	public synchronized int getChildNum() {
		return mChildren.size();
	}

	public synchronized String getThumPath() {
		SpiderNode sn = Spider.getNode(mPath);
		if (sn != null && sn.thum != null) {
			File file = new File(mPath, sn.thum);

			if (file.exists() && file.canRead() && file.isFile()
					&& ImageUtil.isImage(file)) {
				return file.getAbsolutePath();
			}
		}

		if (mChildren.size() > 0) {
			return mChildren.get(0).getPath();
		} else {
			return null;
		}
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (super.equals(obj) == false) {
			return false;
		}

		if (obj instanceof BranchData == false) {
			return false;
		}

		BranchData bd = (BranchData) obj;

		if (mChildren.size() != bd.mChildren.size()) {
			return false;
		}

		int len = mChildren.size();
		for (int i = 0; i < len; i++) {
			if (mChildren.get(i).equals(bd.mChildren.get(i)) == false) {
				return false;
			}
		}

		return true;
	}
}

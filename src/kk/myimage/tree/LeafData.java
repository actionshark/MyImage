package kk.myimage.tree;

import java.io.File;

public class LeafData extends TreeData {
	public LeafData(File file) {
		super(file);
	}

	public LeafData(String path, String name) {
		super(path, name);
	}

	@Override
	public synchronized LeafData clone() {
		return new LeafData(mPath, mName);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof LeafData && super.equals(obj);
	}
}

package kk.myimage.tree;

import java.util.List;

public class RecentBranchData extends BranchData {
	public RecentBranchData() {
		super(Ant.RECENT_FILE);
	}

	@Override
	public synchronized RecentBranchData clone() {
		RecentBranchData rbd = new RecentBranchData();
		List<LeafData> la = getChildren(false);
		List<LeafData> lb = rbd.getChildren(false);

		for (LeafData ld : la) {
			lb.add(ld.clone());
		}

		return rbd;
	}

	@Override
	public int getSortIndex() {
		return -2;
	}
}

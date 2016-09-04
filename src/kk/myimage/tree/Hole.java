package kk.myimage.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.tree.Worm.IFinishcallback;
import kk.myimage.util.AppUtil;

public class Hole {
	public static enum Type {
		Copy, Cut,
	}

	private static Type sType;
	private static List<String> sList;

	public static synchronized boolean isEmpty() {
		return sList == null;
	}

	public static synchronized void clear() {
		sList = null;
	}

	public static synchronized void copy(List<String> list) {
		sType = Type.Copy;
		sList = list;
	}

	public static synchronized void cut(List<String> list) {
		sType = Type.Cut;
		sList = list;
	}

	public static synchronized void paste(final String target) {
		if (isEmpty()) {
			AppUtil.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(BaseActivity.ca().getApplicationContext(),
							R.string.msg_paste_nothing, Toast.LENGTH_SHORT)
							.show();
				}
			});
			return;
		}

		final List<String> list = new ArrayList<String>(sList);
		clear();

		IFinishcallback callback = new IFinishcallback() {
			@Override
			public void onFinish() {
				Ant.addUpdate(target);

				for (String path : list) {
					File file = new File(path);
					Ant.addUpdate(file.getParent());
				}
			}
		};

		if (sType == Type.Cut) {
			Worm.cutLeaf(list, target, callback);
		} else {
			Worm.copyLeaf(list, target, callback);
		}
	}
}

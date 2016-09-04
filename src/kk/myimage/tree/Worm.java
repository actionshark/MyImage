package kk.myimage.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.Dialog;

import kk.myimage.R;
import kk.myimage.activity.BaseActivity;
import kk.myimage.ui.IDialogClickListener;
import kk.myimage.ui.SimpleDialog;
import kk.myimage.util.AppUtil;
import kk.myimage.util.Logger;

public class Worm {
	public static final String COPY_FILE_NAME = "temp";

	public static class ProcessData {
		public int total = 0;
		public int succeed = 0;
		public int failed = 0;
		public boolean ended = false;
	}

	public static interface IOperation {
		public boolean operate(int index) throws Exception;
	}

	public static interface IFinishcallback {
		public void onFinish() throws Exception;
	}

	public static synchronized boolean delete(String path) {
		try {
			File file = new File(path);
			file.delete();
		} catch (Exception e) {
			Logger.print(null, e);
			return false;
		}

		return true;
	}

	public static synchronized boolean copy(String fromPath, String toDir) {
		try {
			File from = new File(fromPath);

			String name = from.getName();
			String subfix = "";
			int index = name.lastIndexOf('.');
			if (index != -1) {
				subfix = name.substring(index, name.length());
			}

			File to = new File(toDir, name);
			for (int i = 1; to.exists(); i++) {
				to = new File(toDir, COPY_FILE_NAME + "_" + i + subfix);
			}

			InputStream is = new FileInputStream(from);
			OutputStream os = new FileOutputStream(to);
			byte[] buf = new byte[1024 * 1024];
			int len = 0;

			while ((len = is.read(buf, 0, buf.length)) != -1) {
				os.write(buf, 0, len);
			}

			is.close();
			os.close();
		} catch (Exception e) {
			Logger.print(null, e);
			return false;
		}

		return true;
	}

	public static void operateLeaf(final int total, final int proessText,
			final int endedText, final IOperation operation,
			final IFinishcallback callback) {

		final ProcessData pd = new ProcessData();
		pd.total = total;

		final SimpleDialog dialog = new SimpleDialog(BaseActivity.ca());
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.setContent(AppUtil.getString(proessText, pd.total, 0, 0));
		dialog.setButtons(new int[] { R.string.cancel });
		dialog.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				if (index == 0) {
					if (pd.ended) {
						dialog.dismiss();
					} else {
						pd.ended = true;
					}
				}
			}
		});
		dialog.show();

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < total; i++) {
					if (pd.ended) {
						break;
					}

					boolean result = false;
					try {
						result = operation.operate(i);
					} catch (Exception e) {
						result = false;
					}

					if (result) {
						pd.succeed++;
					} else {
						pd.failed++;
					}

					if (i < total) {
						AppUtil.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dialog.setContent(AppUtil.getString(proessText,
										pd.total, pd.succeed, pd.failed));
							}
						});
					}
				}

				pd.ended = true;

				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setContent(AppUtil.getString(endedText,
								pd.succeed, pd.failed));
						dialog.setButtons(new int[] { R.string.ok });
					}
				});

				if (callback != null) {
					try {
						callback.onFinish();
					} catch (Exception e) {
						Logger.print(null, e);
					}
				}

				Ant.refresh(true, false);
			}
		});
	}

	public static void deleteLeaf(final List<String> list,
			IFinishcallback callback) {
		operateLeaf(list.size(), R.string.msg_delete_process,
				R.string.msg_delete_result, new IOperation() {
					@Override
					public boolean operate(int index) throws Exception {
						return delete(list.get(index));
					}
				}, callback);
	}

	public static void copyLeaf(final List<String> list, final String target,
			IFinishcallback callback) {

		operateLeaf(list.size(), R.string.msg_copy_process,
				R.string.msg_copy_result, new IOperation() {
					@Override
					public boolean operate(int index) throws Exception {
						return copy(list.get(index), target);
					}
				}, callback);
	}

	public static void cutLeaf(final List<String> list, final String target,
			IFinishcallback callback) {

		operateLeaf(list.size(), R.string.msg_cut_process,
				R.string.msg_cut_result, new IOperation() {
					@Override
					public boolean operate(int index) throws Exception {
						String path = list.get(index);
						return copy(path, target) && delete(path);
					}
				}, callback);
	}
}

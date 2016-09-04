package kk.myimage.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import kk.myimage.R;
import kk.myimage.util.AppUtil;

public abstract class BaseActivity extends Activity {
	private static BaseActivity sActivity;

	public static final String KEY_PATH = "key_path";
	public static final String KEY_INDEX = "key_index";
	public static final String KEY_MODE = "key_mode";

	public static synchronized BaseActivity ca() {
		return sActivity;
	}

	protected View mStatusBar;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle bundle) {
		synchronized (BaseActivity.class) {
			if (sActivity == null) {
				sActivity = this;
			}
		}

		super.onCreate(bundle);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

			mStatusBar = new View(this);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					AppUtil.getStatusBarHeight());
			params.gravity = Gravity.TOP;
			mStatusBar.setLayoutParams(params);
			mStatusBar.setBackgroundColor(AppUtil.getColor(R.color.title_bg));

			ViewGroup decor = (ViewGroup) getWindow().getDecorView();
			decor.addView(mStatusBar);
		}
	}

	@Override
	protected void onResume() {
		synchronized (BaseActivity.class) {
			sActivity = this;
		}

		super.onResume();
	}
}

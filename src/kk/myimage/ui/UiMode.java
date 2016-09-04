package kk.myimage.ui;

public class UiMode {
	public static enum Mode {
		Normal, Select, Detail,
	}

	public static interface ICaller {
		public void changeMode(Mode mode);

		public void updateMode(boolean force);
	}

	public static interface ICallee {
		public void onModeChange(Mode mode);

		public void onModeUpdate(boolean force);
	}
}

package kk.myimage.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;

import kk.myimage.activity.BaseActivity;
import kk.myimage.tree.LeafData;

public class IntentUtil {
	public static void edit(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setDataAndType(uri, "image/*");

		// intent.putExtra("crop", "true");
		// intent.putExtra("output", Uri.fromFile(new
		// File("/mnt/sdcard/temp")));
		// intent.putExtra("outputFormat", "JPEG");
		// intent.putExtra("return-data", true);

		BaseActivity.ca().startActivity(intent);
	}

	public static void edit(LeafData ld) {
		edit(Uri.fromFile(ld.getFile()));
	}

	public static void share(ArrayList<Uri> list) {
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list);

		BaseActivity.ca().startActivity(intent);
	}

	public static void share(List<LeafData> list) {
		ArrayList<Uri> temp = new ArrayList<Uri>();
		for (LeafData ld : list) {
			temp.add(Uri.fromFile(ld.getFile()));
		}
		share(temp);
	}

	public static void share(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, uri);

		BaseActivity.ca().startActivity(intent);
	}

	public static void share(LeafData ld) {
		share(Uri.fromFile(ld.getFile()));
	}
}

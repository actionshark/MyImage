package kk.myimage.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import kk.myimage.R;

public class ImageUtil {
	public static final String BRO_THUM_GOT = "image_util_thum_got";
	public static final String BRO_IMAGE_GOT = "image_util_image_got";

	private static final String[] IMG_SUFFIX = new String[] { ".jpg", ".jpeg",
			".png", };

	private static int sThumWidth;
	private static int sThumHeight;

	private static final int MAX_WIDTH = 4096;
	private static final int MAX_HEIGHT = 4096;

	private static class BitmapNode {
		public Bitmap bitmap;
		public long token;

		public BitmapNode(Bitmap bitmap, long token) {
			this.bitmap = bitmap;
			this.token = token;
		}
	}

	private static final int THUM_CACHE_SIZE = 32;
	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, BitmapNode> THUM_CACHE =
			new LinkedHashMap<String, BitmapNode>(THUM_CACHE_SIZE, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, BitmapNode> eldest) {
			return size() > THUM_CACHE_SIZE;
		}
	};

	private static final int IMAGE_CACHE_SIZE = 4;
	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, BitmapNode> IMAGE_CACHE =
			new LinkedHashMap<String, BitmapNode>(IMAGE_CACHE_SIZE, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, BitmapNode> eldest) {
			return size() > IMAGE_CACHE_SIZE;
		}
	};

	static {
		sThumWidth = (AppUtil.getScreenWidth(false) - AppUtil
				.getDimenInt(R.dimen.grid_hor_gap)) / 2;

		sThumHeight = AppUtil.getDimenInt(R.dimen.grid_height);
	}

	public static boolean isImage(File file) {
		String name = file.getName().toLowerCase(Locale.ENGLISH);

		for (String suffix : IMG_SUFFIX) {
			if (name.endsWith(suffix)) {
				return true;
			}
		}

		return false;
	}

	public static Options getOptions(String path) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		return options;
	}

	public static Bitmap getThum(final String path) {
		synchronized (THUM_CACHE) {
			BitmapNode bitmapNode = THUM_CACHE.get(path);
			
			if (bitmapNode == null) {
				final BitmapNode bn = new BitmapNode(null, System.currentTimeMillis());
				THUM_CACHE.put(path, bn);

				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							boolean selected = true;
							
							synchronized (THUM_CACHE) {
								if (THUM_CACHE.containsKey(path) == false) {
									return;
								}
								
								for (BitmapNode bitmapNode : THUM_CACHE.values()) {
									if (bitmapNode.bitmap == null && bn.token < bitmapNode.token) {
										selected = false;
										break;
									}
								}
							}
								
							if (selected) {
								Options options = getOptions(path);
								int scaleWidth = (options.outWidth + sThumWidth - 1)
										/ sThumWidth;
								int scaleHeight = (options.outHeight + sThumHeight - 1)
										/ sThumHeight;
								options = new Options();
								options.inSampleSize = Math.max(scaleWidth, scaleHeight);

								Bitmap bmp = BitmapFactory.decodeFile(path, options);
								synchronized (THUM_CACHE) {
									bn.bitmap = bmp;
								}

								Broadcast.send(BRO_THUM_GOT, path);
								return;
							}
							
							try {
								Thread.sleep(100);
							} catch (Exception e) {
								Logger.print(null, e);
							}
						}
					}
				});

				return null;
			} else {
				return bitmapNode.bitmap;
			}
		}
	}

	public static Bitmap getImage(final String path) {
		synchronized (IMAGE_CACHE) {
			BitmapNode bitmapNode = IMAGE_CACHE.get(path);

			if (bitmapNode == null) {
				final BitmapNode bn = new BitmapNode(null, System.currentTimeMillis());
				IMAGE_CACHE.put(path, bn);

				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							boolean selected = true;
							
							synchronized (IMAGE_CACHE) {
								if (IMAGE_CACHE.containsKey(path) == false) {
									return;
								}
								
								for (BitmapNode bitmapNode : IMAGE_CACHE.values()) {
									if (bitmapNode.bitmap == null && bn.token < bitmapNode.token) {
										selected = false;
										break;
									}
								}
							}
								
							if (selected) {
								Options oa = getOptions(path);
								int sample = 1;
		
								if (oa.outWidth > MAX_WIDTH * sample) {
									sample = (oa.outWidth + MAX_WIDTH - 1) / MAX_WIDTH;
								}
		
								if (oa.outHeight > MAX_HEIGHT * sample) {
									sample = (oa.outHeight + MAX_HEIGHT - 1)
											/ MAX_HEIGHT;
								}
		
								Options ob = new Options();
								ob.inSampleSize = 1;
								while (ob.inSampleSize < sample) {
									ob.inSampleSize <<= 1;
								}
		
								Bitmap bmp = BitmapFactory.decodeFile(path, ob);
								synchronized (bn) {
									bn.bitmap = bmp;
								}
		
								Broadcast.send(BRO_IMAGE_GOT, path);
								return;
							}
							
							try {
								Thread.sleep(100);
							} catch (Exception e) {
								Logger.print(null, e);
							}
						}
					}
				});

				return null;
			} else {
				return bitmapNode.bitmap;
			}
		}
	}
}

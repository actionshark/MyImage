package kk.myimage.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;

import kk.myimage.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.SystemClock;

public class ImageUtil {
	public static interface IImageListenner {
		public void onImageGot(Bitmap bmp);
	}
	
	private static class DrawableNode {
		public String path;
		public int width;
		public int height;
		public long token;
		public Bitmap bmp;
		public IImageListenner listenner;
		
		public DrawableNode clone() {
			DrawableNode node = new DrawableNode();
			
			node.path = path;
			node.width = width;
			node.height = height;
			node.token = token;
			node.bmp = bmp;
			node.listenner = listenner;
			
			return node;
		}
	}
	
	private static final String[] IMG_SUFFIX = new String[] { ".jpg", ".jpeg",
		".png", ".gif", };

	private static final int THUM_CACHE_SIZE = 20;
	private static final LinkedHashMap<String, DrawableNode> THUM_CACHE =
			new LinkedHashMap<String, DrawableNode>(THUM_CACHE_SIZE, 0.75f, true) {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, DrawableNode> eldest) {
			return size() > THUM_CACHE_SIZE;
		}
	};

	private static final int IMAGE_CACHE_SIZE = 4;
	private static final LinkedHashMap<String, DrawableNode> IMAGE_CACHE =
			new LinkedHashMap<String, DrawableNode>(IMAGE_CACHE_SIZE, 0.75f, true) {
		
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, DrawableNode> eldest) {
			return size() > IMAGE_CACHE_SIZE;
		}
	};

	public static boolean isImage(File file) {
		String name = file.getName().toLowerCase(Locale.ENGLISH);

		for (String suffix : IMG_SUFFIX) {
			if (name.endsWith(suffix)) {
				return true;
			}
		}

		return false;
	}
	
	public static Options getSize(String path) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		return options;
	}

	private static boolean sIsThumRunning = false;
	private static boolean sIsImageRunning = false;

	public static void getThum(final String path, final int width, final int height, final IImageListenner listenner) {
		AppUtil.runOnNewThread(new Runnable() {
			public void run() {
				synchronized (THUM_CACHE) {
					DrawableNode node = THUM_CACHE.get(path);
					
					if (node != null && node.bmp != null) {
						if (listenner != null) {
							final Bitmap bmp = node.bmp;
							
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									listenner.onImageGot(bmp);
								}
							});
						}
						
						return;
					}
					
					if (node == null) {
						node = new DrawableNode();
						THUM_CACHE.put(path, node);
					}
					
					node.path = path;
					node.width = (width > 0 && width < 1024) ? width : 512;
					node.height = (height > 0 && height < 1024) ? height : 512;
					node.token = SystemClock.elapsedRealtime();
					node.listenner = listenner;
					
					if (sIsThumRunning) {
						return;
					}
					sIsThumRunning = true;
				}
				
				try {
					while (true) {
						DrawableNode node = null;
						
						synchronized (THUM_CACHE) {
							for (DrawableNode n : THUM_CACHE.values()) {
								if (n.bmp == null) {
									if (node == null || n.token > node.token) {
										node = n;
									}
								}
							}
						}
						
						if (node == null) {
							return;
						}
						
						final DrawableNode nd;
						synchronized (THUM_CACHE) {
							nd = node.clone();
						}
						
						try {
							Options size = new Options();
							size.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(nd.path, size);
							
							int sw = (size.outWidth + nd.width - 1) / nd.width;
							int sh = (size.outHeight + nd.height - 1) / nd.height;
							
							Options op = new Options();
							op.inSampleSize = Math.min(sw, sh);
							op.inScaled = true;
							
							Bitmap bmp = BitmapFactory.decodeFile(nd.path, op);
							int bw = bmp.getWidth();
							int bh = bmp.getHeight();
							int rw = Math.min(bw, nd.width);
							int rh = Math.min(bh, nd.height);
							int x = (bw - rw) / 2;
							int y = (bh - rh) / 2;
							
							int[] pixels = new int[rw * rh];
							bmp.getPixels(pixels, 0, rw, x, y, rw, rh);
							
							Config config = bmp.getConfig();
							if (config == null) {
								config = Config.ARGB_8888;
							}
							nd.bmp = Bitmap.createBitmap(pixels, rw, rh, config);
							
							synchronized (THUM_CACHE) {
								node.bmp = nd.bmp;
							}
						} catch (Exception e) {
							Logger.print(null, e);
							
							nd.bmp = BitmapFactory.decodeResource(AppUtil.getRes(), R.drawable.ic_icon);
							synchronized (THUM_CACHE) {
								node.bmp = nd.bmp;
							}
						}
						
						if (nd.listenner != null) {
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									nd.listenner.onImageGot(nd.bmp);
								}
							});
						}
					}
				} catch (Exception e) {
					Logger.print(null, e);
				} finally {
					synchronized (THUM_CACHE) {
						sIsThumRunning = false;
					}
				}
			}
		});
	}

	public static void getImage(final String path, final IImageListenner listenner) {
		AppUtil.runOnNewThread(new Runnable() {
			public void run() {
				synchronized (IMAGE_CACHE) {
					DrawableNode node = IMAGE_CACHE.get(path);
					
					if (node != null && node.bmp != null) {
						if (listenner != null) {
							final Bitmap bmp = node.bmp;
							
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									listenner.onImageGot(bmp);
								}
							});
						}
						
						return;
					}
					
					if (node == null) {
						node = new DrawableNode();
						IMAGE_CACHE.put(path, node);
					}
					
					node.path = path;
					node.token = SystemClock.elapsedRealtime();
					node.listenner = listenner;
					
					if (sIsImageRunning) {
						return;
					}
					sIsImageRunning = true;
				}
				
				try {
					while (true) {
						DrawableNode node = null;
						
						synchronized (IMAGE_CACHE) {
							for (DrawableNode n : IMAGE_CACHE.values()) {
								if (n.bmp == null) {
									if (node == null || n.token > node.token) {
										node = n;
									}
								}
							}
						}
						
						if (node == null) {
							return;
						}
						
						final DrawableNode nd;
						synchronized (IMAGE_CACHE) {
							nd = node.clone();
						}
						
						try {
							Options size = new Options();
							size.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(nd.path, size);
							
							Options op = new Options();
							op.inSampleSize = (int) Math.ceil(Math.sqrt((size.outWidth * size.outHeight
								+ 1048576 - 1) / 1048576));
							
							nd.bmp = BitmapFactory.decodeFile(nd.path, op);
							synchronized (IMAGE_CACHE) {
								node.bmp = nd.bmp;
							}
						} catch (Exception e) {
							Logger.print(null, e);
							
							nd.bmp = BitmapFactory.decodeResource(AppUtil.getRes(), R.drawable.ic_icon);
							synchronized (IMAGE_CACHE) {
								node.bmp = nd.bmp;
							}
						}
						
						if (nd.listenner != null) {
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									nd.listenner.onImageGot(nd.bmp);
								}
							});
						}
					}
				} catch (Exception e) {
					Logger.print(null, e);
				} finally {
					synchronized (IMAGE_CACHE) {
						sIsImageRunning = false;
					}
				}
			}
		});
	}
}

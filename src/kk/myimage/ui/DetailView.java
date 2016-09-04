package kk.myimage.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import kk.myimage.util.MathUtil;
import kk.myimage.util.Setting;

public class DetailView extends View {
	private Bitmap mBitmap;
	private final Matrix mMatrix = new Matrix();
	private boolean mNeedReset = false;

	private enum Mode {
		None, Single, Double,
	}

	private Mode mMode = Mode.None;

	private float mDistance;
	private float mRotation;

	private float mDownX;
	private float mDownY;
	private float mBmpRotation = 0;

	public DetailView(Context context) {
		super(context);
	}

	public DetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DetailView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setBitmap(Bitmap bmp) {
		mBitmap = bmp;
		mNeedReset = true;
		invalidate();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: {
			mMode = Mode.Single;

			mDownX = event.getX();
			mDownY = event.getY();
			break;
		}

		case MotionEvent.ACTION_POINTER_DOWN: {
			mMode = Mode.Double;

			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);

			mDistance = MathUtil.distance(dx, dy);
			mRotation = (float) Math.toDegrees(MathUtil.getRotation(dx, dy));

			mDownX = -1000;
			mDownY = -1000;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if (mMode == Mode.Single) {
				if (event.getHistorySize() > 0) {
					float x0 = event.getHistoricalX(0);
					float y0 = event.getHistoricalY(0);
					float x1 = event.getX();
					float y1 = event.getY();
					mMatrix.postTranslate(x1 - x0, y1 - y0);

					invalidate();
				}
			} else if (mMode == Mode.Double) {
				float dx = event.getX(1) - event.getX(0);
				float dy = event.getY(1) - event.getY(0);
				float cx = getWidth() / 2;
				float cy = getHeight() / 2;

				float distance = MathUtil.distance(dx, dy);
				float scale = distance / mDistance;
				mMatrix.postScale(scale, scale, cx, cy);
				mDistance = distance;

				float rotation = (float) Math.toDegrees(MathUtil.getRotation(
						dx, dy));
				mBmpRotation += rotation - mRotation;
				mMatrix.postRotate(rotation - mRotation, cx, cy);
				mRotation = rotation;

				invalidate();
			}
			break;
		}

		case MotionEvent.ACTION_UP: {
			mMode = Mode.None;

			if (Setting.getLockTransform()) {
				adjustMatrix();
			}
			
			invalidate();

			if (MathUtil.distance(event.getX(), event.getY(), mDownX, mDownY) > 4) {
				return true;
			}

			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			mMode = Mode.Single;
			break;
		}
		}

		super.onTouchEvent(event);

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mBitmap == null) {
			return;
		}

		if (mNeedReset) {
			mNeedReset = false;

			int iw = mBitmap.getWidth();
			int ih = mBitmap.getHeight();

			float scaleX = getWidth() / (float) iw;
			float scaleY = getHeight() / (float) ih;
			float scale = Math.min(scaleX, scaleY);

			mMatrix.reset();
			mMatrix.postScale(scale, scale);
			mMatrix.postTranslate((getWidth() - iw * scale) / 2,
					(getHeight() - ih * scale) / 2);
		}

		canvas.save();
		canvas.drawBitmap(mBitmap, mMatrix, null);
		canvas.restore();
	}

	private void adjustMatrix() {
		if (mBitmap == null) {
			return;
		}

		float wd = mBitmap.getWidth();
		float ht = mBitmap.getHeight();
		float width = getWidth();
		float height = getHeight();
		float cx = width / 2;
		float cy = height / 2;

		float rotation = Math.round(mBmpRotation / 90) * 90;
		mMatrix.postRotate(rotation - mBmpRotation, cx, cy);
		mBmpRotation = rotation;

		float[] values = new float[9];
		mMatrix.getValues(values);

		PointF p0 = MathUtil.getPoint(0, 0, values);
		PointF p1 = MathUtil.getPoint(0, ht, values);
		PointF p2 = MathUtil.getPoint(wd, 0, values);
		PointF p3 = MathUtil.getPoint(wd, ht, values);

		float minX = MathUtil.min(p0.x, p1.x, p2.x, p3.x);
		float maxX = MathUtil.max(p0.x, p1.x, p2.x, p3.x);
		float minY = MathUtil.min(p0.y, p1.y, p2.y, p3.y);
		float maxY = MathUtil.max(p0.y, p1.y, p2.y, p3.y);

		if (maxX - minX < width && maxY - minY < height) {
			float scale = MathUtil.min(width / (maxX - minX), height
					/ (maxY - minY));

			minX = (minX - cx) * scale + cx;
			maxX = (maxX - cx) * scale + cx;
			minY = (minY - cy) * scale + cy;
			maxY = (maxY - cy) * scale + cy;

			mMatrix.postScale(scale, scale, cx, cy);
		}

		float dx = 0, dy = 0;

		if (minX > 0 && maxX > width) {
			dx = MathUtil.max(0 - minX, width - maxX);
		} else if (minX < 0 && maxX < width) {
			dx = MathUtil.min(0 - minX, width - maxX);
		}

		if (minY > 0 && maxY > height) {
			dy = MathUtil.max(0 - minY, height - maxY);
		} else if (minY < 0 && maxY < height) {
			dy = MathUtil.min(0 - minY, height - maxY);
		}

		mMatrix.postTranslate(dx, dy);
	}
	
	public Matrix getChange() {
		return mMatrix;
	}
	
	public void setChange(Matrix matrix) {
		mMatrix.set(matrix);
		mNeedReset = false;
		invalidate();
	}
}

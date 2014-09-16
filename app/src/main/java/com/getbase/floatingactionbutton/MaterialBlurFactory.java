package com.getbase.floatingactionbutton;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.TypedValue;

import hugo.weaving.DebugLog;

public class MaterialBlurFactory {

  @DebugLog
  public Drawable getBackground(Context context) {
    RenderScript rs = getRenderScript(context);

    final float topShadowOpacity = 0.16f;
    final float topShadowOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.getResources().getDisplayMetrics());
    final float topShadowSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.getResources().getDisplayMetrics()) * 3;

    final float bottomShadowOpacity = 0.23f;
    final float bottomShadowOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.getResources().getDisplayMetrics());
    final float bottomShadowSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.getResources().getDisplayMetrics()) * 3;

    final float strokeSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, context.getResources().getDisplayMetrics());
    final float halfStrokeSize = strokeSize / 2f;

    final float plusStrokeSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.getResources().getDisplayMetrics());
    final float plusHalfStrokeSize = plusStrokeSize / 2f;
    final float plusSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, context.getResources().getDisplayMetrics());
    final float plusHalfSize = plusSize / 2f;

    final float circleSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, context.getResources().getDisplayMetrics());
    final float circleRadius = circleSize / 2f;

    final Bitmap topShadow = getBitmap(rs, topShadowSize, circleSize, topShadowOpacity);
    final Bitmap bottomShadow = getBitmap(rs, bottomShadowSize, circleSize, bottomShadowOpacity);

    final float drawableWidth = Math.max(bottomShadow.getWidth(), topShadow.getWidth());
    final float bottomShadowLeft = (drawableWidth - bottomShadow.getWidth()) / 2f;
    final float topShadowLeft = (drawableWidth - topShadow.getWidth()) / 2f;
    final float circleLeft = (drawableWidth - circleSize) / 2f;

    final float topShadowTop = topShadowOffset - (circleRadius + topShadowSize);
    final float bottomShadowTop = bottomShadowOffset - (circleRadius + bottomShadowSize);
    final float topShadowBottom = topShadowOffset + (circleRadius + topShadowSize);
    final float bottomShadowBottom = bottomShadowOffset + (circleRadius + bottomShadowSize);

    final float zero = Math.min(topShadowTop, bottomShadowTop);
    final float drawableHeight = Math.max(topShadowBottom, bottomShadowBottom) - Math.min(topShadowTop, bottomShadowTop);

    final float topShadowY = topShadowTop - zero;
    final float bottomShadowY = bottomShadowTop - zero;
    final float circleY = topShadowY + topShadowSize - topShadowOffset;

    final RectF circleRect = new RectF(circleLeft, circleY, circleLeft + circleSize, circleY + circleSize);
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(Color.rgb(250, 250, 250));

    final Bitmap leBitmap = Bitmap.createBitmap((int) drawableWidth, (int) drawableHeight, Config.ARGB_8888);
    final Canvas canvas = new Canvas(leBitmap);
    canvas.drawBitmap(bottomShadow, bottomShadowLeft, bottomShadowY, null);
    canvas.drawBitmap(topShadow, topShadowLeft, topShadowY, null);
    canvas.drawOval(circleRect, paint);

    final Paint lePaint = new Paint();
    lePaint.setStyle(Style.FILL);
    lePaint.setColor(Color.rgb(128, 128, 128));
    lePaint.setAntiAlias(true);

    final PointF circleCenter = new PointF(circleLeft + circleRadius, circleY + circleRadius);
    canvas.drawRect(circleCenter.x - plusHalfSize, circleCenter.y - plusHalfStrokeSize, circleCenter.x + plusHalfSize, circleCenter.y + plusHalfStrokeSize, lePaint);
    canvas.drawRect(circleCenter.x - plusHalfStrokeSize, circleCenter.y - plusHalfSize, circleCenter.x + plusHalfStrokeSize, circleCenter.y + plusHalfSize, lePaint);

    paint.setStrokeWidth(strokeSize);
    paint.setStyle(Style.STROKE);

    paint.setColor(Color.BLACK);
    paint.setAlpha((int) (255f * .02f));
    circleRect.set(circleLeft - halfStrokeSize, circleY - halfStrokeSize, circleLeft + circleSize + halfStrokeSize, circleY + circleSize + halfStrokeSize);
    canvas.drawOval(circleRect, paint);

    circleRect.set(circleLeft + halfStrokeSize, circleY + halfStrokeSize, circleLeft + circleSize - halfStrokeSize, circleY + circleSize - halfStrokeSize);

    paint.setShader(new LinearGradient(circleCenter.x, circleRect.top, circleCenter.x, circleRect.bottom,
        new int[] { Color.TRANSPARENT, Color.argb(128, 0, 0, 0), Color.BLACK },
        new float[] { 0f, .8f, 1f },
        TileMode.CLAMP
    ));
    paint.setAlpha((int) (255f * 0.04f));
    canvas.drawOval(circleRect, paint);

    paint.setShader(new LinearGradient(circleCenter.x, circleRect.top, circleCenter.x, circleRect.bottom,
        new int[] { Color.WHITE, Color.argb(128, 255, 255, 255), Color.TRANSPARENT },
        new float[] { 0f, .2f, 1f },
        TileMode.CLAMP
    ));
    paint.setAlpha((int) (255f * .8f));
    canvas.drawOval(circleRect, paint);

    rs.destroy();
    return new BitmapDrawable(context.getResources(), leBitmap);
  }

  @DebugLog
  private RenderScript getRenderScript(Context context) {
    return RenderScript.create(context);
  }

  @DebugLog
  private Bitmap getBitmap(RenderScript rs, float shadowSize, float circleSize, float opacity) {
    shadowSize = Math.min(shadowSize, 25f);
    final int drawable = (int) (circleSize + 2 * shadowSize);
    final Bitmap bitmapOriginal = Bitmap.createBitmap(drawable, drawable, Config.ARGB_8888);

    final RectF circleRect = new RectF(shadowSize, shadowSize, shadowSize + circleSize, shadowSize + circleSize);
    final Paint paint = new Paint();
    paint.setColor(Color.BLACK);
    paint.setAlpha((int) (255.0f * opacity));
    paint.setAntiAlias(true);
    new Canvas(bitmapOriginal).drawOval(circleRect, paint);

    final Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal);
    final Allocation output = Allocation.createTyped(rs, input.getType());
    final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
    script.setRadius(shadowSize);
    script.setInput(input);
    script.forEach(output);
    output.copyTo(bitmapOriginal);

    return bitmapOriginal;
  }
}

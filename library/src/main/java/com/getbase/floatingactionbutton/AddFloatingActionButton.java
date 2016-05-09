package com.getbase.floatingactionbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.util.AttributeSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AddFloatingActionButton extends FloatingActionButton {
  int mPlusColor;

  public AddFloatingActionButton(Context context) {
    this(context, null);
  }

  public AddFloatingActionButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AddFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  void init(Context context, AttributeSet attributeSet) {
	  super.init(context, attributeSet);
    TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.AddFloatingActionButton, 0, 0);
    mPlusColor = attr.getColor(R.styleable.AddFloatingActionButton_fab_plusIconColor, getColor(android.R.color.white));
	  attr.recycle();


  }

  /**
   * @return the current Color of plus icon.
   */
  public int getPlusColor() {
    return mPlusColor;
  }

  public void setPlusColorResId(@ColorRes int plusColor) {
    setPlusColor(getColor(plusColor));
  }

  public void setPlusColor(int color) {
    if (mPlusColor != color) {
      mPlusColor = color;
      updateBackground();
    }
  }

  @Override
  public void setIcon(@DrawableRes int icon) {
    throw new UnsupportedOperationException("Use FloatingActionButton if you want to use custom icon");
  }

	float getPlusSize(){
		switch (mDrawableIconSize){
			case DRAWABLE_SIZE_HUGE:
				return getDimension(R.dimen.fab_plus_icon_size_huge);
			case DRAWABLE_SIZE_LARGE:
				return getDimension(R.dimen.fab_plus_icon_size_large);
			case DRAWABLE_SIZE_NORMAL:
				return getDimension(R.dimen.fab_plus_icon_size);
		}
		return getDimension(R.dimen.fab_plus_icon_size);
	}

	float getStrokeSize(){
		switch (mDrawableIconSize){
			case DRAWABLE_SIZE_HUGE:
				return getDimension(R.dimen.fab_plus_icon_stroke_huge);
			case DRAWABLE_SIZE_LARGE:
				return getDimension(R.dimen.fab_plus_icon_stroke_large);
			case DRAWABLE_SIZE_NORMAL:
				return getDimension(R.dimen.fab_plus_icon_stroke);
		}
		return getDimension(R.dimen.fab_plus_icon_stroke);
	}

  @Override
  Drawable getIconDrawable() {
    final float iconSize = getDrawableDimen();
    final float iconHalfSize = iconSize / 2f;

    final float plusSize = getPlusSize();
    final float plusHalfStroke = getStrokeSize() / 2f;
    final float plusOffset = (iconSize - plusSize) / 2f;

    final Shape shape = new Shape() {
      @Override
      public void draw(Canvas canvas, Paint paint) {
        canvas.drawRect(plusOffset, iconHalfSize - plusHalfStroke, iconSize - plusOffset, iconHalfSize + plusHalfStroke, paint);
        canvas.drawRect(iconHalfSize - plusHalfStroke, plusOffset, iconHalfSize + plusHalfStroke, iconSize - plusOffset, paint);
      }
    };

    ShapeDrawable drawable = new ShapeDrawable(shape);

    final Paint paint = drawable.getPaint();
    paint.setColor(mPlusColor);
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);

    return drawable;
  }
}

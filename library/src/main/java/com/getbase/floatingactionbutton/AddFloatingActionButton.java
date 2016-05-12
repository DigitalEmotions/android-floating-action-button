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
	int mPlusSize;

	float mPlusSizeFloat;
	float mStrokeSize;

	public static final int PLUS_SIZE_NORMAL = 0;
	public static final int PLUS_SIZE_MINI = 1;
	public static final int PLUS_SIZE_LARGE = 2;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ PLUS_SIZE_NORMAL, PLUS_SIZE_MINI, PLUS_SIZE_LARGE })
	public @interface PLUS_SIZE {
	}


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
	  mPlusSize = attr.getInt(R.styleable.AddFloatingActionButton_fab_plusIconSize, -1);
	  attr.recycle();

	  if(mPlusSize == -1){
		  setPlusSizeBasedOnDrawable();
	  }else{
		  setPlusSize();
	  }
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

	public void setPlusSizeBasedOnDrawable(){
		switch (mDrawableIconSize){
			case DRAWABLE_SIZE_HUGE:
				mPlusSize = PLUS_SIZE_LARGE;
			case DRAWABLE_SIZE_LARGE:
				mPlusSize = PLUS_SIZE_NORMAL;
			case DRAWABLE_SIZE_NORMAL:
				mPlusSize = PLUS_SIZE_MINI;
		}
		setPlusSize();
	}

	public void setPlusSize(){
		mPlusSizeFloat = getPlusSize();
		mStrokeSize = getStrokeSize();
	}


	public void setPlusSize(float size, float stroke){
		mPlusSizeFloat = size;
		mStrokeSize = stroke;
	}

  @Override
  public void setIcon(@DrawableRes int icon) {
    throw new UnsupportedOperationException("Use FloatingActionButton if you want to use custom icon");
  }

	float getPlusSize(){
		switch (mPlusSize){
			case PLUS_SIZE_LARGE:
				return getDimension(R.dimen.fab_plus_icon_size_huge);
			case PLUS_SIZE_NORMAL:
				return getDimension(R.dimen.fab_plus_icon_size_large);
			case PLUS_SIZE_MINI:
				return getDimension(R.dimen.fab_plus_icon_size);
		}
		return getDimension(R.dimen.fab_plus_icon_size);
	}

	float getStrokeSize(){
		switch (mPlusSize){
			case PLUS_SIZE_LARGE:
				return getDimension(R.dimen.fab_plus_icon_stroke_huge);
			case PLUS_SIZE_NORMAL:
				return getDimension(R.dimen.fab_plus_icon_stroke_large);
			case PLUS_SIZE_MINI:
				return getDimension(R.dimen.fab_plus_icon_stroke);
		}
		return getDimension(R.dimen.fab_plus_icon_stroke);
	}

  @Override
  Drawable getIconDrawable() {
    final float iconSize = getDrawableDimen();
    final float iconHalfSize = iconSize / 2f;

    final float plusSize = mPlusSizeFloat;
    final float plusHalfStroke = mStrokeSize / 2f;
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

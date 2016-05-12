package com.getbase.floatingactionbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

public class FloatingActionsMenu extends ViewGroup {
  public static final int EXPAND_UP = 0;
  public static final int EXPAND_DOWN = 1;
  public static final int EXPAND_LEFT = 2;
  public static final int EXPAND_RIGHT = 3;

  public static final int LABELS_ON_LEFT_SIDE = 0;
  public static final int LABELS_ON_RIGHT_SIDE = 1;

  protected int ANIMATION_DURATION_EXPAND = 300;
	protected int ANIMATION_DURATION_COLLAPSE = 300;
  protected static final float COLLAPSED_PLUS_ROTATION = 0f;
  protected static final float EXPANDED_PLUS_ROTATION = 90f + 45f;
	
	protected int mAddButtonPlusColor;
	protected int mAddButtonColorNormal;
  	protected int mAddButtonColorPressed;
	protected int mAddButtonSize;
	protected int mAddButtonIconSize;
  protected boolean mAddButtonStrokeVisible;
  protected int mExpandDirection;
	protected int mExpandedColorNormal;
	protected int mExpandedColorPressed;
	protected int mCollapsedIcon;

	protected float mMenuExpandScale = 1.0f;

  protected int mButtonSpacing;
  protected int mLabelsMargin;
  protected int mLabelsVerticalOffset;

  protected boolean mExpanded;

  protected AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION_EXPAND);
  protected AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION_COLLAPSE);


  protected AddFloatingActionButton mAddButton;
  protected LayerDrawable mRotatingDrawable;
  protected int mMaxButtonWidth;
  protected int mMaxButtonHeight;
  protected int mLabelsStyle;
  protected int mLabelsPosition;
  protected int mButtonsCount;

  protected TouchDelegateGroup mTouchDelegateGroup;

  protected OnFloatingActionsMenuUpdateListener mListener;

  public interface OnFloatingActionsMenuUpdateListener {
    void onMenuExpanded();
    void onMenuCollapsed();
  }

  public FloatingActionsMenu(Context context) {
    this(context, null);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  protected void init(Context context, AttributeSet attributeSet) {
    mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources().getDimension(R.dimen.fab_shadow_offset));
    mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
    mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);

    mTouchDelegateGroup = new TouchDelegateGroup(this);
    setTouchDelegate(mTouchDelegateGroup);

    TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
    mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white));
    mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
    mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
    mAddButtonSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize, FloatingActionButton.SIZE_NORMAL);
    mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
    mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
    mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
    mLabelsPosition = attr.getInt(R.styleable.FloatingActionsMenu_fab_labelsPosition, LABELS_ON_LEFT_SIDE);
	mExpandedColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormalExpanded, mAddButtonColorNormal);
	  mExpandedColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressedExpanded, mAddButtonColorPressed);
	  mCollapsedIcon = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_collapsedIcon, 0);
	  mAddButtonIconSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_icon_size, FloatingActionButton.DRAWABLE_SIZE_NORMAL);
	  ANIMATION_DURATION_EXPAND = attr.getInt(R.styleable.FloatingActionsMenu_fab_animationDurationExpand, 300);
	  ANIMATION_DURATION_COLLAPSE = attr.getInt(R.styleable.FloatingActionsMenu_fab_animationDurationCollapse, 300);
	  mMenuExpandScale = attr.getFloat(R.styleable.FloatingActionsMenu_fab_addButtonScaleOnMenuExpand, 1.0f);
	  mLabelsMargin = attr.getDimensionPixelSize(R.styleable.FloatingActionsMenu_fab_label_margin, mLabelsMargin);
	  attr.recycle();

    if (mLabelsStyle != 0 && expandsHorizontally()) {
      throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
    }

    createAddButton(context);
  }

  public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
    mListener = listener;
  }

	public void setCollapsedIcon(@DrawableRes int icon) {
		this.mCollapsedIcon = icon;
	}

  protected boolean expandsHorizontally() {
    return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
  }

  protected static class RotatingDrawable extends LayerDrawable {
    public RotatingDrawable(Drawable drawable) {
      super(new Drawable[] { drawable });
    }

    protected float mRotation;

    @SuppressWarnings("UnusedDeclaration")
    public float getRotation() {
      return mRotation;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRotation(float rotation) {
      mRotation = rotation;
      invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.save();
      canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
      super.draw(canvas);
      canvas.restore();
    }
  }

  protected void createAddButton(Context context) {
    mAddButton = new AddFloatingActionButton(context) {



      @Override
      void updateBackground() {
        mPlusColor = mAddButtonPlusColor;
        mColorNormal = mAddButtonColorNormal;
        mColorPressed = mAddButtonColorPressed;
        mStrokeVisible = mAddButtonStrokeVisible;
        super.updateBackground();
      }


		@Override
		protected StateListDrawable createFillDrawable(float strokeWidth) {
			StateListDrawable drawable = new StateListDrawable();
			drawable.addState(new int[] { -android.R.attr.state_enabled }, createCircleDrawable(mColorDisabled, strokeWidth));
			drawable.addState(new int[]{R.attr.state_menu_expanded, android.R.attr.state_pressed}, createCircleDrawable(mExpandedColorPressed, strokeWidth));
			drawable.addState(new int[] { android.R.attr.state_pressed, -R.attr.state_menu_expanded }, createCircleDrawable(mColorPressed, strokeWidth));
			drawable.addState(new int[] {R.attr.state_menu_expanded}, createCircleDrawable(mExpandedColorNormal, strokeWidth));
			drawable.addState(new int[] { }, createCircleDrawable(mColorNormal, strokeWidth));

			return drawable;
		}

		Drawable getIconDrawable() {
			FloatingActionsMenu.RotatingDrawable expandDrawable = new FloatingActionsMenu.RotatingDrawable(super.getIconDrawable());
			Drawable[] layers;
			if(FloatingActionsMenu.this.mCollapsedIcon != 0) {
				FloatingActionsMenu.RotatingDrawable layeredDrawable = new FloatingActionsMenu.RotatingDrawable(this.getResources().getDrawable(FloatingActionsMenu.this.mCollapsedIcon));
				layers = new Drawable[]{layeredDrawable, expandDrawable};
			} else {
				layers = new Drawable[]{expandDrawable};
			}

			LayerDrawable layeredDrawable1 = new LayerDrawable(layers);
			FloatingActionsMenu.this.mRotatingDrawable = layeredDrawable1;
			OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
			ObjectAnimator collapseCollapsedDrawableAnimator = ObjectAnimator.ofFloat(layeredDrawable1.getDrawable(0), "rotation", new float[]{135.0F, 0.0F});
			ObjectAnimator expandCollapsedDrawableAnimator = ObjectAnimator.ofFloat(layeredDrawable1.getDrawable(0), "rotation", new float[]{0.0F, 135.0F});
			collapseCollapsedDrawableAnimator.setInterpolator(overshootInterpolator);
			expandCollapsedDrawableAnimator.setInterpolator(overshootInterpolator);
			FloatingActionsMenu.this.mExpandAnimation.play(expandCollapsedDrawableAnimator);
			FloatingActionsMenu.this.mCollapseAnimation.play(collapseCollapsedDrawableAnimator);

			if(layeredDrawable1.getNumberOfLayers() > 1) {
				ObjectAnimator collapseExpandedDrawableAnimator = ObjectAnimator.ofFloat(layeredDrawable1.getDrawable(1), "rotation", new float[]{135.0F, 0.0F});
				ObjectAnimator expandExpandedDrawableAnimator = ObjectAnimator.ofFloat(layeredDrawable1.getDrawable(1), "rotation", new float[]{0.0F, 135.0F});
				collapseExpandedDrawableAnimator.setInterpolator(overshootInterpolator);
				expandExpandedDrawableAnimator.setInterpolator(overshootInterpolator);
				FloatingActionsMenu.this.mExpandAnimation.play(expandExpandedDrawableAnimator);
				FloatingActionsMenu.this.mCollapseAnimation.play(collapseExpandedDrawableAnimator);
				layeredDrawable1.getDrawable(1).setAlpha(0);
				ObjectAnimator fadeInCollapsedDrawableAnimator = ObjectAnimator.ofInt(layeredDrawable1.getDrawable(0), View.ALPHA.getName(), new int[]{0, 255, 255, 255});
				ObjectAnimator fadeOutCollapsedDrawableAnimator = ObjectAnimator.ofInt(layeredDrawable1.getDrawable(0), View.ALPHA.getName(), new int[]{255, 0, 0, 0});
				ObjectAnimator fadeInExpandedDrawableAnimator = ObjectAnimator.ofInt(layeredDrawable1.getDrawable(1), View.ALPHA.getName(), new int[]{0, 255, 255, 255});
				ObjectAnimator fadeOutExpandedDrawableAnimator = ObjectAnimator.ofInt(layeredDrawable1.getDrawable(1), View.ALPHA.getName(), new int[]{255, 0, 0, 0});
				LinearInterpolator accelerateInterpolator = new LinearInterpolator();
				fadeInCollapsedDrawableAnimator.setInterpolator(accelerateInterpolator);
				fadeOutCollapsedDrawableAnimator.setInterpolator(accelerateInterpolator);
				fadeInExpandedDrawableAnimator.setInterpolator(accelerateInterpolator);
				fadeOutExpandedDrawableAnimator.setInterpolator(accelerateInterpolator);
				FloatingActionsMenu.this.mExpandAnimation.play(fadeOutCollapsedDrawableAnimator).with(fadeInExpandedDrawableAnimator);
				FloatingActionsMenu.this.mCollapseAnimation.play(fadeInCollapsedDrawableAnimator).with(fadeOutExpandedDrawableAnimator);
			}

			return layeredDrawable1;
		}
	};

    mAddButton.setId(R.id.fab_expand_menu_button);
    mAddButton.setSize(mAddButtonSize);
	  mAddButton.setIconSize(mAddButtonIconSize);
    mAddButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toggle();
      }
    });

    addView(mAddButton, super.generateDefaultLayoutParams());
    mButtonsCount++;
  }

  public void addButton(FloatingActionButton button) {
    addView(button, mButtonsCount - 1);
    mButtonsCount++;

    if (mLabelsStyle != 0) {
      createLabels();
    }
  }

  public void removeButton(FloatingActionButton button) {
    removeView(button.getLabelView());
    removeView(button);
    button.setTag(R.id.fab_label, null);
    mButtonsCount--;
  }

  protected int getColor(@ColorRes int id) {
    return getResources().getColor(id);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildren(widthMeasureSpec, heightMeasureSpec);

    int width = 0;
    int height = 0;

    mMaxButtonWidth = 0;
    mMaxButtonHeight = 0;
    int maxLabelWidth = 0;

    for (int i = 0; i < mButtonsCount; i++) {
      View child = getChildAt(i);

      if (child.getVisibility() == GONE) {
        continue;
      }

      switch (mExpandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
        height += child.getMeasuredHeight();
        break;
      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        width += child.getMeasuredWidth();
        mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
        break;
      }

      if (!expandsHorizontally()) {
        TextView label = (TextView) child.getTag(R.id.fab_label);
        if (label != null) {
          maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
        }
      }
    }

    if (!expandsHorizontally()) {
      width = mMaxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + mLabelsMargin : 0);
    } else {
      height = mMaxButtonHeight;
    }

    switch (mExpandDirection) {
    case EXPAND_UP:
    case EXPAND_DOWN:
      height += mButtonSpacing * (mButtonsCount - 1);
      height = adjustForOvershoot(height);
      break;
    case EXPAND_LEFT:
    case EXPAND_RIGHT:
      width += mButtonSpacing * (mButtonsCount - 1);
      width = adjustForOvershoot(width);
      break;
    }

    setMeasuredDimension(width, height);
  }

  protected int adjustForOvershoot(int dimension) {
    return dimension * 12 / 10;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    switch (mExpandDirection) {
    case EXPAND_UP:
    case EXPAND_DOWN:
      boolean expandUp = mExpandDirection == EXPAND_UP;

      if (changed) {
        mTouchDelegateGroup.clearTouchDelegates();
      }

      int addButtonY = expandUp ? b - t - mAddButton.getMeasuredHeight() : 0;
      // Ensure mAddButton is centered on the line where the buttons should be
      int buttonsHorizontalCenter = mLabelsPosition == LABELS_ON_LEFT_SIDE
          ? r - l - mMaxButtonWidth / 2
          : mMaxButtonWidth / 2;
      int addButtonLeft = buttonsHorizontalCenter - mAddButton.getMeasuredWidth() / 2;
      mAddButton.layout(addButtonLeft, addButtonY, addButtonLeft + mAddButton.getMeasuredWidth(), addButtonY + mAddButton.getMeasuredHeight());

      int labelsOffset = mMaxButtonWidth / 2 + mLabelsMargin;
      int labelsXNearButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
          ? buttonsHorizontalCenter - labelsOffset
          : buttonsHorizontalCenter + labelsOffset;

      int nextY = expandUp ?
          addButtonY - mButtonSpacing :
          addButtonY + mAddButton.getMeasuredHeight() + mButtonSpacing;

      for (int i = mButtonsCount - 1; i >= 0; i--) {
        final View child = getChildAt(i);

        if (child == mAddButton || child.getVisibility() == GONE) continue;

        int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
        int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
        child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

        float collapsedTranslation = addButtonY - childY;
        float expandedTranslation = 0f;

        child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
        child.setAlpha(mExpanded ? 1f : 0f);

        LayoutParams params = (LayoutParams) child.getLayoutParams();
        params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
        params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
        params.setAnimationsTarget(child);

        View label = (View) child.getTag(R.id.fab_label);
        if (label != null) {
          int labelXAwayFromButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
              ? labelsXNearButton - label.getMeasuredWidth()
              : labelsXNearButton + label.getMeasuredWidth();

          int labelLeft = mLabelsPosition == LABELS_ON_LEFT_SIDE
              ? labelXAwayFromButton
              : labelsXNearButton;

          int labelRight = mLabelsPosition == LABELS_ON_LEFT_SIDE
              ? labelsXNearButton
              : labelXAwayFromButton;

          int labelTop = childY - mLabelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

          label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

          Rect touchArea = new Rect(
              Math.min(childX, labelLeft),
              childY - mButtonSpacing / 2,
              Math.max(childX + child.getMeasuredWidth(), labelRight),
              childY + child.getMeasuredHeight() + mButtonSpacing / 2);
          mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(touchArea, child));

          label.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
          label.setAlpha(mExpanded ? 1f : 0f);

          LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
          labelParams.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
          labelParams.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
          labelParams.setAnimationsTarget(label);
        }

        nextY = expandUp ?
            childY - mButtonSpacing :
            childY + child.getMeasuredHeight() + mButtonSpacing;
      }
      break;

    case EXPAND_LEFT:
    case EXPAND_RIGHT:
      boolean expandLeft = mExpandDirection == EXPAND_LEFT;

      int addButtonX = expandLeft ? r - l - mAddButton.getMeasuredWidth() : 0;
      // Ensure mAddButton is centered on the line where the buttons should be
      int addButtonTop = b - t - mMaxButtonHeight + (mMaxButtonHeight - mAddButton.getMeasuredHeight()) / 2;
      mAddButton.layout(addButtonX, addButtonTop, addButtonX + mAddButton.getMeasuredWidth(), addButtonTop + mAddButton.getMeasuredHeight());

      int nextX = expandLeft ?
          addButtonX - mButtonSpacing :
          addButtonX + mAddButton.getMeasuredWidth() + mButtonSpacing;

      for (int i = mButtonsCount - 1; i >= 0; i--) {
        final View child = getChildAt(i);

        if (child == mAddButton || child.getVisibility() == GONE) continue;

        int childX = expandLeft ? nextX - child.getMeasuredWidth() : nextX;
        int childY = addButtonTop + (mAddButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
        child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

        float collapsedTranslation = addButtonX - childX;
        float expandedTranslation = 0f;

        child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
        child.setAlpha(mExpanded ? 1f : 0f);

        LayoutParams params = (LayoutParams) child.getLayoutParams();
        params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
        params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
        params.setAnimationsTarget(child);

        nextX = expandLeft ?
            childX - mButtonSpacing :
            childX + child.getMeasuredWidth() + mButtonSpacing;
      }

      break;
    }
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(super.generateDefaultLayoutParams());
  }

  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(super.generateLayoutParams(attrs));
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(super.generateLayoutParams(p));
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return super.checkLayoutParams(p);
  }

  protected static Interpolator sExpandInterpolator = new OvershootInterpolator();
  protected static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
  protected static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

  protected class LayoutParams extends ViewGroup.LayoutParams {

    protected ObjectAnimator mExpandDir = new ObjectAnimator();
    protected ObjectAnimator mExpandAlpha = new ObjectAnimator();
    protected ObjectAnimator mCollapseDir = new ObjectAnimator();
    protected ObjectAnimator mCollapseAlpha = new ObjectAnimator();

	  protected ObjectAnimator mExpandX = new ObjectAnimator();
	  protected ObjectAnimator mExpandY = new ObjectAnimator();
	  protected ObjectAnimator mCollapseX = new ObjectAnimator();
	  protected ObjectAnimator mCollapseY = new ObjectAnimator();

    protected boolean animationsSetToPlay;

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);

      mExpandDir.setInterpolator(sExpandInterpolator);
      mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
      mCollapseDir.setInterpolator(sCollapseInterpolator);
      mCollapseAlpha.setInterpolator(sCollapseInterpolator);

      mCollapseAlpha.setProperty(View.ALPHA);
      mCollapseAlpha.setFloatValues(1f, 0f);

      mExpandAlpha.setProperty(View.ALPHA);
      mExpandAlpha.setFloatValues(0f, 1f);

		mExpandX.setProperty(View.SCALE_X);
		mExpandY.setProperty(View.SCALE_Y);
		mExpandX.setFloatValues(mMenuExpandScale);
		mExpandY.setFloatValues(mMenuExpandScale);

		mCollapseX.setProperty(View.SCALE_X);
		mCollapseY.setProperty(View.SCALE_Y);
		mCollapseX.setFloatValues(1f);
		mCollapseY.setFloatValues(1f);


      switch (mExpandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        mCollapseDir.setProperty(View.TRANSLATION_Y);
        mExpandDir.setProperty(View.TRANSLATION_Y);
        break;
      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        mCollapseDir.setProperty(View.TRANSLATION_X);
        mExpandDir.setProperty(View.TRANSLATION_X);
        break;
      }
    }

    public void setAnimationsTarget(View view) {
      mCollapseAlpha.setTarget(view);
      mCollapseDir.setTarget(view);
      mExpandAlpha.setTarget(view);
      mExpandDir.setTarget(view);
		mExpandX.setTarget(mAddButton);
		mExpandY.setTarget(mAddButton);
		mCollapseX.setTarget(mAddButton);
		mCollapseY.setTarget(mAddButton);

      // Now that the animations have targets, set them to be played
      if (!animationsSetToPlay) {
        addLayerTypeListener(mExpandDir, view);
        addLayerTypeListener(mCollapseDir, view);

        mCollapseAnimation.play(mCollapseAlpha);
        mCollapseAnimation.play(mCollapseDir);
        mCollapseAnimation.play(mCollapseX);
        mCollapseAnimation.play(mCollapseY);
        mExpandAnimation.play(mExpandAlpha);
        mExpandAnimation.play(mExpandDir);
		  mExpandAnimation.play(mExpandX);
		  mExpandAnimation.play(mExpandY);

        animationsSetToPlay = true;
      }
    }

    protected void addLayerTypeListener(Animator animator, final View view) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          view.setLayerType(LAYER_TYPE_NONE, null);
        }

        @Override
        public void onAnimationStart(Animator animation) {
          view.setLayerType(LAYER_TYPE_HARDWARE, null);
        }
      });
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    bringChildToFront(mAddButton);
    mButtonsCount = getChildCount();

    if (mLabelsStyle != 0) {
      createLabels();
    }
  }

  protected void createLabels() {
    Context context = new ContextThemeWrapper(getContext(), mLabelsStyle);

    for (int i = 0; i < mButtonsCount; i++) {
      FloatingActionButton button = (FloatingActionButton) getChildAt(i);
      String title = button.getTitle();

      if (button == mAddButton || title == null ||
          button.getTag(R.id.fab_label) != null) continue;

      TextView label = new TextView(context);
      label.setTextAppearance(getContext(), mLabelsStyle);
      label.setText(button.getTitle());
      addView(label);

      button.setTag(R.id.fab_label, label);
    }
  }

  public void collapse() {
    collapse(false);
  }

  public void collapseImmediately() {
    collapse(true);
  }

  protected void collapse(boolean immediately) {
    if (mExpanded) {
      mExpanded = false;
      mTouchDelegateGroup.setEnabled(false);
      mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION_COLLAPSE);
      mCollapseAnimation.start();
		mAddButton.setExpanded(false);
      mExpandAnimation.cancel();

      if (mListener != null) {
        mListener.onMenuCollapsed();

      }
    }
  }

  public void toggle() {
    if (mExpanded) {
      collapse();
    } else {
      expand();
    }
  }

  public void expand() {
    if (!mExpanded) {
      mExpanded = true;
      mTouchDelegateGroup.setEnabled(true);
      mCollapseAnimation.cancel();
		mExpandAnimation.setDuration(ANIMATION_DURATION_EXPAND);
      mExpandAnimation.start();
		mAddButton.setExpanded(true);

      if (mListener != null) {
        mListener.onMenuExpanded();

      }
    }
  }

  public boolean isExpanded() {
    return mExpanded;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    mAddButton.setEnabled(enabled);
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.mExpanded = mExpanded;

    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      SavedState savedState = (SavedState) state;
      mExpanded = savedState.mExpanded;
      mTouchDelegateGroup.setEnabled(mExpanded);

      if (mRotatingDrawable != null) {
		  ((FloatingActionsMenu.RotatingDrawable)this.mRotatingDrawable.getDrawable(0)).setRotation(this.mExpanded?135.0F:0.0F);
      }

      super.onRestoreInstanceState(savedState.getSuperState());
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  public static class SavedState extends BaseSavedState {
    public boolean mExpanded;

    public SavedState(Parcelable parcel) {
      super(parcel);
    }

    protected SavedState(Parcel in) {
      super(in);
      mExpanded = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(mExpanded ? 1 : 0);
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }
}

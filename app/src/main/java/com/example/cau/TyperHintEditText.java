package com.example.cau;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Random;

@SuppressLint("AppCompatCustomView")
public class TyperHintEditText extends androidx.appcompat.widget.AppCompatEditText implements TextWatcher {

    // clear text
    @DrawableRes
    private static final int DEFAULT_CLEAR_ICON_RES_ID = R.drawable.baseline_close_24;
    protected Drawable mClearIconDrawable;
    public boolean mIsClearIconShown = false;
    public boolean mClearIconDrawWhenFocused = true;
    public OnTextClearedListener textClearedListener;

    // typer
    private final Random random = new Random();
    private CharSequence mText;
    private final Handler handler = new Handler(Looper.myLooper());
    private int charIncrease = 1;
    private int typerSpeed = 50;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int currentLength = getHint().length();
            if (currentLength < mText.length()) {
                if (currentLength + charIncrease > mText.length()) {
                    charIncrease = mText.length() - currentLength;
                }
                if (currentLength + charIncrease >= mText.length())
                    setHint(mText.subSequence(0, currentLength + charIncrease));
                else
                    setHint(mText.subSequence(0, currentLength + charIncrease) + "|");
                long randomTime = typerSpeed + random.nextInt(typerSpeed);
                handler.postDelayed(runnable, randomTime);
            }
        }
    };

    public TyperHintEditText(@NonNull Context context) {
        super(context);
    }

    public TyperHintEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public void init(@Nullable AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TyperTextView);
        typerSpeed = typedArray.getInt(R.styleable.TyperTextView_typerSpeed, 100);
        charIncrease = typedArray.getInt(R.styleable.TyperTextView_charIncrease, 1);
        if (typedArray.hasValue(R.styleable.TyperTextView_clearIconDrawable)) {
            mClearIconDrawable = typedArray.getDrawable(R.styleable.TyperTextView_clearIconDrawable);
        } else {
            setIcon(getContext());
        }
        if (mClearIconDrawable != null) {
            mClearIconDrawable.setCallback(this);
        }
        mClearIconDrawWhenFocused = typedArray.getBoolean(R.styleable.TyperTextView_clearIconDrawWhenFocused, true);
        mText = getHint();
        setHint("");
        handler.post(runnable);
        typedArray.recycle();
    }

    public TyperHintEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public int getCharIncrease() {
        return charIncrease;
    }

    public void setCharIncrease(int charIncrease) {
        this.charIncrease = charIncrease;
    }

    public int getTyperSpeed() {
        return typerSpeed;
    }

    public void setTyperSpeed(int typerSpeed) {
        this.typerSpeed = typerSpeed;
    }

    public void animateText(CharSequence text) {
        if (text == null) return;
        mText = text;
        setHint("");
        handler.post(runnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }


    @Override
    public void afterTextChanged(Editable s) {
    }

    public interface OnTextClearedListener {
        void onTextCleared();
    }


    protected void setIcon(Context context) {
        mClearIconDrawable = ContextCompat.getDrawable(context, DEFAULT_CLEAR_ICON_RES_ID);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return mIsClearIconShown ? new ClearIconSavedState(superState, true) : superState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ClearIconSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        ClearIconSavedState savedState = (ClearIconSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mIsClearIconShown = savedState.isClearIconShown();
        showClearIcon(mIsClearIconShown);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!mClearIconDrawWhenFocused || hasFocus()) {
            showClearIcon(!TextUtils.isEmpty(s));
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        showClearIcon((!mClearIconDrawWhenFocused || focused) && !TextUtils.isEmpty(getText()));
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClearIconTouched(event)) {
            setText(null);
            event.setAction(MotionEvent.ACTION_CANCEL);
            showClearIcon(false);
            if (textClearedListener != null) textClearedListener.onTextCleared();
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public int getAutofillType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return AUTOFILL_TYPE_NONE;
        } else {
            return super.getAutofillType();
        }
    }

    public boolean isClearIconTouched(MotionEvent event) {
        if (!mIsClearIconShown) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int touchPointX = (int) event.getX();
            final int widthOfView = getWidth();
            Configuration config = getResources().getConfiguration();
            int compoundPadding = 0;
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                compoundPadding = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        ? getCompoundPaddingStart()
                        : getCompoundPaddingLeft();
                return compoundPadding * 2 + mClearIconDrawable.getMinimumWidth() >= touchPointX;
            } else {
                compoundPadding =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                                ? getCompoundPaddingEnd()
                                : getCompoundPaddingRight();
                return touchPointX >= widthOfView - compoundPadding;
            }
        }
        return false;
    }

    public void showClearIcon(boolean show) {
        setIcon(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Drawable[] drawables = getCompoundDrawablesRelative();
            if (show) {
                // show icon on the right
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                        drawables[0], drawables[1], mClearIconDrawable, drawables[3]);
            } else {
                // remove icon
                setCompoundDrawablesRelative(drawables[0], drawables[1], null, drawables[3]);
            }
        } else {
            Drawable[] drawables = getCompoundDrawables();

            if (show) {
                // show icon on the right
                setCompoundDrawablesWithIntrinsicBounds(
                        drawables[0], drawables[1], mClearIconDrawable, drawables[3]);
            } else {
                // remove icon
                setCompoundDrawables(drawables[0], drawables[1], null, drawables[3]);
            }
        }

        mIsClearIconShown = show;
    }

    protected static class ClearIconSavedState extends BaseSavedState {

        public static final Creator<ClearIconSavedState> CREATOR =
                new Creator<ClearIconSavedState>() {
                    @Override
                    public ClearIconSavedState createFromParcel(Parcel source) {
                        return new ClearIconSavedState(source);
                    }

                    @Override
                    public ClearIconSavedState[] newArray(int size) {
                        return new ClearIconSavedState[size];
                    }
                };
        private final boolean mIsClearIconShown;

        private ClearIconSavedState(Parcel source) {
            super(source);
            mIsClearIconShown = source.readByte() != 0;
        }

        ClearIconSavedState(Parcelable superState, boolean isClearIconShown) {
            super(superState);
            mIsClearIconShown = isClearIconShown;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mIsClearIconShown ? 1 : 0));
        }

        boolean isClearIconShown() {
            return mIsClearIconShown;
        }
    }

    @SuppressWarnings("unused")
    public void setOnTextClearedListener(OnTextClearedListener textClearedListener) {
        this.textClearedListener = textClearedListener;
    }
}

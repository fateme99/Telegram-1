package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.provider.FontsContractCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.webrtc.Logging;

public class FontCell extends FrameLayout {
    private TextView textView;
    private TextView textViewDesc;
    private ImageView checkImage;
    private ImageView fontImageView;
    private boolean needDivider;
    private LocaleController.FontInfo currentLocale;
    private boolean isDialog;

    public FontCell(Context context, boolean dialog) {
        super(context);
        if (Theme.dividerPaint == null) {
            Theme.createCommonResources(context);
        }

        setWillNotDraw(false);
        isDialog = dialog;

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(dialog ? Theme.key_dialogTextBlack : Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getGlobalFont()));
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 + 20 : 23 + 20, (isDialog ? 4 + 10 : 7 + 10), LocaleController.isRTL ? 23 + 20 : 23 + 20 + 48, 0));

        textViewDesc = new TextView(context);
        textViewDesc.setTextColor(Theme.getColor(dialog ? Theme.key_dialogTextGray3 : Theme.key_windowBackgroundWhiteGrayText3));
        textViewDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);

        textViewDesc.setLines(1);
        textViewDesc.setMaxLines(1);
        textViewDesc.setSingleLine(true);
        textViewDesc.setEllipsize(TextUtils.TruncateAt.END);
        textViewDesc.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(textViewDesc, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 + 20 : 23 + 20, (isDialog ? 25 + 10 : 29 + 10), LocaleController.isRTL ? 23 + 20 : 23 + 20 + 48, 0));

        checkImage = new ImageView(context);
        checkImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addedIcon), PorterDuff.Mode.MULTIPLY));
        checkImage.setImageResource(R.drawable.sticker_added);
        checkImage.setVisibility(INVISIBLE);
        addView(checkImage, LayoutHelper.createFrame(19, 14, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 23, 0, 23, 0));

        fontImageView = new ImageView(context);
        fontImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addedIcon), PorterDuff.Mode.MULTIPLY));
        addView(fontImageView, LayoutHelper.createFrame(19, 14, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 15, 5, 23, 0));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(isDialog ? 50 : 54) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setFont(LocaleController.FontInfo font) {
        textView.setText(font.name);
        textViewDesc.setText(font.testText);
        Logging.d("TAG", "font location in setFont " + font.pathToFile);
        textViewDesc.setTypeface(AndroidUtilities.getTypeface(font.pathToFile));
        fontImageView.setImageResource(font.imageId);
        checkImage.setImageResource(R.drawable.sticker_added);
        if (SharedConfig.fontType==font.id)
            checkImage.setVisibility(VISIBLE);
        fontImageView.setVisibility(VISIBLE);
    }

    public void setValue(LocaleController.FontInfo font) {
        textView.setText(font.name);
        textViewDesc.setText(font.testText);
        textViewDesc.setTypeface(AndroidUtilities.getTypeface(font.pathToFile));
        fontImageView.setImageResource(font.imageId);
        checkImage.setVisibility(INVISIBLE);
        fontImageView.setVisibility(VISIBLE);
        currentLocale = null;
        needDivider = false;
    }

    public LocaleController.FontInfo getCurrentFont() {
        return currentLocale;
    }

    public void setFontSelected(boolean value) {
        checkImage.setVisibility(value ? VISIBLE : INVISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}

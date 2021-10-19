package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ResponseCell extends FrameLayout {
    private TextView classNameTxtView;
    private TextView responseTime;
    private boolean needDivider;
    private boolean isDialog;

    public ResponseCell(Context context,boolean isDialog){
        super(context);
        this.isDialog =isDialog;

        classNameTxtView =new TextView(context);
        classNameTxtView.setTextColor(Theme.getColor(this.isDialog ? Theme.key_dialogTextBlack : Theme.key_windowBackgroundWhiteBlackText));
        classNameTxtView.setTypeface(AndroidUtilities.getGlobalTypeFace());
        classNameTxtView.setLines(1);
        classNameTxtView.setMaxLines(1);
        classNameTxtView.setSingleLine(true);
        classNameTxtView.setEllipsize(TextUtils.TruncateAt.END);
        classNameTxtView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(classNameTxtView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 + 20 : 23 + 20, (isDialog ? 4 + 10 : 7 + 10), LocaleController.isRTL ? 23 + 20 : 23 + 20 + 48, 0));

        responseTime=new TextView(context);
        responseTime.setTextColor(Theme.getColor(this.isDialog ? Theme.key_dialogTextGray3 : Theme.key_windowBackgroundWhiteGrayText3));
        responseTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        responseTime.setTypeface(AndroidUtilities.getGlobalTypeFace());
        responseTime.setLines(1);
        responseTime.setMaxLines(1);
        responseTime.setSingleLine(true);
        responseTime.setEllipsize(TextUtils.TruncateAt.END);
        responseTime.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(responseTime, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 + 20 : 23 + 20, (isDialog ? 25 + 10 : 29 + 10), LocaleController.isRTL ? 23 + 20 : 23 + 20 + 48, 0));

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(isDialog ? 50 : 54) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setResponse(TLRPC.RequestResponse response , boolean divider) {
        classNameTxtView.setText(response.className);
        responseTime.setText(getDateString(response.timeResponse));
        needDivider=divider;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
    private String getDateString(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }
}

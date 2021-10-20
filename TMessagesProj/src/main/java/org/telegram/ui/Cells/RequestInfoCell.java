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

public class RequestInfoCell extends FrameLayout {
    private TextView classNameTxtView;
    private TextView requestTime;
    private boolean needDivider;

    public RequestInfoCell(Context context) {
        super(context);
        classNameTxtView = new TextView(context);
        classNameTxtView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        classNameTxtView.setTypeface(AndroidUtilities.getGlobalTypeFace());
        classNameTxtView.setLines(1);
        classNameTxtView.setMaxLines(1);
        classNameTxtView.setSingleLine(true);
        classNameTxtView.setEllipsize(TextUtils.TruncateAt.END);
        classNameTxtView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(classNameTxtView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 + 20 : 23 + 20, ( 7 + 10), LocaleController.isRTL ? 23 + 20 : 23 + 20 + 48, 0));

        requestTime = new TextView(context);
        requestTime.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        requestTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        requestTime.setTypeface(AndroidUtilities.getGlobalTypeFace());
        requestTime.setLines(1);
        requestTime.setMaxLines(1);
        requestTime.setSingleLine(true);
        requestTime.setEllipsize(TextUtils.TruncateAt.END);
        requestTime.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(requestTime, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 + 20 : 23 + 20, ( 29 + 10), LocaleController.isRTL ? 23 + 20 : 23 + 20 + 48, 0));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp( 54) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setRequestInfo(TLRPC.RequestInfo requestInfo, boolean divider) {
        classNameTxtView.setText(requestInfo.className);
        requestTime.setText(getDateString(requestInfo.timeResponse));
        needDivider = divider;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    private String getDateString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }
}

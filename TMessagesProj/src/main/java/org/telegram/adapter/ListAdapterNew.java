package org.telegram.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.List;

public class ListAdapterNew extends RecyclerListView.SelectionAdapter {
    public static final int customViewType = 0;
    public static final int shadowViewType = 1;
    private Context mContext;
    private boolean search;
    private List<LocaleController.LocaleInfo>localeList;
    public ListAdapterNew(Context context, boolean isSearch, List<LocaleController.LocaleInfo> list) {
        mContext = context;
        search = isSearch;
        localeList=list;
    }
    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return holder.getItemViewType() == customViewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case customViewType: {
                view = new LanguageCell(mContext, false);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case shadowViewType:
            default: {
                view = new ShadowSectionCell(mContext);
                break;
            }
        }
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case customViewType: {
                LanguageCell textSettingsCell = (LanguageCell) holder.itemView;
                LocaleController.LocaleInfo localeInfo;
                boolean last;
                if (!localeList.isEmpty() && position >= 0 && position < localeList.size()) {
                    localeInfo = localeList.get(position);
                    last = position == localeList.size() - 1;
                } else {
                    if (!localeList.isEmpty()) {
                        position -= localeList.size() + 1;
                    }
                    localeInfo = localeList.get(position);
                    last = position == localeList.size() - 1;
                }
                if (localeInfo.isLocal()) {
                    textSettingsCell.setLanguage(localeInfo, String.format("%1$s (%2$s)", localeInfo.name, LocaleController.getString("LanguageCustom", R.string.LanguageCustom)), !last);
                } else {
                    textSettingsCell.setLanguage(localeInfo, null, !last);
                }
                textSettingsCell.setLanguageSelected(localeInfo == LocaleController.getInstance().getCurrentLocaleInfo());
                break;
            }
            case shadowViewType: {
                ShadowSectionCell sectionCell = (ShadowSectionCell) holder.itemView;
                if (!localeList.isEmpty() && position == localeList.size()) {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                } else {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                }
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return localeList.size();
    }

    @Override
    public int getItemViewType(int i) {
        if (search) {
            return customViewType;
        }
        if (!localeList.isEmpty() && (i == localeList.size() )) {
            return shadowViewType;
        }
        return customViewType;
    }
}

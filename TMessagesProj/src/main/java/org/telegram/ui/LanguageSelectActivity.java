/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.adapter.ListAdapter;
import org.telegram.adapter.ListAdapterNew;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.viewModel.LanguageViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LanguageSelectActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{

    public static final int backMenuItemId = -1;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private ListAdapter searchListViewAdapter;
    private EmptyTextProgressView emptyView;
    private LanguageViewModel languageViewModel;
    private List<LocaleController.LocaleInfo>searchResult;
    private List<LocaleController.LocaleInfo>sortedLanguages;
    private List<LocaleController.LocaleInfo>unofficialLanguages;
    @Override
    public boolean onFragmentCreate() {
        LanguageViewModel.AdapterCallBack callBack= text -> updateSearchView(text);
        searchResult=new ArrayList<>();
        sortedLanguages=new ArrayList<>();
        unofficialLanguages = new ArrayList<>();
        languageViewModel=new LanguageViewModel(sortedLanguages,unofficialLanguages,callBack);
        languageViewModel.loadRemoteLanguages(currentAccount);
        languageViewModel.addNotificationObserver(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        languageViewModel.removeNotificationObserver(this);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("Language", R.string.Language));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == backMenuItemId) {
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchCollapse() {
                searchListViewAdapter.notifyDataSetChanged();
                if (listView != null) {
                    emptyView.setVisibility(View.GONE);
                    listView.setAdapter(listAdapter);
                }
                languageViewModel.search(null,sortedLanguages,unofficialLanguages);
                if (listView != null) {
                    emptyView.setVisibility(View.GONE);
                    listView.setAdapter(listAdapter);
                }
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                languageViewModel.search(text,sortedLanguages,unofficialLanguages);
                FileLog.d("in on text chane "+ languageViewModel.getSeachResult().size());
                updateSearchView(text);
            }
        });
        item.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));

        listAdapter = new ListAdapter(context, false,searchResult,sortedLanguages,unofficialLanguages);
        searchListViewAdapter = new ListAdapter(context, true,searchResult,sortedLanguages,unofficialLanguages);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        emptyView = new EmptyTextProgressView(context);
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.showTextView();
        emptyView.setShowAtCenter(true);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (getParentActivity() == null || parentLayout == null || !(view instanceof LanguageCell)) {
                return;
            }
            LanguageCell cell = (LanguageCell) view;
            LocaleController.LocaleInfo localeInfo = cell.getCurrentLocale();
            if (localeInfo != null) {
                LocaleController.getInstance().applyLanguage(localeInfo, true, false, false, true, currentAccount);
                parentLayout.rebuildAllFragmentViews(false, false);
            }
            finishFragment();
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if (getParentActivity() == null || parentLayout == null || !(view instanceof LanguageCell)) {
                return false;
            }
            LanguageCell cell = (LanguageCell) view;
            LocaleController.LocaleInfo localeInfo = cell.getCurrentLocale();
            if (localeInfo == null || localeInfo.pathToFile == null || localeInfo.isRemote() && localeInfo.serverIndex != Integer.MAX_VALUE) {
                return false;
            }
            final LocaleController.LocaleInfo finalLocaleInfo = localeInfo;
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("DeleteLocalizationTitle", R.string.DeleteLocalizationTitle));
            builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("DeleteLocalizationText", R.string.DeleteLocalizationText, localeInfo.name)));
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                if (LocaleController.getInstance().deleteLanguage(finalLocaleInfo, currentAccount)) {
                    if (searchResult != null) {
                        searchResult.remove(finalLocaleInfo);
                    }
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    if (searchListViewAdapter != null) {
                        searchListViewAdapter.notifyDataSetChanged();
                    }
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
            }
            return true;
        });

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
        });

        return fragmentView;
    }

    private void updateSearchView(String text) {
        searchResult=languageViewModel.getSeachResult();
        if (text.length() != 0) {
            if (listView != null) {
                listView.setAdapter(searchListViewAdapter);
            }
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.suggestedLangpack) {
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{LanguageCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"checkImage"}, null, null, null, Theme.key_featuredStickers_addedIcon));

        return themeDescriptions;
    }

}

package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.RequestInfoCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class ResponseActivity extends BaseFragment {
    public static final int shadowViewType = 1;
    public static final int customViewType = 0;
    public static final int backMenuItemId = -1;
    private ListAdapter listAdapter;
    private RecyclerListView recyclerView;
    private EmptyTextProgressView emptyView;
    private RLottieImageView response_lottie;
    private List<TLRPC.RequestInfo> responses = new ArrayList<>();

    @Override
    public boolean onFragmentCreate() {
        fillResponses();
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("Request Response", R.string.RequestResponse));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == backMenuItemId) {
                    finishFragment();
                }
            }
        });
        listAdapter = new ResponseActivity.ListAdapter();

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        emptyView = new EmptyTextProgressView(context);
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.showTextView();
        emptyView.setShowAtCenter(true);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        recyclerView = new RecyclerListView(context);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setAdapter(listAdapter);
        frameLayout.addView(recyclerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        response_lottie = new RLottieImageView(context);
        response_lottie.setAnimation(R.raw.response, 300, 300);
        frameLayout.addView(response_lottie, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    private void fillResponses() {
        getRequestsList(responses);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        emptyView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        AndroidUtilities.runOnUIThread(() -> {
            recyclerView.setVisibility(View.VISIBLE);
            response_lottie.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }, 2000);
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case customViewType: {
                    view = new RequestInfoCell(parent.getContext());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case shadowViewType:
                default: {
                    view = new ShadowSectionCell(parent.getContext());
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case customViewType: {
                    RequestInfoCell textRequestInfoCell = (RequestInfoCell) holder.itemView;
                    TLRPC.RequestInfo requestInfo = responses.get(position);
                    if (position == responses.size() - 1)
                        textRequestInfoCell.setRequestInfo(requestInfo, false);
                    else
                        textRequestInfoCell.setRequestInfo(requestInfo, true);
                    break;
                }
                case shadowViewType: {
                    ShadowSectionCell sectionCell = (ShadowSectionCell) holder.itemView;
                    if (!responses.isEmpty() && position == responses.size()) {
                        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(getParentActivity(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(getParentActivity(), R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return responses.size();
        }
    }

    public void getRequestsList(List<TLRPC.RequestInfo> requestsList) {
        SQLiteDatabase database = MessagesStorage.getInstance(currentAccount).getDatabase();
        getMessagesStorage().getStorageQueue().postRunnable(() -> {
            try {
                SQLiteCursor cursor = database.queryFinalized("SELECT * FROM requestsList");
                while (cursor.next()) {
                    NativeByteBuffer requestInfoStream = cursor.byteBufferValue(1);
                    TLRPC.RequestInfo requestInfo = new TLRPC.RequestInfo();
                    requestInfo.readParams(requestInfoStream, false);
                    requestsList.add(requestInfo);
                }
                cursor.dispose();
            } catch (Exception e) {
                requestsList.clear();
                FileLog.e(e);
            }
        });
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        themeDescriptions.add(new ThemeDescription(recyclerView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{RequestInfoCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(recyclerView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));

        themeDescriptions.add(new ThemeDescription(recyclerView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));

        themeDescriptions.add(new ThemeDescription(recyclerView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(recyclerView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(recyclerView, 0, new Class[]{RequestInfoCell.class}, new String[]{"classNameTxtView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(recyclerView, 0, new Class[]{RequestInfoCell.class}, new String[]{"responseTime"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        return themeDescriptions;
    }
}

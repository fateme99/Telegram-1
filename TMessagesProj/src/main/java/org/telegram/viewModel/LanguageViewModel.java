package org.telegram.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.checkerframework.checker.units.qual.A;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LanguageViewModel extends ViewModel {
    AdapterCallBack adapterCallBack;
    private Timer searchTimer;
    private List<LocaleController.LocaleInfo>seachResult;

    public List<LocaleController.LocaleInfo> getSeachResult() {
        return seachResult;
    }

    public LanguageViewModel(List<LocaleController.LocaleInfo> sortedList, List<LocaleController.LocaleInfo> unofficialList,AdapterCallBack callBack) {
        adapterCallBack=callBack;
        initLists(sortedList, unofficialList);
        seachResult=new ArrayList<>();
    }

    public void initLists(List<LocaleController.LocaleInfo> sortedList, List<LocaleController.LocaleInfo> unofficialList) {
        unofficialList=new ArrayList<>(LocaleController.getInstance().unofficialLanguages);
        ArrayList<LocaleController.LocaleInfo> arrayList = LocaleController.getInstance().languages;
        for (int a = 0, size = arrayList.size(); a < size; a++) {
            LocaleController.LocaleInfo info = arrayList.get(a);
            if (info.serverIndex == Integer.MAX_VALUE) {
                unofficialList.add(info);
            } else {
                sortedList.add(info);
            }

        }
        Collections.sort(unofficialList, getComparator());
        Collections.sort(sortedList, getComparator());
    }

    public void loadRemoteLanguages(final int currentAccount) {
        LocaleController.getInstance().loadRemoteLanguages(currentAccount);
    }

    public void addNotificationObserver(NotificationCenter.NotificationCenterDelegate observer) {
        NotificationCenter.getGlobalInstance().addObserver(observer, NotificationCenter.suggestedLangpack);
    }

    public void removeNotificationObserver(NotificationCenter.NotificationCenterDelegate observer) {
        NotificationCenter.getGlobalInstance().removeObserver(observer, NotificationCenter.suggestedLangpack);
    }


    private Comparator<LocaleController.LocaleInfo> getComparator() {
        final LocaleController.LocaleInfo currentLocale = LocaleController.getInstance().getCurrentLocaleInfo();
        Comparator<LocaleController.LocaleInfo> comparator = (o, o2) -> {
            if (o == currentLocale) {
                return -1;
            } else if (o2 == currentLocale) {
                return 1;
            } else if (o.serverIndex == o2.serverIndex) {
                return o.name.compareTo(o2.name);
            }
            if (o.serverIndex > o2.serverIndex) {
                return 1;
            } else if (o.serverIndex < o2.serverIndex) {
                return -1;
            }
            return 0;
        };
        return comparator;
    }

    public void search(final String query, List<LocaleController.LocaleInfo> sortedList, List<LocaleController.LocaleInfo> unofficialList) {
        if (query == null) {
            seachResult = null;
        } else {
            try {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        searchTimer.cancel();
                        searchTimer = null;
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    processSearch(query, sortedList, unofficialList);
                }
            }, 100, 300);
        }
    }

    private void processSearch(final String query, List<LocaleController.LocaleInfo> sortedLanguages, List<LocaleController.LocaleInfo> unofficialLanguages) {
        Utilities.searchQueue.postRunnable(() -> {

            String q = query.trim().toLowerCase();
            if (q.length() == 0) {
                return;
            }
            List<LocaleController.LocaleInfo> result = new ArrayList<>();
            for (int a = 0, N = unofficialLanguages.size(); a < N; a++) {
                LocaleController.LocaleInfo c = unofficialLanguages.get(a);
                if (c.name.toLowerCase().startsWith(query) || c.nameEnglish.toLowerCase().startsWith(query)) {
                    result.add(c);
                }
            }

            for (int a = 0, N = sortedLanguages.size(); a < N; a++) {
                LocaleController.LocaleInfo c = sortedLanguages.get(a);
                if (c.name.toLowerCase().startsWith(query) || c.nameEnglish.toLowerCase().startsWith(query)) {
                    result.add(c);
                }
            }

            updateSearchResults(q,result);
        });
    }
    private void updateSearchResults(String text,final List<LocaleController.LocaleInfo> arrCounties) {
        AndroidUtilities.runOnUIThread(() -> {
            seachResult = arrCounties;
            adapterCallBack.updateView(text);
        });
    }

    public interface AdapterCallBack{
        void updateView(String text);
    }


}

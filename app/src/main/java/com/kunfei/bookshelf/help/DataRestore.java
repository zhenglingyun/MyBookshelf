package com.kunfei.bookshelf.help;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.utils.FileUtil;
import com.kunfei.bookshelf.utils.SharedPreferencesUtil;
import com.kunfei.bookshelf.utils.XmlUtils;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.utils.FileUtil;
import com.kunfei.bookshelf.utils.SharedPreferencesUtil;
import com.kunfei.bookshelf.utils.XmlUtils;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by GKF on 2018/1/30.
 * 数据恢复
 */

public class DataRestore {

    public static DataRestore getInstance() {
        return new DataRestore();
    }

    public Boolean run() throws Exception {
        String dirPath = FileUtil.getSdCardPath() + "/YueDu";
        restoreConfig(dirPath);
        restoreBookSource(dirPath);
        restoreBookShelf(dirPath);
        restoreSearchHistory(dirPath);
        restoreReplaceRule(dirPath);
        return true;
    }

    private void restoreConfig(String dirPath) {
        Map<String, ?> entries = null;
        try (FileInputStream ins = new FileInputStream(dirPath + "/config.xml")) {
            entries = XmlUtils.readMapXml(ins);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (entries == null || entries.isEmpty()) {
            String json = DocumentHelper.readString("config.json", dirPath);
            if (json != null) {
                entries = new Gson().fromJson(json, new TypeToken<Map<String, ?>>() {
                }.getType());
            }
        }
        if (entries == null || entries.isEmpty()) return;
        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            Object v = entry.getValue();
            String key = entry.getKey();
            SharedPreferencesUtil.saveData(key, v);
        }
        SharedPreferencesUtil.saveData("versionCode", MApplication.getVersionCode());
    }

    private void restoreBookShelf(String file) throws Exception {
        String json = DocumentHelper.readString("myBookShelf.json", file);
        if (json != null) {
            List<BookShelfBean> bookShelfList = new Gson().fromJson(json, new TypeToken<List<BookShelfBean>>() {
            }.getType());
            for (BookShelfBean bookshelf : bookShelfList) {
                if (bookshelf.getNoteUrl() != null) {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookshelf);
                }
                if (bookshelf.getBookInfoBean().getNoteUrl() != null) {
                    DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookshelf.getBookInfoBean());
                }
            }
        }
    }

    private void restoreBookSource(String file) throws Exception {
        String json = DocumentHelper.readString("myBookSource.json", file);
        if (json != null) {
            List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
            }.getType());
            BookSourceManager.addBookSource(bookSourceBeans);
        }
    }

    private void restoreSearchHistory(String file) throws Exception {
        String json = DocumentHelper.readString("myBookSearchHistory.json", file);
        if (json != null) {
            List<SearchHistoryBean> searchHistoryBeans = new Gson().fromJson(json, new TypeToken<List<SearchHistoryBean>>() {
            }.getType());
            if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
                DbHelper.getInstance().getmDaoSession().getSearchHistoryBeanDao().insertOrReplaceInTx(searchHistoryBeans);
            }
        }
    }

    private void restoreReplaceRule(String file) throws Exception {
        String json = DocumentHelper.readString("myBookReplaceRule.json", file);
        if (json != null) {
            List<ReplaceRuleBean> replaceRuleBeans = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
            }.getType());
            ReplaceRuleManager.addDataS(replaceRuleBeans);
        }
    }
}

package com.semantyca.core.server;

import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.localization.Sentence;
import com.semantyca.core.localization.exception.UtilityDBException;

import java.util.List;

public interface IUtilityDatabase {
    List<String> getAllWordsGrouped() throws UtilityDBException;
    List<String> getAllWordsGrouped(String app) throws UtilityDBException;
    int getApplication(String appCode);
    Sentence getWord(String app, String keyword);
    Sentence getWordEntry(int id);
    Sentence getWord(int id);
    boolean isExistWord(int appId, String keyword, LanguageCode lang);
    int saveWord(int appId, String keyword, LanguageCode lang, String value) throws UtilityDBException;
    int delWord(int id);
    int delWord(String app, String keyword);
}

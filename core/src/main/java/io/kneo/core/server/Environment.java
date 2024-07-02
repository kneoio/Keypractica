package io.kneo.core.server;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.localization.Vocabulary;

import java.util.List;

public class Environment {
    public static List<LanguageCode> AVAILABLE_LANGUAGES = List.of(LanguageCode.ENG, LanguageCode.POR);
    public static final String realm = "kneo.io";
    public static final String realmShortName = "kneo";
    public static String[] publicModules = {"workspace"};
    public static IUtilityDatabase utilityDatabase;
    public static Vocabulary vocabulary;
    public static Boolean mailEnable = false;





}

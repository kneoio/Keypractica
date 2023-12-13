package io.kneo.core.localization;

import lombok.Getter;

public enum LanguageCode {
    UNKNOWN(0, "unknown", "???"), ENG(45, "english", "en"), RUS(570, "russian", "ru"), KAZ(255, "kazakh", "kk"), BUL(
            115, "bulgarian", "bg"), POR(545, "portuguese", "pt"), SPA(230, "spanish", "es"), CHI(315, "chinese",
            "zh"), DEU(316, "german", "de"), FRA(317, "french", "fr"), POL(318, "polish", "pl"), BEL(319,
            "belarusian",
            "be"), CES(320, "czech", "cs"), GRE(321, "greek", "el"), UKR(322, "ukrainian", "uk"), TUR(
            323, "turkish", "tr"), ITA(324, "italian", "it"), KOR(325, "korean", "ko"), JPN(326,
            "japanese", "ja"), HIN(327, "hindi", "hi"), ARA(328, "arabic", "ar"), LAV(329, "latvian", "lv");
    // @Deprecated CHN(3150, "chinese"), @Deprecated CHO(3151, "chinese");

    @Getter
    private final int code;
    @Getter
    private final String lang;
    private final String altCode;

    LanguageCode(int code, String lang, String altCode) {
        this.code = code;
        this.lang = lang;
        this.altCode = altCode;
    }

    public static LanguageCode getType(int code) {
        for (LanguageCode type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getAlternateCode() {
        return altCode;
    }

}

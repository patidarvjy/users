package com.docutools.users.values;

public enum LanguageTag {
    DE,
    EN,
    BG,
    HR,
    CS,
    DA,
    ES,
    FR,
    HU,
    IT,
    NL,
    NO,
    PL,
    PT,
    TR,
    RO,
    RU,
    SK,
    SL,
    SR,
    SV;

    public static LanguageTag[] getLanguages() {
        return LanguageTag.values();
    }
}

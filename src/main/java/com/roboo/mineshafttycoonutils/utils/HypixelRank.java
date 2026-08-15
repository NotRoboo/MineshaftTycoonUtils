package com.roboo.mineshafttycoonutils.utils;

import java.util.Locale;

public enum HypixelRank {
    MVPPP, MVPP, MVP, VIPP, VIP, NONE;

    public static HypixelRank parse(String bracketContent) {
        if (bracketContent == null) return NONE;
        String tag = bracketContent.toUpperCase(Locale.ROOT).trim();
        if (tag.startsWith("MVP++")) return MVPPP;
        if (tag.startsWith("MVP+")) return MVPP;
        if (tag.startsWith("MVP")) return MVP;
        if (tag.startsWith("VIP+")) return VIPP;
        if (tag.startsWith("VIP")) return VIP;
        return NONE;
    }
}
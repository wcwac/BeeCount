package com.wangc.bill.reference;

import android.content.SharedPreferences;

/** Defaults confirmed in the original database initialization and list adapter. */
public final class OriginalSettings {
    public static final String REMARK_MAIN = "remark_main";
    public static final String SHOW_TIME = "show_time";
    public static final String SHOW_OLD_COST = "show_old_cost";
    public static final boolean DEFAULT_REMARK_MAIN = false;
    public static final boolean DEFAULT_SHOW_TIME = false;
    // Original database.c.c explicitly sets ShowOldCost=true on first creation.
    public static final boolean DEFAULT_SHOW_OLD_COST = true;
    public static final boolean DEFAULT_AUTO_JUMP_ACCESSIBILITY = true;

    private OriginalSettings() {}

    public static boolean remarkMain(SharedPreferences preferences) {
        if (preferences.contains(REMARK_MAIN)) return preferences.getBoolean(REMARK_MAIN, DEFAULT_REMARK_MAIN);
        // Previous builds stored this visible setting under a misleading key. Honor an explicit
        // choice; the old, unwritten default must not become a user's preference during upgrade.
        if (preferences.contains("show_remark")) return preferences.getBoolean("show_remark", DEFAULT_REMARK_MAIN);
        return DEFAULT_REMARK_MAIN;
    }

    public static boolean showTime(SharedPreferences preferences) {
        return preferences.getBoolean(SHOW_TIME, DEFAULT_SHOW_TIME);
    }

    public static boolean showOldCost(SharedPreferences preferences) {
        return preferences.getBoolean(SHOW_OLD_COST, DEFAULT_SHOW_OLD_COST);
    }
}

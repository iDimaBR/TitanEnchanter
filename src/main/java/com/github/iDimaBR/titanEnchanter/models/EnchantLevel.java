package com.github.iDimaBR.titanEnchanter.menus;

public enum EnchantLevel {
    LEVEL_1_6(1, 6, "1-6"),
    LEVEL_7_12(7, 12, "7-12"),
    LEVEL_13_18(13, 18, "13-18"),
    LEVEL_19_24(19, 24, "19-24"),
    LEVEL_25_30(25, 30, "25-30");

    private final int minLevel;
    private final int maxLevel;
    private final String configKey;

    EnchantLevel(int minLevel, int maxLevel, String configKey) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.configKey = configKey;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getConfigKey() {
        return configKey;
    }
}
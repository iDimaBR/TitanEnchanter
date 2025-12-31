package com.github.iDimaBR.titanEnchanter.models;

public enum EnchantLevel {
    LEVEL_1_6(1, 6, "1-6", new int[]{1, 3, 6}),
    LEVEL_7_12(7, 12, "7-12", new int[]{7, 9, 12}),
    LEVEL_13_18(13, 18, "13-18", new int[]{13, 15, 18}),
    LEVEL_19_24(19, 24, "19-24", new int[]{19, 21, 24}),
    LEVEL_25_30(25, 30, "25-30", new int[]{25, 27, 30});

    private final int minLevel;
    private final int maxLevel;
    private final String configKey;
    private final int[] offeredCosts;

    EnchantLevel(int minLevel, int maxLevel, String configKey, int[] offeredCosts) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.configKey = configKey;
        this.offeredCosts = offeredCosts;
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

    public int[] getOfferedCosts() {
        return offeredCosts;
    }
}

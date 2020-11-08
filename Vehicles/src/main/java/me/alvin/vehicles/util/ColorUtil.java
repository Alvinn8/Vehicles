package me.alvin.vehicles.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public final class ColorUtil {
    private ColorUtil() {}

    @Nullable
    public static DyeColor getDyeColorForMaterial(Material material) {
        String materialName = material.toString();
        if (materialName.endsWith("_DYE")) {
            String colorName = materialName.substring(0, materialName.length() - 4);
            return DyeColor.valueOf(colorName);
        }
        return null;
        /*
        switch (material) {
            case WHITE_DYE: return DyeColor.WHITE;
            case ORANGE_DYE: return DyeColor.ORANGE;
            case MAGENTA_DYE: return DyeColor.MAGENTA;
            case LIGHT_BLUE_DYE: return DyeColor.LIGHT_BLUE;
            case YELLOW_DYE: return DyeColor.YELLOW;
            case LIME_DYE: return DyeColor.LIME;
            case PINK_DYE: return DyeColor.PINK;
            case GRAY_DYE: return DyeColor.GRAY;
            case LIGHT_GRAY_DYE: return DyeColor.LIGHT_GRAY;
            case CYAN_DYE: return DyeColor.CYAN;
            case PURPLE_DYE: return DyeColor.PURPLE;
            case BLUE_DYE: return DyeColor.BLUE;
            case BROWN_DYE: return DyeColor.BROWN;
            case GREEN_DYE: return DyeColor.GREEN;
            case RED_DYE: return DyeColor.RED;
            case BLACK_DYE: return DyeColor.BLACK;
        }
        */
    }
}

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
    }
}

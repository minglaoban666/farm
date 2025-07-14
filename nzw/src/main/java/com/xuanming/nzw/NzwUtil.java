package com.xuanming.nzw;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

public class NzwUtil {
    public static final String THY_PREFIX = "§b桃韵斋>>>";

    public static void print(String text) {
        Bukkit.getConsoleSender().sendMessage(text);
    }

    public static boolean isInventoryFull(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                return false;
            }
        }
        return true;
    }

    public static void openMenu(Player player, String menu) {
        // TODO: 实现自定义菜单打开逻辑
        print(THY_PREFIX + " §e打开菜单: " + menu);
    }
} 
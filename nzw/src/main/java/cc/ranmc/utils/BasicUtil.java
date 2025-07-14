package cc.ranmc.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 基础工具类
 * 提供常用的工具方法
 */
public class BasicUtil {
    
    public static final String THY_PREFIX = "§b[桃韵斋] §r";
    
    /**
     * 检查玩家背包是否已满
     * @param player 玩家
     * @return 是否已满
     */
    public static boolean isInventoryFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }
    
    /**
     * 输出日志
     * @param message 消息
     */
    public static void print(String message) {
        System.out.println(message);
    }
    
    /**
     * 检查物品是否为空
     * @param item 物品
     * @return 是否为空
     */
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }
} 
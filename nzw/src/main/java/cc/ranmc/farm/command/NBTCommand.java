package cc.ranmc.farm.command;

import cc.ranmc.farm.util.NBTUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.print;

/**
 * NBT测试命令
 * 用于测试NBT功能
 */
public class NBTCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            print(color("&c该命令只能由玩家执行"));
            return true;
        }

        if (!sender.hasPermission("fm.admin")) {
            player.sendMessage(color("&c你没有权限使用此命令"));
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                showNBTInfo(player);
                break;
            case "set":
                if (args.length >= 3) {
                    setNBT(player, args[1], args[2]);
                } else {
                    player.sendMessage(color("&c用法: /nbt set <key> <value>"));
                }
                break;
            case "get":
                if (args.length >= 2) {
                    getNBT(player, args[1]);
                } else {
                    player.sendMessage(color("&c用法: /nbt get <key>"));
                }
                break;
            case "remove":
                if (args.length >= 2) {
                    removeNBT(player, args[1]);
                } else {
                    player.sendMessage(color("&c用法: /nbt remove <key>"));
                }
                break;
            case "compare":
                compareItems(player);
                break;
            case "create":
                if (args.length >= 3) {
                    createCustomItem(player, args[1], args[2]);
                } else {
                    player.sendMessage(color("&c用法: /nbt create <key> <value>"));
                }
                break;
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(color("&6=== NBT命令帮助 ==="));
        player.sendMessage(color("&e/nbt info &7- 显示手持物品的NBT信息"));
        player.sendMessage(color("&e/nbt set <key> <value> &7- 设置NBT标签"));
        player.sendMessage(color("&e/nbt get <key> &7- 获取NBT标签值"));
        player.sendMessage(color("&e/nbt remove <key> &7- 移除NBT标签"));
        player.sendMessage(color("&e/nbt compare &7- 比较两个物品的NBT"));
        player.sendMessage(color("&e/nbt create <key> <value> &7- 创建自定义物品"));
    }

    private void showNBTInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(color("&c请手持一个物品"));
            return;
        }

        player.sendMessage(color("&6=== 物品NBT信息 ==="));
        player.sendMessage(color("&e物品类型: &f" + item.getType()));
        player.sendMessage(color("&e数量: &f" + item.getAmount()));
        
        String nbtString = NBTUtil.getNBTString(item);
        if (nbtString.isEmpty()) {
            player.sendMessage(color("&eNBT数据: &f无"));
        } else {
            player.sendMessage(color("&eNBT数据: &f" + nbtString));
        }

        // 显示所有NBT标签
        Map<String, Object> allNBT = NBTUtil.getAllNBT(item);
        if (!allNBT.isEmpty()) {
            player.sendMessage(color("&6=== NBT标签列表 ==="));
            for (Map.Entry<String, Object> entry : allNBT.entrySet()) {
                player.sendMessage(color("&e" + entry.getKey() + ": &f" + entry.getValue()));
            }
        }
    }

    private void setNBT(Player player, String key, String value) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(color("&c请手持一个物品"));
            return;
        }

        ItemStack newItem = NBTUtil.setNBTString(item, key, value);
        player.getInventory().setItemInMainHand(newItem);
        player.sendMessage(color("&a成功设置NBT标签: &e" + key + " = " + value));
    }

    private void getNBT(Player player, String key) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(color("&c请手持一个物品"));
            return;
        }

        if (NBTUtil.hasNBTKey(item, key)) {
            String value = NBTUtil.getNBTString(item, key);
            player.sendMessage(color("&aNBT标签 &e" + key + " &a的值: &f" + value));
        } else {
            player.sendMessage(color("&c物品没有NBT标签: &e" + key));
        }
    }

    private void removeNBT(Player player, String key) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(color("&c请手持一个物品"));
            return;
        }

        ItemStack newItem = NBTUtil.removeNBTKey(item, key);
        player.getInventory().setItemInMainHand(newItem);
        player.sendMessage(color("&a成功移除NBT标签: &e" + key));
    }

    private void compareItems(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand.getType() == Material.AIR || offHand.getType() == Material.AIR) {
            player.sendMessage(color("&c请双手各持一个物品进行比较"));
            return;
        }

        boolean sameNBT = NBTUtil.hasSameNBT(mainHand, offHand);
        player.sendMessage(color("&6=== 物品比较结果 ==="));
        player.sendMessage(color("&e主手物品: &f" + mainHand.getType()));
        player.sendMessage(color("&e副手物品: &f" + offHand.getType()));
        player.sendMessage(color("&eNBT是否相同: &f" + (sameNBT ? "是" : "否")));
    }

    private void createCustomItem(Player player, String key, String value) {
        ItemStack customItem = NBTUtil.createCustomItem(
            new ItemStack(Material.DIAMOND), 
            key, 
            value
        );
        
        // 添加自定义名称
        customItem = NBTUtil.setNBTString(customItem, "CustomName", "自定义物品");
        
        player.getInventory().addItem(customItem);
        player.sendMessage(color("&a成功创建自定义物品"));
        player.sendMessage(color("&eNBT标签: &f" + key + " = " + value));
    }
} 
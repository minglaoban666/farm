package cc.ranmc.farm.util;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.config.ConfigManager;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
// import cc.ranmc.utils.BasicUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cc.ranmc.farm.constant.FarmConstant.PANE;
// import static cc.ranmc.utils.BasicUtil.THY_PREFIX;

public class FarmUtil {

    private static final Main plugin = Main.getInstance();

    /**
     * 打开农作物仓库菜单
     */
    public static void openCropGUI(Player player, String crop, int page) {
        ConfigManager config = ConfigManager.getInstance();
        
        // 根据权限获取最大页数
        int maxPage = config.getMaxPagesNormal();
        if (player.hasPermission(config.getPermission("vip"))) {
            maxPage = config.getMaxPagesVip();
        }
        if (player.hasPermission(config.getPermission("svip"))) {
            maxPage = config.getMaxPagesSvip();
        }
        
        if (page > maxPage) page = maxPage;
        if (page < 1) page = 1;

        crop = crop.toUpperCase();
        Cop cop = new Cop(crop);
        if (cop.getMaterial() == Material.AIR) {
            player.sendMessage(color(config.getErrorMessage("crop_not_found")));
            return;
        }
        SQLRow playerRow = DataUtil.getPlayerData(player);
        int count = playerRow.getInt(crop, 0);
        Inventory inventory = Bukkit.createInventory(null, config.getGuiSize(),
                color(config.getGuiTitle()));

        // 使用配置文件中的按钮设置
        inventory.setItem(config.getButtonSlot("back"), 
            getItem(Material.valueOf(config.getButtonMaterial("back")), 1, 
                color(config.getButtonName("back"))));
        inventory.setItem(46, PANE);
        inventory.setItem(config.getButtonSlot("previous_page"), 
            getItem(Material.valueOf(config.getButtonMaterial("previous_page")), 1,
                color(config.getButtonName("previous_page").replace("{page}", String.valueOf(page))),
                config.getButtonLore("previous_page").toArray(new String[0])));
        inventory.setItem(48, PANE);
        inventory.setItem(config.getButtonSlot("info"), 
            getItem(cop.getMaterial(), 1,
                color(config.getButtonName("info").replace("{crop_name}", cop.getName())),
                config.getButtonLore("info").stream()
                    .map(lore -> lore.replace("{count}", String.valueOf(count)))
                    .toArray(String[]::new)));
        inventory.setItem(50, PANE);
        inventory.setItem(config.getButtonSlot("next_page"), 
            getItem(Material.valueOf(config.getButtonMaterial("next_page")), 1,
                color(config.getButtonName("next_page").replace("{page}", String.valueOf(page))),
                config.getButtonLore("next_page").toArray(new String[0])));
        inventory.setItem(52, PANE);
        inventory.setItem(config.getButtonSlot("close"), 
            getItem(Material.valueOf(config.getButtonMaterial("close")), 1,
                color(config.getButtonName("close"))));

        int itemsPerPage = config.getItemsPerPage();
        int maxStackSize = cop.getMaterial().getMaxStackSize();
        int startIndex = (page - 1) * itemsPerPage * maxStackSize;
        int endIndex = page * itemsPerPage * maxStackSize;
        if (endIndex > count) endIndex = count;
        int pageCount = endIndex - startIndex;
        ItemStack copItem = new ItemStack(cop.getMaterial());
        while (pageCount > 0) {
            int amount = Math.min(pageCount, maxStackSize);
            pageCount -= amount;
            copItem.setAmount(amount);
            inventory.addItem(copItem.clone());
        }
        player.openInventory(inventory);
    }

    public static int getInventoryAirCount(Player player) {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i) == null) {
                count++;
            }
        }
        return count;
    }

    /**
     * 检查玩家背包是否已满
     * @param player 玩家
     * @return 是否已满
     */
    public static boolean isInventoryFull(Player player) {
        return getInventoryAirCount(player) == 0;
    }

    /**
     * 检查物品是否为有效的农作物（支持NBT比较）
     * @param item 物品
     * @param targetMaterial 目标材料
     * @return 是否为有效农作物
     */
    public static boolean isValidCrop(ItemStack item, Material targetMaterial) {
        if (item == null || item.getType() != targetMaterial) {
            return false;
        }
        
        // 使用NBT比较，确保物品完全相同
        return NBTUtil.hasSameNBT(item, new ItemStack(targetMaterial));
    }

    public static void save(Player player, Inventory inventory) {
        ConfigManager config = ConfigManager.getInstance();
        
        ItemStack copItem = inventory.getItem(config.getButtonSlot("info"));
        if (copItem == null) return;
        Cop cop = new Cop(copItem.getType().toString());
        if (cop.getMaterial() == Material.AIR) return;
        
        ItemStack pageItem = inventory.getItem(config.getButtonSlot("previous_page"));
        if (pageItem == null || pageItem.getItemMeta() == null) return;
        int page = Integer.parseInt(pageItem.getItemMeta().getDisplayName().split(" ")[1]);
        
        SQLRow playerRow = DataUtil.getPlayerData(player);
        int totalItems = playerRow.getInt(cop.getMaterial().toString().toUpperCase(), 0);
        int itemsPerPage = config.getItemsPerPage();
        int maxStackSize = cop.getMaterial().getMaxStackSize();
        int startIndex = (page - 1) * itemsPerPage * maxStackSize;
        int endIndex = page * itemsPerPage * maxStackSize;
        if (endIndex > totalItems) endIndex = totalItems;
        int pageCount = endIndex - startIndex;
        if (pageCount < 0) pageCount = 0;
        int count = 0;
        for (int i = 0; i < itemsPerPage; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                // 使用NBT比较来确保物品完全相同
                if (isValidCrop(item, cop.getMaterial())) {
                    count += Objects.requireNonNull(inventory.getItem(i)).getAmount();
                } else {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                    if (isInventoryFull(player)) {
                        player.getWorld().dropItem(player.getLocation(), item);
                        player.sendMessage(color(config.getErrorMessage("invalid_item_dropped")));
                    } else {
                        player.getInventory().addItem(item);
                        player.sendMessage(color(config.getErrorMessage("invalid_item")));
                    }
                }
            }
        }
        if (count > pageCount) {
            totalItems += count - pageCount;
        } else if (count < pageCount) {
            totalItems -= pageCount - count;
        }
        DataUtil.setPlayerData(player, cop, totalItems);
        inventory.setItem(config.getButtonSlot("info"), new ItemStack(Material.AIR));
    }

    /**
     * 输出日志
     * @param text 内容
     */
    public static void print(String text){
        Bukkit.getConsoleSender().sendMessage(color(text));
    }

    /**
     * 文本替换
     * @param text 内容
     * @param p 玩家
     * @return 内容
     */
    public static String color(String text, Player p) {
        if(text == null) {
            text = "";
            print("&b[作物仓库] §c加载文本错误");
        }else {
            text=text.replace("&", "§")
                    .replace("%player%",p.getName())
                    .replace("%player_x%",""+p.getLocation().getBlockX())
                    .replace("%player_y%",""+p.getLocation().getBlockY())
                    .replace("%player_z%",""+p.getLocation().getBlockZ());
        }
        return text;
    }

    public static String color(String text) {
        if(text == null) {
            text = "";
            print("&b[作物仓库] §c加载文本错误");
        }else {
            text=text.replace("&", "§");
        }
        return text;
    }

    /**
     * 获取物品
     */
    public static ItemStack getItem(Material material, int count) {
        return new ItemStack(material, count);
    }

    public static ItemStack getItem(Material material, int count, String name) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(color(name));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItem(Material material, int count, String name, String... lore) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        for (int i = 0; i < lore.length; i++) {
            lore[i] = color(lore[i]);
        }
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItem(Material material, int count, String name, List<String> lore) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(color(name));
        lore.replaceAll(FarmUtil::color);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 创建带有NBT标签的自定义物品
     * @param material 材料
     * @param count 数量
     * @param name 显示名称
     * @param nbtKey NBT键
     * @param nbtValue NBT值
     * @param lore 描述
     * @return 自定义物品
     */
    public static ItemStack getCustomItem(Material material, int count, String name, String nbtKey, String nbtValue, String... lore) {
        ItemStack item = getItem(material, count, name, lore);
        return NBTUtil.setNBTString(item, nbtKey, nbtValue);
    }

    /**
     * 检查物品是否为自定义物品
     * @param item 物品
     * @param nbtKey NBT键
     * @return 是否为自定义物品
     */
    public static boolean isCustomItem(ItemStack item, String nbtKey) {
        return NBTUtil.isCustomItem(item, nbtKey);
    }
}

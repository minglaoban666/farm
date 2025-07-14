package cc.ranmc.farm.util;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
import com.xuanming.nzw.NzwUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

import static cc.ranmc.farm.constant.FarmConstant.PANE;

public class FarmUtil {

    private static final Main plugin = Main.getInstance();

    public static String getMainGuiTitle() {
        return Main.getInstance().getConfig().getString("gui.main_title", "&d&l桃韵斋丨作物总仓库");
    }
    public static String getCropGuiTitle(String cropDisplay) {
        String pattern = Main.getInstance().getConfig().getString("gui.crop_title", "&d&l桃韵斋丨%crop%仓库");
        return pattern.replace("%crop%", cropDisplay);
    }

    /**
     * 打开农作物仓库菜单
     */
    public static void openCropGUI(Player player, String cropIdInput, int pageInput) {
        final String cropIdFinal = cropIdInput;
        final int pageParamFinal = pageInput;
        CropConfig found = null;
        for (CropConfig c : Main.getInstance().getCrops()) {
            if (c.getId().equalsIgnoreCase(cropIdInput)) {
                found = c;
                break;
            }
        }
        final CropConfig cropFinal = found;
        final String displayNameFinal = cropFinal != null ? cropFinal.getDisplay() : color("&c未知作物");
        final Material cropMaterialFinal = cropFinal != null ? cropFinal.getMaterial() : Material.BARRIER;
        DataUtil.getPlayerDataAsync(player, true, playerRow -> {
            int total = playerRow.getInt(cropIdFinal.toUpperCase(), 0);
            int maxStack = cropMaterialFinal.getMaxStackSize();
            int itemsPerPage = 45 * maxStack;
            int totalPages = Math.max(1, (total + itemsPerPage - 1) / itemsPerPage);
            int safePage = Math.max(1, Math.min(pageParamFinal, totalPages));
            int start = (safePage - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, total);
            int left = end - start;
            Inventory inv = Bukkit.createInventory(player, 54, color(getCropGuiTitle(displayNameFinal)));
            for (int i = 0; i < 45; i++) {
                if (left <= 0) break;
                int amount = Math.min(left, maxStack);
                inv.setItem(i, new ItemStack(cropMaterialFinal, amount));
                left -= amount;
            }
            // 恢复下方按钮
            inv.setItem(45, getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c返回菜单"));
            inv.setItem(46, PANE);
            inv.setItem(47, getItem(Material.PAPER, 1, "&b当前页数 " + safePage + "/" + totalPages, "&e左键切换上页", "&e右键跳转首页"));
            inv.setItem(48, PANE);
            inv.setItem(49, getItem(cropMaterialFinal, 1, displayNameFinal, "&e仓库库存: &f" + total, "&e点击取出/存入作物"));
            inv.setItem(50, PANE);
            inv.setItem(51, getItem(Material.PAPER, 1, "&b当前页数 " + safePage + "/" + totalPages, "&e左键切换下页", "&e右键快速翻页"));
            inv.setItem(52, PANE);
            inv.setItem(53, getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c关闭菜单"));
            player.openInventory(inv);
        });
    }

    /**
     * 打开总仓库菜单
     */
    public static void openMainGUI(Player player) {
        Main plugin = Main.getInstance();
        List<CropConfig> crops = plugin.getCrops();
        Inventory inv = Bukkit.createInventory(null, 54, color(getMainGuiTitle()));
        fillMainMenuWithCrops(inv, crops, player);
        player.openInventory(inv);
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

    public static String stripColor(String input) {
        return input == null ? null : input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    public static void save(Player player, Inventory inventory) {
        ItemStack cropItem = inventory.getItem(49);
        if (cropItem == null || cropItem.getType() == Material.AIR) {
            return;
        }
        // 通过 config.yml 查找 cropId
        String cropId;
        String display = stripColor(cropItem.getItemMeta() != null ? cropItem.getItemMeta().getDisplayName() : null);
        Material mat = cropItem.getType();
        cropId = Main.getInstance().getCrops().stream().filter(crop -> crop.getMaterial() == mat && stripColor(crop.getDisplay()).equals(display)).findFirst().map(CropConfig::getId).orElse(null);
        if (cropId == null) return;
        int page = 1;
        try {
            String pageStr = Objects.requireNonNull(inventory.getItem(47)).getItemMeta().getDisplayName().split(" ")[1];
            if (pageStr.contains("/")) pageStr = pageStr.split("/")[0];
            page = Integer.parseInt(pageStr);
        } catch (Exception e) {
            page = 1;
        }
        // 异步获取并写入玩家数据
        int finalPage = page;
        DataUtil.getPlayerDataAsync(player, false, playerRow -> {
            int totalItems = playerRow.getInt(cropId.toUpperCase(), 0);
            int maxStackSize = cropItem.getType().getMaxStackSize();
            int itemsPerPage = 45 * maxStackSize;
            int startIndex = (finalPage - 1) * itemsPerPage;
            int endIndex = Math.min(finalPage * itemsPerPage, totalItems);
            int pageCount = endIndex - startIndex;
            if (pageCount < 0) pageCount = 0;
            int count = 0;
            for (int i = 0; i < 45; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() == cropItem.getType()) {
                    count += item.getAmount();
                } else if (item != null) {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                }
            }
            if (count > pageCount) {
                totalItems += count - pageCount;
            } else if (count < pageCount) {
                totalItems -= pageCount - count;
            }
            Map<String, Integer> updateMap = new HashMap<>();
            updateMap.put(cropId.toUpperCase(), totalItems);
            DataUtil.setPlayerDataAsync(player, updateMap, null);
            inventory.setItem(49, new ItemStack(Material.AIR));
        });
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

    public static class CropConfig {
        private final String id;
        private final String display;
        private final Material material;
        private final int slot;
        private final List<String> lore;
        public CropConfig(String id, String display, Material material, int slot, List<String> lore) {
            this.id = id;
            this.display = display;
            this.material = material;
            this.slot = slot;
            this.lore = lore;
        }
        public String getId() { return id; }
        public String getDisplay() { return display; }
        public Material getMaterial() { return material; }
        public int getSlot() { return slot; }
        public List<String> getLore() { return lore; }
    }

    public static List<CropConfig> loadCropsFromConfig(JavaPlugin plugin) {
        List<CropConfig> crops = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("crops");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                String display = section.getString(id + ".display", id);
                String materialStr = section.getString(id + ".material", "WHEAT");
                int slot = section.getInt(id + ".slot", 0);
                List<String> lore = section.getStringList(id + ".lore");
                Material material = Material.matchMaterial(materialStr);
                if (material == null) material = Material.WHEAT;
                crops.add(new CropConfig(id, display, material, slot, lore));
            }
        }
        return crops;
    }

    public static void fillMainMenuWithCrops(Inventory inv, List<CropConfig> crops, Player player) {
        // 异步获取玩家数据
        DataUtil.getPlayerDataAsync(player, true, playerRow -> {
            for (CropConfig crop : crops) {
                int count = playerRow.getInt(crop.getId().toUpperCase(), 0);
                inv.setItem(crop.getSlot(), getItem(crop.getMaterial(), 1, crop.getDisplay(), "&e仓库库存: &f" + count, "&e点击打开作物仓库"));
            }
        });
    }
}

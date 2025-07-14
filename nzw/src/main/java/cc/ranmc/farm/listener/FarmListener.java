package cc.ranmc.farm.listener;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.DataUtil;
import cc.ranmc.farm.util.FarmUtil;
import cc.ranmc.farm.util.FarmUtil.CropConfig;
import com.xuanming.nzw.NzwUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.event.EventPriority;
import java.util.UUID;

import static cc.ranmc.farm.constant.FarmConstant.CROP_TYPE;
import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.getInventoryAirCount;
import static cc.ranmc.farm.util.FarmUtil.openCropGUI;
import static cc.ranmc.farm.util.FarmUtil.openMainGUI;

public class FarmListener implements Listener {

    private static final Main plugin = Main.getInstance();
    // 提示
    private final List<String> noteList = new ArrayList<>();
    // 新增：记录每个玩家上次提示时间
    private final Map<UUID, Long> lastTipTime = new HashMap<>();

    /**
     * 菜单关闭
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(color("&d&l桃韵斋丨作物仓库"))) {
            return;
        }
        // 移除自动 save，防止数量膨胀
        // Player player = (Player) event.getPlayer();
        // Inventory inventory = event.getInventory();
        // FarmUtil.save(player, inventory);
    }

    /**
     * 菜单点击
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        if (inv == null || event.getCurrentItem() == null) return;
        String title = event.getView().getTitle();
        if (title != null && title.contains("桃韵斋") && title.contains("仓库")) {
            // 全面拦截所有非法操作
            event.setCancelled(true);
            if (event.getAction().name().contains("SHIFT") ||
                event.getAction().name().contains("HOTBAR") ||
                event.getAction().name().contains("SWAP") ||
                event.getClick().isKeyboardClick() ||
                event.getClick().isShiftClick()) {
                return;
            }
            if (event.getClickedInventory() != null && !event.getClickedInventory().equals(event.getInventory())) {
                return;
            }
            int slot = event.getRawSlot();
            // 总仓库菜单作物按钮点击处理
            if (title != null && title.contains("桃韵斋") && title.contains("总仓库")) {
                event.setCancelled(true);
                ItemStack clicked = inv.getItem(slot);
                if (clicked == null || clicked.getType() == Material.AIR) return;
                String display = FarmUtil.stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : null);
                for (CropConfig crop : Main.getInstance().getCrops()) {
                    if (crop.getMaterial() == clicked.getType() && FarmUtil.stripColor(crop.getDisplay()).equals(display)) {
                        openCropGUI(player, crop.getId(), 1);
                        return;
                    }
                }
                return;
            }
            // 下方按钮区
            if (slot >= 45 && slot <= 53) {
                if (slot == 45) {
                    FarmUtil.save(player, inv);
                    openMainGUI(player);
                    return;
                }
                if (slot == 53) {
                    player.closeInventory();
                    return;
                }
                // 翻页按钮
                if (slot == 47 || slot == 51) {
                    ItemStack clicked = inv.getItem(slot);
                    if (clicked != null && clicked.hasItemMeta()) {
                        String name = clicked.getItemMeta().getDisplayName();
                        int page = 1;
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("当前页数 (\\d+)").matcher(name);
                        if (m.find()) page = Integer.parseInt(m.group(1));
                        // 获取 cropId
                        ItemStack cropItem = inv.getItem(49);
                        String cropId = null;
                        if (cropItem != null && cropItem.hasItemMeta()) {
                            String display = FarmUtil.stripColor(cropItem.getItemMeta().getDisplayName());
                            for (CropConfig crop : Main.getInstance().getCrops()) {
                                if (crop.getMaterial() == cropItem.getType() && FarmUtil.stripColor(crop.getDisplay()).equals(display)) {
                                    cropId = crop.getId();
                                    break;
                                }
                            }
                        }
                        if (cropId == null) return;
                        if (slot == 47) {
                            if (event.isLeftClick()) page--;
                            if (event.isRightClick()) page = 1;
                        } else if (slot == 51) {
                            if (event.isLeftClick()) page++;
                            if (event.isRightClick()) page += 10;
                        }
                        openCropGUI(player, cropId, Math.max(1, page));
                        return;
                    }
                }
                // 作物信息按钮存取逻辑
                if (slot == 49) {
                    ItemStack clicked = inv.getItem(slot);
                    ItemStack cropItem = inv.getItem(49);
                    String cropId = null;
                    if (cropItem != null && cropItem.hasItemMeta()) {
                        String display = FarmUtil.stripColor(cropItem.getItemMeta().getDisplayName());
                        for (CropConfig crop : Main.getInstance().getCrops()) {
                            if (crop.getMaterial() == cropItem.getType() && FarmUtil.stripColor(crop.getDisplay()).equals(display)) {
                                cropId = crop.getId();
                                break;
                            }
                        }
                    }
                    if (cropId == null) return;
                    // 左键：取出当前页全部（背包满时只取出能放下的数量）
                    if (event.isLeftClick()) {
                        int current = cc.ranmc.farm.util.DataUtil.getPlayerData(player, true).getInt(cropId.toUpperCase(), 0);
                        if (current <= 0) {
                            player.sendMessage(color("&c你没有该作物！"));
                            return;
                        }
                        // 计算当前页可取数量
                        int maxStack = cropItem.getType().getMaxStackSize();
                        int pageMax = 45 * maxStack;
                        int takeCount = Math.min(current, pageMax);
                        // 计算背包可用空间
                        int freeSlots = 0;
                        org.bukkit.inventory.PlayerInventory invAll = player.getInventory();
                        for (int i = 0; i < 36; i++) {
                            ItemStack item = invAll.getItem(i);
                            if (item == null || item.getType() == Material.AIR) {
                                freeSlots++;
                            } else if (item.getType() == cropItem.getType() && item.getAmount() < maxStack) {
                                freeSlots += 0.5; // 半格视为可合并
                            }
                        }
                        int maxCanTake = freeSlots * maxStack;
                        int realTake = Math.min(takeCount, maxCanTake);
                        if (realTake <= 0) {
                            player.sendMessage(color("&c背包已满，无法取出更多作物！"));
                            return;
                        }
                        int left = realTake;
                        for (int i = 0; i < 45; i++) {
                            if (left <= 0) break;
                            int amount = Math.min(left, maxStack);
                            ItemStack outItem = new ItemStack(cropItem.getType(), amount);
                            player.getInventory().addItem(outItem);
                            left -= amount;
                        }
                        cc.ranmc.farm.util.DataUtil.setPlayerData(player, java.util.Collections.singletonMap(cropId.toUpperCase(), -realTake));
                        player.sendMessage(color("&a已取出 " + realTake + " 个作物！" + (realTake < takeCount ? " &e(背包已满，剩余作物留在仓库)" : "")));
                        openCropGUI(player, cropId, 1); // 刷新
                    }
                    // 右键存入
                    else if (event.isRightClick()) {
                        Material configMat = null;
                        for (CropConfig crop : Main.getInstance().getCrops()) {
                            if (crop.getId().equalsIgnoreCase(cropId)) {
                                configMat = crop.getMaterial();
                                break;
                            }
                        }
                        int totalAdd = 0;
                        org.bukkit.inventory.PlayerInventory invAll = player.getInventory();
                        for (int i = 0; i < 36; i++) {
                            ItemStack item = invAll.getItem(i);
                            if (item != null && item.getType() != Material.AIR && configMat != null && item.getType().name().equalsIgnoreCase(configMat.name())) {
                                totalAdd += item.getAmount();
                                invAll.setItem(i, null);
                            }
                        }
                        if (totalAdd > 0) {
                            cc.ranmc.farm.util.DataUtil.setPlayerData(player, java.util.Collections.singletonMap(cropId.toUpperCase(), totalAdd));
                            player.sendMessage(color("&a已存入 " + totalAdd + " 个作物！"));
                            openCropGUI(player, cropId, 1); // 刷新
                        } else {
                            player.sendMessage(color("&c背包中没有可存入的同类作物！"));
                        }
                    }
                    return;
                }
                return;
            }
            // 只允许自定义交互：左键/右键点击仓库物品区
            if (slot >= 0 && slot < 45) {
                ItemStack clicked = inv.getItem(slot);
                ItemStack cropItem = inv.getItem(49);
                String cropId = null;
                if (cropItem != null && cropItem.hasItemMeta()) {
                    String display = FarmUtil.stripColor(cropItem.getItemMeta().getDisplayName());
                    for (CropConfig crop : Main.getInstance().getCrops()) {
                        if (crop.getMaterial() == cropItem.getType() && FarmUtil.stripColor(crop.getDisplay()).equals(display)) {
                            cropId = crop.getId();
                            break;
                        }
                    }
                }
                if (cropId == null) return;
                // 只允许左键单独取出该格物品
                if (event.isLeftClick()) {
                    int current = cc.ranmc.farm.util.DataUtil.getPlayerData(player, true).getInt(cropId.toUpperCase(), 0);
                    if (current <= 0) {
                        player.sendMessage(color("&c你没有该作物！"));
                        return;
                    }
                    if (clicked != null && clicked.getType() != Material.AIR) {
                        int take = clicked.getAmount();
                        player.getInventory().addItem(clicked.clone());
                        // 数据库扣减
                        cc.ranmc.farm.util.DataUtil.setPlayerData(player, java.util.Collections.singletonMap(cropId.toUpperCase(), -take));
                        inv.setItem(slot, new ItemStack(Material.AIR));
                        player.sendMessage(color("&a已取出作物！"));
                        openCropGUI(player, cropId, 1); // 刷新
                    }
                }
                // 禁止物品区点击时存入
                return;
            }
            return;
        }
    }

    /**
     * 储存掉落物
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItemEvent(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        List<Item> items = event.getItems();
        if (items.isEmpty()) return;
        // 支持 config.yml 自定义作物类型
        List<Material> configCrops = new ArrayList<>();
        for (FarmUtil.CropConfig crop : Main.getInstance().getCrops()) {
            configCrops.add(crop.getMaterial());
        }
        Map<String, Integer> updateMap = new HashMap<>();
        boolean hasCrop = false;
        for (Item value : new ArrayList<>(items)) { // 防止并发修改
            ItemStack item = value.getItemStack();
            if (configCrops.contains(item.getType())) {
                hasCrop = true;
                String type = item.getType().toString().toUpperCase();
                updateMap.put(type, updateMap.getOrDefault(type, 0) + item.getAmount());
                value.remove(); // 移除掉落物
            }
        }
        if (hasCrop) {
            DataUtil.setPlayerData(player, updateMap);
            long now = System.currentTimeMillis();
            UUID uuid = player.getUniqueId();
            Long last = lastTipTime.get(uuid);
            if (last == null || now - last > 3000) { // 3秒=3000毫秒
                player.sendMessage(color("&b桃韵斋>>> &a作物已自动存入仓库！"));
                lastTipTime.put(uuid, now);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title != null && title.contains("桃韵斋") && title.contains("仓库")) {
            event.setCancelled(true);
        }
    }
}

package cc.ranmc.farm.listener;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.config.ConfigManager;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.DataUtil;
import cc.ranmc.farm.util.FarmUtil;
// import cc.ranmc.utils.MenuUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cc.ranmc.farm.constant.FarmConstant.CROP_TYPE;
import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.getInventoryAirCount;
import static cc.ranmc.farm.util.FarmUtil.openCropGUI;

public class FarmListener implements Listener {

    private static final Main plugin = Main.getInstance();
    // 提示
    private final List<String> noteList = new ArrayList<>();

    /**
     * 菜单关闭
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        ConfigManager config = ConfigManager.getInstance();
        if (!event.getView().getTitle().equals(color(config.getGuiTitle()))) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        FarmUtil.save(player, inventory);
    }

    /**
     * 菜单点击
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ConfigManager config = ConfigManager.getInstance();
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (event.getView().getTitle().equals(color(config.getGuiTitle()))) {
            Inventory inventory = event.getClickedInventory();
            if (inventory == null) return;
            if (event.getRawSlot() >= config.getItemsPerPage() &&
                    inventory != player.getInventory()) {
                event.setCancelled(true);
            }
            if (clicked == null &&
                    inventory == player.getInventory()) {
                return;
            }
            if (event.getRawSlot() == config.getButtonSlot("info")) {

                int airCount = getInventoryAirCount(player);

                for (int i = 0; i < config.getItemsPerPage(); i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null) {
                        if (airCount == 0) {
                            break;
                        }
                        inventory.setItem(i, new ItemStack(Material.AIR));
                        player.getInventory().addItem(item);
                        airCount--;
                    }
                }
                player.closeInventory();
                return;
            }

            if (event.getRawSlot() == config.getButtonSlot("close")) {
                player.closeInventory();
                return;
            }
            if (event.getRawSlot() == config.getButtonSlot("back")) {
                FarmUtil.save(player, inventory);
                if (Main.getInstance().isRanmc()) {
                    // MenuUtil.open(player, "farm");
                    player.closeInventory();
                } else player.closeInventory();
                return;
            }
            ItemStack item = inventory.getItem(config.getButtonSlot("info"));
            if (item == null || item.getType() == Material.AIR) return;
            String copType = item.getType().toString();
            if (event.getRawSlot() == config.getButtonSlot("previous_page")) {
                int page = Integer.parseInt(Objects.requireNonNull(clicked).getItemMeta().getDisplayName().split(" ")[1]);
                if (event.getClick().isLeftClick()) page--;
                if (event.getClick().isRightClick()) page = 1;
                FarmUtil.save(player, inventory);
                openCropGUI(player, copType, page);
                return;
            }
            if (event.getRawSlot() == config.getButtonSlot("next_page")) {
                int page = Integer.parseInt(Objects.requireNonNull(clicked).getItemMeta().getDisplayName().split(" ")[1]);
                if (event.getClick().isLeftClick()) page++;
                if (event.getClick().isRightClick()) page += 10;
                FarmUtil.save(player, inventory);
                openCropGUI(player, copType, page);
            }
        }

    }

    /**
     * 储存掉落物
     * @param event 事件
     */
    @EventHandler
    public void onBlockDropItemEvent(BlockDropItemEvent event) {
        ConfigManager config = ConfigManager.getInstance();
        
        Player player = event.getPlayer();
        boolean isCrop = true;
        List<Item> items = event.getItems();
        if (items.isEmpty()) return;
        for (Item value : items) {
            ItemStack item = value.getItemStack();
            // 使用NBT检查确保是纯净的农作物
            if (!CROP_TYPE.contains(item.getType()) || !FarmUtil.isValidCrop(item, item.getType())) {
                isCrop = false;
            }
        }
        if (isCrop) {
            SQLRow playerRow = DataUtil.getPlayerData(player);
            if (!playerRow.getBoolean(SQLKey.OPEN, config.getDefaultOpen())) return;
            
            // 检查是否启用自动收集
            if (!config.isAutoCollectEnabled()) return;
            
            Map<String,Integer> updateMap = new HashMap<>();
            for (Item value : items) {
                ItemStack item = value.getItemStack();
                String type = item.getType().toString().toUpperCase();
                updateMap.put(type,
                        updateMap.getOrDefault(type, playerRow.getInt(type, 0))
                         + item.getAmount());
            }
            DataUtil.setPlayerData(player, updateMap);
            
            // 检查是否显示收集消息
            if (config.isShowCollectMessage()) {
                Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
                    noteList.remove(player.getName());
                    if (!noteList.contains(player.getName())) {
                        player.sendMessage(color(config.getSuccessMessage("crop_stored")));
                    }
                }, 80);
                noteList.add(player.getName());
            }
            event.setCancelled(true);
        }
    }
}

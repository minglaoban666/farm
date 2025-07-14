package cc.ranmc.farm.constant;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

import static cc.ranmc.farm.util.FarmUtil.getItem;

public class FarmConstant {

    // 板
    public static final ItemStack PANE = getItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
    // 收集掉落物的农作物列表
    public static final List<Material> CROP_TYPE = Arrays.asList(
            Material.WHEAT,
            Material.WHEAT_SEEDS,
            Material.POTATO,
            Material.CARROT,
            Material.BEETROOT,
            Material.BEETROOT_SEEDS,
            Material.PUMPKIN,
            Material.MELON_SLICE,
            Material.CACTUS,
            Material.BAMBOO,
            Material.SUGAR_CANE,
            Material.NETHER_WART
    );

}

package cc.ranmc.farm.papi;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.DataUtil;
// import cc.ranmc.papi.bean.PapiHandler;
import org.bukkit.entity.Player;

// public class RanmcPapi extends PapiHandler {
public class RanmcPapi {

    private final Main plugin = Main.getInstance();

    public RanmcPapi(String prefix) {
        // super(prefix);
    }

    /*
    @Override
    public String handle(Player player, String identifier) {
        if (player == null || !player.isOnline()) return "&c目标错误";
        SQLRow playerRow = DataUtil.getPlayerData(player);
        return String.valueOf(playerRow.getInt(identifier.toUpperCase(), 0));
    }
    */
}

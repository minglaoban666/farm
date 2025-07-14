package cc.ranmc.farm.util;

import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.sql.Database;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DataUtil {

    private static Database data = null;
    private static final Map<String, SQLRow> playerData = new HashMap<>();

    public static void init() {
        if (data == null) {
            data = new Database();
        }
    }

    public static void close() {
        if (data != null) {
            data.close();
        }
    }

    public static SQLRow getPlayerData(String playerName) {
        if (data == null) {
            init();
        }
        if (data == null) {
            // 如果数据库初始化失败，返回空数据
            SQLRow emptyRow = new SQLRow();
            emptyRow.set(SQLKey.PLAYER, playerName);
            return emptyRow;
        }
        
        if (playerData.containsKey(playerName)) {
            return playerData.get(playerName);
        }
        SQLRow playerRow = data.selectMap(SQLKey.PLAYER,
                new SQLFilter().where(SQLKey.PLAYER, playerName));
        if (playerRow.isEmpty()) {
            SQLRow parms = new SQLRow();
            parms.set(SQLKey.PLAYER, playerName);
            data.insert(SQLKey.PLAYER, parms);
            playerRow.set(SQLKey.ID, data.insert(SQLKey.PLAYER, parms));
        }
        playerData.put(playerName, playerRow);
        return playerRow;
    }

    public static SQLRow getPlayerData(Player player) {
        return getPlayerData(player.getName());
    }

    public static void setPlayerData(String playerName, Cop cop, int total) {
        if (data == null) {
            init();
        }
        if (data == null) {
            return; // 数据库不可用，跳过
        }
        
        SQLRow playerRow = DataUtil.getPlayerData(playerName);
        String type = cop.getMaterial().toString().toUpperCase();
        data.update(SQLKey.PLAYER,
                new SQLFilter()
                        .set(type, total)
                        .where(playerRow.getInt(SQLKey.ID)));
        playerRow.set(type, total);
        playerData.put(playerName, playerRow);
    }

    public static void setPlayerData(Player player, Cop cop, int total) {
        setPlayerData(player.getName(), cop, total);
    }

    public static void setPlayerData(String playerName, Map<String,Integer> copMap) {
        if (data == null) {
            init();
        }
        if (data == null) {
            return; // 数据库不可用，跳过
        }
        
        SQLRow playerRow = DataUtil.getPlayerData(playerName);
        SQLFilter filter = new SQLFilter();
        boolean first = true;
        for (String type : copMap.keySet()) {
            int total = copMap.get(type);
            if (first) {
                filter.set(type, total);
                first = false;
            } else {
                filter.andSet(type, total);
            }
            playerRow.set(type, total);
        }
        data.update(SQLKey.PLAYER, filter.where(playerRow.getInt(SQLKey.ID)));
        playerData.put(playerName, playerRow);
    }

    public static void setPlayerData(Player player, Map<String,Integer> copMap) {
        setPlayerData(player.getName(), copMap);
    }

    public static void setPlayerOpen(String playerName, boolean open) {
        if (data == null) {
            init();
        }
        if (data == null) {
            return; // 数据库不可用，跳过
        }
        
        SQLRow playerRow = DataUtil.getPlayerData(playerName);
        data.update(SQLKey.PLAYER,
                new SQLFilter()
                        .set(SQLKey.OPEN, open)
                        .where(playerRow.getInt(SQLKey.ID)));
        playerRow.set(SQLKey.OPEN, open);
        playerData.put(playerName, playerRow);
    }

    public static void setPlayerOpen(Player player, boolean open) {
        setPlayerOpen(player.getName(), open);
    }

}

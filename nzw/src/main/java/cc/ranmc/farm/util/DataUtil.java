package cc.ranmc.farm.util;

import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.sql.Database;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import cc.ranmc.farm.Main;

import java.util.HashMap;
import java.util.Map;

public class DataUtil {

    private static Database getDatabase() {
        return cc.ranmc.farm.Main.getDatabase();
    }
    private static final Map<String, SQLRow> playerData = new HashMap<>();

    public static void close() {
        getDatabase().close();
    }

    public static SQLRow getPlayerData(String playerName, boolean forceReload) {
        if (!forceReload && playerData.containsKey(playerName)) {
            return playerData.get(playerName);
        }
        SQLRow playerRow = getDatabase().selectMap(SQLKey.TABLE,
                new SQLFilter().where(SQLKey.PLAYER, playerName));
        if (playerRow.isEmpty()) {
            SQLRow parms = new SQLRow();
            parms.set(SQLKey.PLAYER, playerName);
            getDatabase().insert(SQLKey.TABLE, parms);
            playerRow.set(SQLKey.ID, getDatabase().insert(SQLKey.TABLE, parms));
        }
        playerData.put(playerName, playerRow);
        return playerRow;
    }

    public static SQLRow getPlayerData(String playerName) {
        return getPlayerData(playerName, false);
    }

    public static SQLRow getPlayerData(Player player, boolean forceReload) {
        return getPlayerData(player.getName(), forceReload);
    }

    public static SQLRow getPlayerData(Player player) {
        return getPlayerData(player.getName(), false);
    }

    public static void setPlayerData(String playerName, Cop cop, int total) {
        SQLRow playerRow = DataUtil.getPlayerData(playerName);
        String type = cop.getMaterial().toString().toUpperCase();
        getDatabase().update(SQLKey.TABLE,
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
        SQLRow playerRow = DataUtil.getPlayerData(playerName);
        SQLFilter filter = new SQLFilter();
        for (String type : copMap.keySet()) {
            int add = copMap.get(type);
            int old = playerRow.getInt(type, 0);
            int total = old + add;
            filter.set(type, total);
            playerRow.set(type, total);
        }
        getDatabase().update(SQLKey.TABLE, filter.where(SQLKey.PLAYER, playerName));
        playerData.put(playerName, playerRow);
    }

    public static void setPlayerData(Player player, Map<String,Integer> copMap) {
        setPlayerData(player.getName(), copMap);
    }

    public static void setPlayerOpen(String playerName, boolean open) {
        SQLRow playerRow = DataUtil.getPlayerData(playerName);
        getDatabase().update(SQLKey.TABLE,
                new SQLFilter()
                        .set(SQLKey.OPEN, open)
                        .where(playerRow.getInt(SQLKey.ID)));
        playerRow.set(SQLKey.OPEN, open);
        playerData.put(playerName, playerRow);
    }

    public static void setPlayerOpen(Player player, boolean open) {
        setPlayerOpen(player.getName(), open);
    }

    // 新增：异步写入玩家数据
    public static void setPlayerDataAsync(Player player, Map<String,Integer> copMap, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            setPlayerData(player, copMap);
            if (callback != null) {
                Bukkit.getScheduler().runTask(Main.getInstance(), callback);
            }
        });
    }

    // 新增：异步读取玩家数据
    public static void getPlayerDataAsync(Player player, boolean forceReload, java.util.function.Consumer<SQLRow> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            SQLRow row = getPlayerData(player, forceReload);
            if (callback != null) {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(row));
            }
        });
    }

}

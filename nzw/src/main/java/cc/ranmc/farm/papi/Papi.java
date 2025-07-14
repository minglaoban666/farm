package cc.ranmc.farm.papi;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.DataUtil;
import org.bukkit.OfflinePlayer;

// import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

// public class Papi extends PlaceholderExpansion {
public class Papi {
    
	private final Main plugin = Main.getInstance();

    public Papi() {}
	
    /*
    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getAuthor(){
        return "Ranica";
    }

    @Override
    public @NotNull String getIdentifier(){
        return "fm";
    }

    @Override
    public @NotNull String getVersion(){
        return "Beta";
    }
  
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier){
    	
        if (player == null || !player.isOnline()) return "&c目标错误";
        SQLRow playerRow = DataUtil.getPlayerData(player.getName());
        return String.valueOf(playerRow.getInt(identifier.toUpperCase(), 0));
    }
    */
}
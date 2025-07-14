package cc.ranmc.farm.command;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.config.ConfigManager;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.DataUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.openCropGUI;
import static cc.ranmc.farm.util.FarmUtil.print;

public class FarmCommand implements CommandExecutor {

    private static final Main plugin = Main.getInstance();
    /**
     * 指令控制
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        ConfigManager config = ConfigManager.getInstance();
        
        if (args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission(config.getPermission("admin"))) {
                    config.loadConfig();
                    sender.sendMessage(color(config.getSuccessMessage("reload")));
                } else {
                    sender.sendMessage(color(config.getErrorMessage("no_permission")));
                }
                return true;
            }

            if (!(sender instanceof Player player)) {
                print(color(config.getErrorMessage("player_only")));
                return true;
            }

            if (!sender.hasPermission(config.getPermission("user"))) {
                player.sendMessage(color(config.getErrorMessage("no_permission")));
                return true;
            }

            if (args[0].equalsIgnoreCase("switch")) {
                SQLRow playerRow = DataUtil.getPlayerData(player);
                boolean open = playerRow.getBoolean(SQLKey.OPEN, config.getDefaultOpen());
                DataUtil.setPlayerOpen(player, !open);
                player.sendMessage(color(open ? config.getSuccessMessage("switch_off") : config.getSuccessMessage("switch_on")));
                return true;
            }

            openCropGUI(player, args[0], 1);
            return true;
        }

        sender.sendMessage(color(config.getErrorMessage("unknown_command")));
        return true;
    }
}

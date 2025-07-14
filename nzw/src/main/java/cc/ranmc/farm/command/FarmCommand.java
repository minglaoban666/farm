package cc.ranmc.farm.command;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLRow;
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
import static cc.ranmc.farm.util.FarmUtil.openMainGUI;

public class FarmCommand implements CommandExecutor {

    private static final Main plugin = Main.getInstance();
    /**
     * 指令控制
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){

        if (args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("ck.admin")) {
                    plugin.loadConfig();
                    sender.sendMessage(color("&b[作物仓库] &a重载完成"));
                } else {
                    sender.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                }
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&b[作物仓库] &c该指令不能在控制台输入"));
                return true;
            }

            if (!sender.hasPermission("ck.user")) {
                player.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                return true;
            }

            if (args[0].equalsIgnoreCase("switch")) {
                SQLRow playerRow = DataUtil.getPlayerData(player);
                boolean open = playerRow.getBoolean(SQLKey.OPEN, true);
                DataUtil.setPlayerOpen(player, !open);
                player.sendMessage(color("&b桃韵斋>>>&e你已" + (open ? "关闭" : "打开") + "作物仓库"));
                return true;
            }

            openCropGUI(player, args[0], 1);
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&b[作物仓库] &c该指令不能在控制台输入"));
                return true;
            }
            if (!sender.hasPermission("ck.user")) {
                player.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                return true;
            }
            openMainGUI(player);
            return true;
        }

        sender.sendMessage("§c未知指令");
        return true;
    }
}

package cc.ranmc.farm.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FarmAutoComplete implements TabCompleter {
    /**
     * 命令补全
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("fm.user")) {
                list.add("switch");
            }
            if (sender.hasPermission("fm.admin")) {
                list.add("reload");
            }

            return list;
        }
        return new ArrayList<>();
    }
}

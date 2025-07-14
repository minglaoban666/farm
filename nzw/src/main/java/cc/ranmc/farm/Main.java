package cc.ranmc.farm;

import java.util.Objects;

import cc.ranmc.farm.command.FarmAutoComplete;
import cc.ranmc.farm.command.FarmCommand;
import cc.ranmc.farm.command.NBTCommand;
import cc.ranmc.farm.config.ConfigManager;
import cc.ranmc.farm.listener.FarmListener;
// import cc.ranmc.farm.papi.Papi;
// import cc.ranmc.farm.papi.RanmcPapi;
import cc.ranmc.farm.sql.Database;
import cc.ranmc.farm.util.DataUtil;
// import cc.ranmc.papi.PapiAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.print;
// import static jdk.tools.jlink.internal.plugins.PluginsResourceBundle.getDescription;

public class Main extends JavaPlugin implements Listener {

	@Getter
	private static Main instance;
	@Getter
	private boolean ranmc = false;
	
	/**
	 * 插件启动
	 */
	@Override
	public void onEnable() {
		instance = this;
		color("§e-----------------------");
		color("§bFarmInventory §dBy xuanming");
		color("§bVersion: 4.5");
		color("§e-----------------------");
	    
	    // 注册 Event
        Bukkit.getPluginManager().registerEvents(new FarmListener(), this);

		// 注册指令
		Objects.requireNonNull(Bukkit.getPluginCommand("farm")).setExecutor(new FarmCommand());
		Objects.requireNonNull(Bukkit.getPluginCommand("fm")).setExecutor(new FarmCommand());
		Objects.requireNonNull(Bukkit.getPluginCommand("farm")).setTabCompleter(new FarmAutoComplete());
		Objects.requireNonNull(Bukkit.getPluginCommand("fm")).setTabCompleter(new FarmAutoComplete());
		
		// 注册NBT测试指令
		Objects.requireNonNull(Bukkit.getPluginCommand("nbt")).setExecutor(new NBTCommand());

		// 初始化配置管理器
		ConfigManager.getInstance();
		
		// 初始化数据库
		DataUtil.init();
		
		// 加载配置
		loadConfig();
        
		super.onEnable();
	}
	
	/**
	 * 插件卸载
	 */
	@Override
	public void onDisable() {
		DataUtil.close();
		print("&b[作物仓库] §a已经成功卸载");
		super.onDisable();
	}
	
	/**
	 * 加载配置
	 */
	public void loadConfig() {

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			// new Papi().register();
			print("&b[作物仓库] §a成功载入PlaceholderAPI");
		}

		if (Bukkit.getPluginManager().getPlugin("桃韵斋") != null) {
			// PapiAPI.registerHandler(new RanmcPapi("fm"));
			print("&b[作物仓库] §a成功载入桃韵斋");
			ranmc = true;
		}
	}
	
}

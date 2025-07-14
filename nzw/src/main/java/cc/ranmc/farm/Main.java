package cc.ranmc.farm;

import java.util.Objects;

import cc.ranmc.farm.command.FarmAutoComplete;
import cc.ranmc.farm.command.FarmCommand;
import cc.ranmc.farm.listener.FarmListener;
import cc.ranmc.farm.sql.Database;
import cc.ranmc.farm.util.DataUtil;
import cc.ranmc.farm.util.FarmUtil;
import cc.ranmc.farm.util.FarmUtil.CropConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.List;

public class Main extends JavaPlugin implements Listener {

	private static Main instance;
	@Getter
	private boolean ranmc = false;
	private List<CropConfig> crops;
	// 新增 Database 实例
	private Database database;

	public static Main getInstance() { return instance; }
	public static Database getDatabase() { return getInstance().database; }
	public List<CropConfig> getCrops() { return crops; }
	
	/**
	 * 插件启动
	 */
	@Override
	public void onEnable() {
		instance = this;
		// 只保留插件启动必要提示
		Bukkit.getConsoleSender().sendMessage("§bVersion: " + getDescription().getVersion());
		Bukkit.getConsoleSender().sendMessage("§c桃韵斋-农作物仓库已加载");
        
	    // 初始化 Database，参数从 config.yml 读取
        String host = getConfig().getString("database.host", "localhost");
        int port = getConfig().getInt("database.port", 3306);
        String db = getConfig().getString("database.name", "tyz");
        String user = getConfig().getString("database.user", "tyz");
        String password = getConfig().getString("database.password", "wm123456");
        this.database = new Database(host, port, db, user, password);

		// 注册 Event
        Bukkit.getPluginManager().registerEvents(new FarmListener(), this);

		// 注册指令
		Objects.requireNonNull(Bukkit.getPluginCommand("ck")).setExecutor(new FarmCommand());
		Objects.requireNonNull(Bukkit.getPluginCommand("ck")).setTabCompleter(new FarmAutoComplete());

		saveDefaultConfig(); // 自动生成 config.yml（如不存在）
		loadConfig();
        
		super.onEnable();
	}
	
	/**
	 * 插件卸载
	 */
	@Override
	public void onDisable() {
		DataUtil.close();
		// 只保留卸载提示
		Bukkit.getConsoleSender().sendMessage("&b[作物仓库] §a已经成功卸载");
		super.onDisable();
	}
	
	/**
	 * 加载配置
	 */
	public void loadConfig() {
		reloadConfig(); // 重新加载 config.yml
		ensureTableExists();
		this.crops = FarmUtil.loadCropsFromConfig(this);
		syncCropColumnsToDatabase();

		if (Bukkit.getPluginManager().getPlugin("桃韵斋") != null) {
			// 只保留成功载入提示
			Bukkit.getConsoleSender().sendMessage("&b[作物仓库] §a成功载入桃韵斋");
			ranmc = true;
		}
	}
	
	private void ensureTableExists() {
		try (Connection conn = getDatabaseConnection()) {
			String sql = "CREATE TABLE IF NOT EXISTS THCK (" +
					"ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
					"PLAYER VARCHAR(255) NOT NULL" +
					")";
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(sql);
			}
			// 检查并添加 OPEN 字段
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rs = meta.getColumns(null, null, "THCK", "OPEN");
			if (!rs.next()) {
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE THCK ADD COLUMN OPEN TINYINT(1) DEFAULT 1");
				}
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage("&c[作物仓库] 自动建表/加字段失败: " + e.getMessage());
		}
	}
	
	private void syncCropColumnsToDatabase() {
        try (Connection conn = getDatabaseConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "THck", null);
            java.util.Set<String> existing = new java.util.HashSet<>();
            while (rs.next()) {
                existing.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            for (CropConfig crop : crops) {
                String col = crop.getId().toUpperCase();
                if (!existing.contains(col.toLowerCase())) {
                    // 检查字段名是否合法
                    if (!col.matches("^[A-Z0-9_]+$")) continue;
                    String sql = "ALTER TABLE THck ADD COLUMN `" + col + "` INT DEFAULT 0";
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(sql);
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage("&c[作物仓库] 自动添加字段失败: " + col + " 错误: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("&c[作物仓库] 同步字段失败: " + e.getMessage());
        }
    }

    private Connection getDatabaseConnection() throws Exception {
        String host = getConfig().getString("database.host", "localhost");
        int port = getConfig().getInt("database.port", 3306);
        String db = getConfig().getString("database.name", "tyz");
        String user = getConfig().getString("database.user", "tyz");
        String password = getConfig().getString("database.password", "wm123456");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return java.sql.DriverManager.getConnection(url, user, password);
    }
}
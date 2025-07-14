package cc.ranmc.farm.config;

import cc.ranmc.farm.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 配置管理类
 * 负责读取和管理插件配置
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Main plugin;
    private FileConfiguration config;

    private ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager(Main.getInstance());
        }
        return instance;
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 保存默认配置文件
        plugin.saveDefaultConfig();
        
        // 重新加载配置
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * 获取数据库类型
     */
    public String getDatabaseType() {
        return config.getString("database.type", "h2");
    }

    /**
     * 获取H2数据库文件路径
     */
    public String getH2DatabaseFile() {
        return config.getString("database.h2.file", "data");
    }

    /**
     * 获取H2数据库用户名
     */
    public String getH2Username() {
        return config.getString("database.h2.username", "sa");
    }

    /**
     * 获取H2数据库密码
     */
    public String getH2Password() {
        return config.getString("database.h2.password", "");
    }

    /**
     * 是否自动创建表
     */
    public boolean isAutoCreateTables() {
        return config.getBoolean("database.h2.auto_create_tables", true);
    }

    /**
     * 获取MySQL主机
     */
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    /**
     * 获取MySQL端口
     */
    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    /**
     * 获取MySQL数据库名
     */
    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "farm_inventory");
    }

    /**
     * 获取MySQL用户名
     */
    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    /**
     * 获取MySQL密码
     */
    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }

    /**
     * 获取插件前缀
     */
    public String getPrefix() {
        return config.getString("settings.prefix", "&b[作物仓库]");
    }

    /**
     * 是否启用自动收集
     */
    public boolean isAutoCollectEnabled() {
        return config.getBoolean("settings.auto_collect", true);
    }

    /**
     * 是否显示收集消息
     */
    public boolean isShowCollectMessage() {
        return config.getBoolean("settings.show_collect_message", true);
    }

    /**
     * 获取默认开关状态
     */
    public boolean getDefaultOpen() {
        return config.getBoolean("settings.default_open", true);
    }

    /**
     * 获取普通用户最大页数
     */
    public int getMaxPagesNormal() {
        return config.getInt("settings.max_pages.normal", 20);
    }

    /**
     * 获取VIP最大页数
     */
    public int getMaxPagesVip() {
        return config.getInt("settings.max_pages.vip", 30);
    }

    /**
     * 获取SVIP最大页数
     */
    public int getMaxPagesSvip() {
        return config.getInt("settings.max_pages.svip", 50);
    }

    /**
     * 获取启用的农作物列表
     */
    public List<String> getEnabledCrops() {
        return config.getStringList("crops.enabled");
    }

    /**
     * 获取农作物显示名称
     */
    public String getCropName(String cropType) {
        return config.getString("crops.names." + cropType, cropType);
    }

    /**
     * 获取GUI标题
     */
    public String getGuiTitle() {
        return config.getString("gui.title", "&d&l桃韵斋丨作物仓库");
    }

    /**
     * 获取每页物品数量
     */
    public int getItemsPerPage() {
        return config.getInt("gui.items_per_page", 45);
    }

    /**
     * 获取GUI大小
     */
    public int getGuiSize() {
        return config.getInt("gui.size", 54);
    }

    /**
     * 获取消息前缀
     */
    public String getMessagePrefix() {
        return config.getString("messages.prefix", "&b桃韵斋>>>");
    }

    /**
     * 获取成功消息
     */
    public String getSuccessMessage(String key) {
        return config.getString("messages.success." + key, "&a操作成功");
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage(String key) {
        return config.getString("messages.error." + key, "&c操作失败");
    }

    /**
     * 获取信息消息
     */
    public String getInfoMessage(String key) {
        return config.getString("messages.info." + key, "&b信息");
    }

    /**
     * 获取权限节点
     */
    public String getPermission(String key) {
        return config.getString("permissions." + key, "");
    }

    /**
     * 是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }

    /**
     * 是否显示详细错误信息
     */
    public boolean isShowDetailedErrors() {
        return config.getBoolean("debug.show_detailed_errors", true);
    }

    /**
     * 是否记录数据库操作
     */
    public boolean isLogDatabaseOperations() {
        return config.getBoolean("debug.log_database_operations", false);
    }

    /**
     * 获取按钮配置
     */
    public String getButtonConfig(String button, String key) {
        return config.getString("gui.buttons." + button + "." + key, "");
    }

    /**
     * 获取按钮槽位
     */
    public int getButtonSlot(String button) {
        return config.getInt("gui.buttons." + button + ".slot", 0);
    }

    /**
     * 获取按钮材料
     */
    public String getButtonMaterial(String button) {
        return config.getString("gui.buttons." + button + ".material", "STONE");
    }

    /**
     * 获取按钮名称
     */
    public String getButtonName(String button) {
        return config.getString("gui.buttons." + button + ".name", "");
    }

    /**
     * 获取按钮描述
     */
    public List<String> getButtonLore(String button) {
        return config.getStringList("gui.buttons." + button + ".lore");
    }
} 
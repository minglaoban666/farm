package cc.ranmc.farm.sql;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.FarmUtil;
import cc.ranmc.farm.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static cc.ranmc.farm.util.FarmUtil.print;

public class Database {

    private Connection connection;

    public Database() {
        try {
            Class.forName("org.h2.Driver");
            Main plugin = Main.getInstance();
            if (plugin != null && plugin.getDataFolder() != null) {
                ConfigManager config = ConfigManager.getInstance();
                String dbFile = config.getH2DatabaseFile();
                String username = config.getH2Username();
                String password = config.getH2Password();
                
                String dbPath = plugin.getDataFolder().getPath() + "/" + dbFile;
                print("数据库路径: " + dbPath);
                print("数据库用户名: " + username);
                
                connection = DriverManager.getConnection("jdbc:h2:./" + dbPath, username, password);
                print("数据库连接成功");
                
                if (config.isAutoCreateTables()) {
                    createTable();
                }
            } else {
                print("数据库错误: 插件实例或数据文件夹为空");
                if (plugin == null) {
                    print("插件实例为空");
                }
                if (plugin != null && plugin.getDataFolder() == null) {
                    print("数据文件夹为空");
                }
            }
        } catch (Exception e) {
            print("数据库错误: " + e.getMessage());
            if (ConfigManager.getInstance().isShowDetailedErrors()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (Exception e) {
            print("数据库错误" + e.getMessage());
        }

    }

    /**
     * 新增数据库表
     */
    public void createTable() {
        runCommand("CREATE TABLE PLAYER " +
                "(ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " PLAYER TEXT NOT NULL," +
                " OPEN BOOLEAN," +
                " CARROT INTEGER," +
                " WHEAT INTEGER," +
                " WHEAT_SEEDS INTEGER," +
                " BEETROOT INTEGER," +
                " BEETROOT_SEEDS INTEGER," +
                " NETHER_WART INTEGER," +
                " PUMPKIN INTEGER," +
                " MELON INTEGER," +
                " CACTUS INTEGER," +
                " BAMBOO INTEGER," +
                " SUGAR_CANE INTEGER," +
                " POTATO INTEGER)");
    }

    /**
     * 新增数据库
     * @param table 表
     * @param data 内容
     */
    public int insert(String table, SQLRow data) {
        if (connection == null) {
            print("数据库错误: 连接为空，无法执行插入操作");
            return -1;
        }
        
        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for (String key : data.keySet()) {
            name.append(key);
            name.append(",");
            value.append("?,");
        }
        if (!name.isEmpty()) name.deleteCharAt(name.length() - 1);
        if (!value.isEmpty()) value.deleteCharAt(value.length() - 1);
        String command = "INSERT INTO " + table.toUpperCase() + " ("+name+") VALUES (" + value + ");";
        try {
            PreparedStatement statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (String key : data.keySet()) {
                statement.setObject(i, data.getObject(key));
                i++;
            }
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        }
        return -1;
    }

    /**
     * 查询表数据
     * @param table 表
     * @param filter 数据
     * @return 数据
     */
    public SQLRow selectMap(String table, SQLFilter filter) {
        return queryMap("SELECT * FROM " + table.toUpperCase() + filter.getResult());
    }

    public SQLRow selectMap(String table) {
        return queryMap("SELECT * FROM " + table.toUpperCase());
    }

    public List<SQLRow> selectList(String table, SQLFilter filter) {
        return queryList("SELECT * FROM " + table.toUpperCase() + filter.getResult());
    }

    public List<SQLRow> selectList(String table) {
        return queryList("SELECT * FROM " + table.toUpperCase());
    }

    public int selectCount(String table) {
        return queryMap("SELECT COUNT(*) FROM " + table)
                .getInt("COUNT(*)", 0);
    }

    public int selectCount(String table, SQLFilter filter) {
        return queryMap("SELECT COUNT(*) FROM " + table.toUpperCase() + filter.getResult())
                .getInt("COUNT(*)", 0);
    }

    /**
     * 分析数据
     * @param command 命令
     * @return 数据
     */
    protected SQLRow queryMap(String command) {
        SQLRow data = new SQLRow();
        if (connection == null) {
            print("数据库错误: 连接为空，无法执行查询操作");
            return data;
        }
        
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(command);
            if (rs.next()) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        data.set(md.getColumnName(i), rs.getObject(i));
                    }
                }
            }
        } catch (Exception e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
        return data;
    }

    protected List<SQLRow> queryList(String command) {
        List<SQLRow> list = new ArrayList<>();
        if (connection == null) {
            print("数据库错误: 连接为空，无法执行查询操作");
            return list;
        }
        
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(command);
            while (rs.next()) {
                if (!rs.isClosed()) {
                    SQLRow data = new SQLRow();
                    ResultSetMetaData md = rs.getMetaData();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        if (rs.getString(i) != null) {
                            data.set(md.getColumnName(i), rs.getObject(i));
                        }
                    }
                    list.add(data);
                }
            }
        } catch (Exception e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
        return list;
    }

    /**
     * 更新表数据
     * @param table 表
     * @param filter 数据
     */
    public void update(String table, SQLFilter filter) {
        runCommand("UPDATE " + table.toUpperCase() + filter.getResult());
    }

    /**
     * 删除表数据
     * @param table 表
     * @param id 编号
     */
    public void delete(String table, String id) {
        runCommand("DELETE FROM " + table.toUpperCase() + " WHERE ID = " + id);
    }

    public void delete(String table, SQLFilter filter) {
        runCommand("DELETE FROM " + table.toUpperCase() + filter.getResult());
    }

    /**
     * 执行数据库指令
     * @param command 命令
     */
    public void runCommand(String command) {
        if (connection == null) {
            print("数据库错误: 连接为空，无法执行命令");
            return;
        }
        
        try {
            connection.createStatement().executeUpdate(command);
        } catch (SQLException e) {
            if (!command.contains("CREATE TABLE")) {
                print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
    }

    private int runCommandGetId(String command) {
        if (connection == null) {
            print("数据库错误: 连接为空，无法执行命令");
            return -1;
        }
        
        try {
            PreparedStatement statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        }
        return -1;
    }
}

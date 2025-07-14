package cc.ranmc.farm.sql;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLRow;
import cc.ranmc.farm.bean.SQLFilter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.xuanming.nzw.NzwUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Database {

    private Connection connection;

    // 新增：带参数的构造方法
    public Database(String host, int port, String db, String user, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
            connection = DriverManager.getConnection(url, user, password);
            if (connection != null) {
                createTable();
            } else {
                NzwUtil.print("数据库连接未初始化，无法创建表");
            }
        } catch (Exception e) {
            NzwUtil.print("数据库错误" + e.getMessage());
            e.printStackTrace();
        }
    }

    public Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tyz?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                "tyz", "wm123456");
            if (connection != null) {
                createTable();
            } else {
                NzwUtil.print("数据库连接未初始化，无法创建表");
            }
        } catch (Exception e) {
            NzwUtil.print("数据库错误" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (Exception e) {
            NzwUtil.print("数据库错误" + e.getMessage());
        }

    }

    /**
     * 新增数据库表
     */
    public void createTable() {
        runCommand("CREATE TABLE IF NOT EXISTS THck (" +
                "ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "PLAYER VARCHAR(255) NOT NULL," +
                "OPEN TINYINT(1)," +
                "CARROT INTEGER," +
                "WHEAT INTEGER," +
                "WHEAT_SEEDS INTEGER," +
                "BEETROOT INTEGER," +
                "BEETROOT_SEEDS INTEGER," +
                "NETHER_WART INTEGER," +
                "PUMPKIN INTEGER," +
                "MELON INTEGER," +
                "CACTUS INTEGER," +
                "BAMBOO INTEGER," +
                "SUGAR_CANE INTEGER," +
                "POTATO INTEGER" +
                ")");
    }

    /**
     * 异步建表
     */
    public void createTableAsync(Plugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::createTable);
    }

    /**
     * 新增数据库
     * @param table 表
     * @param data 内容
     */
    public int insert(String table, SQLRow data) {
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
            NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
        }
        return -1;
    }

    /**
     * 异步插入
     */
    public void insertAsync(String table, SQLRow data, Plugin plugin, java.util.function.Consumer<Integer> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int result = insert(table, data);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result));
            }
        });
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

    /**
     * 异步查询单条
     */
    public void selectMapAsync(String table, SQLFilter filter, Plugin plugin, java.util.function.Consumer<SQLRow> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            SQLRow row = selectMap(table, filter);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(row));
            }
        });
    }

    /**
     * 异步查询多条
     */
    public void selectListAsync(String table, SQLFilter filter, Plugin plugin, java.util.function.Consumer<List<SQLRow>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<SQLRow> list = selectList(table, filter);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(list));
            }
        });
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
            NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
        return data;
    }

    protected List<SQLRow> queryList(String command) {
        List<SQLRow> list = new ArrayList<>();
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
            NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
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
     * 异步更新
     */
    public void updateAsync(String table, SQLFilter filter, Plugin plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            update(table, filter);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
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
     * 异步删除
     */
    public void deleteAsync(String table, String id, Plugin plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            delete(table, id);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    /**
     * 执行数据库指令
     * @param command 命令
     */
    public void runCommand(String command) {
        if (connection == null) {
            NzwUtil.print("数据库连接未初始化，无法执行命令: " + command);
            return;
        }
        try {
            connection.createStatement().executeUpdate(command);
        } catch (SQLException e) {
            if (!command.contains("CREATE TABLE")) {
                NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
    }

    /**
     * 异步执行SQL
     */
    public void runCommandAsync(String command, Plugin plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            runCommand(command);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    private int runCommandGetId(String command) {
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
            NzwUtil.print("数据库错误" + e.getMessage() + "\n" + command);
        }
        return -1;
    }
}

# FarmInventory - 农作物仓库插件

## 简介
FarmInventory 是一个 Minecraft 农作物仓库管理插件，支持自动收集农作物、GUI界面管理、数据库存储等功能。

## 功能特性
- 🌾 自动收集农作物到仓库
- 🎮 美观的GUI界面管理
- 💾 H2数据库存储
- 🔧 完整的配置文件系统
- 🎯 支持NBT精确物品比较
- 👥 权限系统支持
- 🔄 配置热重载

## 配置文件说明

### 数据库配置
```yaml
database:
  # 数据库类型: h2, mysql, sqlite
  type: "h2"
  
  # H2数据库配置
  h2:
    file: "data"              # 数据库文件路径
    username: "sa"            # 用户名
    password: ""              # 密码
    auto_create_tables: true  # 自动创建表
```

### 插件基本设置
```yaml
settings:
  prefix: "&b[作物仓库]"      # 插件前缀
  auto_collect: true          # 自动收集农作物
  show_collect_message: true  # 显示收集提示
  default_open: true          # 默认仓库开关状态
  
  # 最大页数限制
  max_pages:
    normal: 20    # 普通用户
    vip: 30       # VIP用户
    svip: 50      # SVIP用户
```

### 农作物配置
```yaml
crops:
  # 支持的农作物类型
  enabled:
    - "POTATO"
    - "CARROT"
    - "WHEAT"
    # ... 更多农作物
  
  # 农作物显示名称
  names:
    POTATO: "马铃薯"
    CARROT: "胡萝卜"
    # ... 更多名称
```

### GUI界面配置
```yaml
gui:
  title: "&d&l桃韵斋丨作物仓库"  # 界面标题
  items_per_page: 45           # 每页物品数量
  size: 54                     # 界面大小
  
  # 按钮配置
  buttons:
    back:
      slot: 45
      material: "RED_STAINED_GLASS_PANE"
      name: "&c返回菜单"
    # ... 更多按钮配置
```

### 消息配置
```yaml
messages:
  prefix: "&b桃韵斋>>>"
  
  success:
    reload: "&a配置重载成功"
    crop_stored: "&a作物已存放仓库,打开菜单查看吧"
    # ... 更多成功消息
  
  error:
    no_permission: "&c你没有权限这样做"
    crop_not_found: "&c没有找到这个农作物"
    # ... 更多错误消息
```

### 权限配置
```yaml
permissions:
  user: "fm.user"      # 基础用户权限
  admin: "fm.admin"    # 管理员权限
  vip: "ranmc.vip"     # VIP权限
  svip: "ranmc.svip"   # SVIP权限
```

## 命令
- `/fm <农作物>` - 打开指定农作物仓库
- `/fm switch` - 切换仓库开关状态
- `/fm reload` - 重载配置文件 (需要管理员权限)

## 权限节点
- `fm.user` - 基础使用权限
- `fm.admin` - 管理员权限
- `ranmc.vip` - VIP权限
- `ranmc.svip` - SVIP权限

## 安装说明

1. 下载插件JAR文件
2. 将文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 插件会自动生成配置文件 `plugins/FarmInventory/config.yml`
5. 根据需要修改配置文件
6. 使用 `/fm reload` 重载配置

## 技术特性

### 数据库支持
- **H2数据库**: 轻量级文件数据库，默认使用
- **MySQL数据库**: 支持远程数据库 (配置中已预留)
- **自动表创建**: 首次运行自动创建必要的数据表

### NBT支持
- 精确的物品比较，避免重复物品
- 支持自定义NBT标签
- 纯净农作物检测

### 配置系统
- 完整的YAML配置文件
- 热重载支持
- 默认值保护
- 详细的配置注释

## 开发信息
- **版本**: 4.0
- **作者**: xuanming
- **目标版本**: Java 21
- **依赖**: Paper API, H2 Database, Lombok

## 更新日志

### v4.0
- ✅ 添加完整的配置文件系统
- ✅ 支持数据库配置
- ✅ 添加GUI界面配置
- ✅ 添加消息配置
- ✅ 添加权限配置
- ✅ 支持配置热重载
- ✅ 改进错误处理和日志记录

### 之前的版本
- 基础农作物仓库功能
- GUI界面
- 数据库存储
- NBT支持

## 注意事项
1. 确保服务器使用Java 21或更高版本
2. 首次运行会自动创建配置文件
3. 修改配置后需要重载插件或使用重载命令
4. 数据库文件存储在插件数据文件夹中

## 支持
如有问题或建议，请联系开发者或提交Issue。 
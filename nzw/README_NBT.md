# FarmInventory NBT API 集成

## 概述

本项目已成功集成 NBT API，用于处理 Minecraft 物品的 NBT（Named Binary Tag）数据。NBT API 提供了强大的物品数据操作功能，可以精确比较物品、创建自定义物品等。

## 集成的功能

### 1. NBT 工具类 (`NBTUtil.java`)

提供了完整的 NBT 操作功能：

- **读取 NBT 数据**: `getNBTString()`, `getNBTInteger()`, `getNBTBoolean()`
- **设置 NBT 数据**: `setNBTString()`, `setNBTInteger()`, `setNBTBoolean()`
- **移除 NBT 标签**: `removeNBTKey()`
- **比较物品**: `hasSameNBT()` - 精确比较两个物品的 NBT 数据
- **检查标签**: `hasNBTKey()` - 检查物品是否有特定 NBT 标签
- **获取所有 NBT**: `getAllNBT()` - 获取物品的所有 NBT 数据
- **自定义物品**: `isCustomItem()`, `createCustomItem()` - 创建和识别自定义物品

### 2. 增强的农作物检测

在 `FarmUtil.java` 中集成了 NBT 支持：

- `isValidCrop()` - 使用 NBT 比较确保物品是纯净的农作物
- 改进了物品存储逻辑，防止带有特殊 NBT 标签的物品被误认为农作物

### 3. 改进的事件监听器

在 `FarmListener.java` 中：

- 使用 NBT 检查确保只有纯净的农作物会被自动收集
- 防止带有特殊属性的物品被误处理

### 4. NBT 测试命令

新增了 `/nbt` 命令用于测试 NBT 功能：

- `/nbt info` - 显示手持物品的 NBT 信息
- `/nbt set <key> <value>` - 设置 NBT 标签
- `/nbt get <key>` - 获取 NBT 标签值
- `/nbt remove <key>` - 移除 NBT 标签
- `/nbt compare` - 比较两个物品的 NBT
- `/nbt create <key> <value>` - 创建自定义物品

## 依赖配置

在 `pom.xml` 中添加了 NBT API 依赖：

```xml
<dependency>
    <groupId>de.tr7zw</groupId>
    <artifactId>item-nbt-api</artifactId>
    <version>2.12.2</version>
    <scope>provided</scope>
</dependency>
```

同时添加了 CodeMC 仓库：

```xml
<repository>
    <id>codemc-repo</id>
    <url>https://repo.codemc.org/repository/maven-public/</url>
</repository>
```

## 使用示例

### 1. 检查物品是否为纯净农作物

```java
ItemStack item = player.getInventory().getItemInMainHand();
if (FarmUtil.isValidCrop(item, Material.WHEAT)) {
    // 这是纯净的小麦
}
```

### 2. 创建自定义物品

```java
ItemStack customItem = NBTUtil.createCustomItem(
    new ItemStack(Material.DIAMOND), 
    "CustomType", 
    "SpecialItem"
);
```

### 3. 比较两个物品

```java
boolean sameNBT = NBTUtil.hasSameNBT(item1, item2);
```

### 4. 获取 NBT 数据

```java
String customValue = NBTUtil.getNBTString(item, "CustomKey");
int customNumber = NBTUtil.getNBTInteger(item, "CustomNumber");
boolean customFlag = NBTUtil.getNBTBoolean(item, "CustomFlag");
```

## 优势

1. **精确物品识别**: 通过 NBT 比较，可以精确识别物品，避免误处理
2. **自定义物品支持**: 可以创建和识别带有特殊属性的自定义物品
3. **数据完整性**: 确保只有纯净的农作物被存储到仓库中
4. **扩展性**: 为未来的功能扩展提供了强大的基础
5. **调试工具**: 提供了完整的 NBT 测试命令，便于开发和调试

## 注意事项

1. NBT API 需要服务器支持，确保服务器已安装相应的插件
2. 使用 NBT 功能时要注意性能影响，避免在频繁调用的地方过度使用
3. 自定义物品的 NBT 标签应该遵循一定的命名规范，避免冲突

## 未来扩展

基于 NBT API 的集成，可以考虑以下扩展功能：

1. **品质系统**: 为农作物添加品质属性
2. **特殊农作物**: 支持带有特殊属性的农作物
3. **物品追踪**: 追踪物品的来源和历史
4. **高级筛选**: 基于 NBT 属性的高级筛选功能 
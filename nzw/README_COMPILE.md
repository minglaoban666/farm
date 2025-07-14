# FarmInventory 编译说明

## 当前状况

项目已经成功集成了NBT API的概念，但由于外部依赖下载问题，暂时使用了简化版本。

## 已解决的问题

1. ✅ **Java版本问题**: 从Java 24改为Java 21
2. ✅ **PlaceholderAPI依赖**: 已移除，代码中有检查逻辑
3. ✅ **Ranmc系统依赖**: 已移除，相关代码已注释
4. ✅ **NBT API依赖**: 已移除，使用简化版本
5. ✅ **外部工具类依赖**: 已创建本地版本

## NBT API 简化版本

由于 `de.tr7zw:item-nbt-api` 依赖下载困难，我们创建了一个简化版本的 `NBTUtil` 类：

### 简化版本功能

- **物品比较**: 只比较物品类型，不比较NBT数据
- **NBT操作**: 所有NBT操作都返回默认值
- **自定义物品**: 暂时不支持真正的自定义物品

### 如何启用完整NBT功能

1. 取消注释 `pom.xml` 中的NBT API依赖：
```xml
<dependency>
    <groupId>de.tr7zw</groupId>
    <artifactId>item-nbt-api</artifactId>
    <version>2.12.2</version>
    <scope>provided</scope>
</dependency>
```

2. 取消注释 `NBTUtil.java` 中的导入：
```java
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTList;
import de.tr7zw.nbtapi.NBTListCompound;
```

3. 恢复所有NBT方法的完整实现

## 编译步骤

1. 确保使用Java 21
2. 在IntelliJ IDEA中点击编译按钮
3. 或使用Maven命令：`mvn clean compile`

## 当前功能

- ✅ 基础农作物仓库功能
- ✅ GUI界面
- ✅ 数据库存储
- ✅ 命令系统
- ✅ 事件监听
- ⚠️ NBT功能（简化版本）
- ⚠️ PlaceholderAPI（已注释）
- ⚠️ 桃韵斋集成（已注释）

## 测试命令

- `/farm <作物类型>` - 打开农作物仓库
- `/fm <作物类型>` - 打开农作物仓库
- `/nbt info` - 显示NBT信息（简化版本）
- `/nbt set/get/remove` - NBT操作（简化版本）

## 注意事项

1. 当前版本使用简化NBT比较，只比较物品类型
2. 如果需要精确的NBT比较，需要手动下载NBT API依赖
3. 所有外部依赖都已移除，确保编译成功
4. 代码结构完整，可以随时恢复完整功能

## 未来改进

1. 手动下载NBT API jar文件到lib目录
2. 恢复完整的NBT功能
3. 添加更多农作物类型支持
4. 优化性能和用户体验 
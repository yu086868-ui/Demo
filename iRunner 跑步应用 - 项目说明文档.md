# iRunner 跑步应用 - 项目说明文档

## 项目概述

iRunner 是一款基于 Android 平台的跑步追踪应用，参考 Keep 跑步的设计理念，提供完整的跑步记录、实时轨迹绘制、数据统计和成就系统功能。

## 技术架构

### 技术栈
- **开发语言**: Kotlin
- **最小 SDK**: API 24 (Android 7.0)
- **目标 SDK**: API 36
- **开发工具**: Android Studio
- **架构模式**: MVVM + Repository 模式

### 核心框架
- **Room**: 本地数据持久化
- **高德地图**: 实时轨迹绘制和定位
- **Coroutines**: 异步任务处理
- **LiveData**: 数据观察和UI更新
- **Material Design**: 现代化UI组件

##  项目结构

```
app/src/main/java/com/example/demo/
├── ui/
│   ├── activities/
│   │   └── MainActivity.kt                        # 主Activity，协调所有Fragment
│   ├── fragments/
│   │   ├── HomeFragment.kt                        # 首页 - 数据概览和快速入口
│   │   ├── RunningFragment.kt                     # 跑步界面 - 实时数据和地图
│   │   ├── RunHistoryFragment.kt                  # 跑步历史 - 记录查看和统计
│   │   └── AchievementFragment.kt                 # 成就系统 - 成就展示和进度
│   └── adapters/                                  
├── data/
│   ├── models/
│   │   ├── Run.kt                                 # 跑步数据模型及位置点模型
│   │   └── Achievement.kt                         # 成就数据模型
│   ├── repository/
│   │   ├── AchievementDatabaseRepository.kt       # 成就数据库操作封装      
│   │   ├── RunRepository.kt                       # 跑步数据仓库（内存+数据库）
│   │   ├── AchievementRepository.kt               # 成就数据仓库（内存+数据库）
│   │   └── DatabaseRepository.kt                  # 跑步数据库操作封装
│   ├── database/
│   │   ├── entities/                              # Room实体类
│   │   ├── dao/                                   # 数据访问对象
│   │   ├── converters/                            # 类型转换器
│   │   └── AppDatabase.kt
|   └── persistence/
│       └── PersistenceManager.kt
├── service/
│   └── RunningService.kt                          # 后台跑步服务
└── utils/                                         
```

## 核心功能模块

### 1. 跑步记录模块
- **开始跑步**: GPS定位和轨迹记录
- **实时数据**: 距离、时长、速度、卡路里实时计算
- **轨迹绘制**: 高德地图实时绘制跑步路径
- **暂停/继续**: 灵活的跑步控制
- **结束保存**: 数据持久化存储

### 2. 数据统计模块
- **单次跑步统计**: 详细的数据分析
- **累计统计**: 总次数、总距离、总时长、总卡路里
- **历史记录**: 按时间排序的跑步记录列表
- **数据可视化**: 图表展示跑步趋势

### 3. 成就系统模块
- **成就分类**
- **进度追踪**: 实时更新成就完成进度
- **解锁通知**: 成就达成自动解锁

### 4. 数据持久化模块
- **Room数据库**: SQLite封装，自动数据同步
- **内存缓存**: 快速数据访问
- **类型安全**: 编译时查询检查
- **数据迁移**: 版本管理和迁移支持

## 系统界面展示

<img src="C:\Users\Lenovo\Desktop\1.jpg" style="zoom:25%;" />

<img src="C:\Users\Lenovo\Desktop\2.jpg" style="zoom:25%;" />

<img src="C:\Users\Lenovo\Desktop\3.jpg" style="zoom:25%;" />

<img src="C:\Users\Lenovo\Desktop\4.jpg" style="zoom:25%;" />

##  配置说明

### 权限配置
```xml
<!-- 定位权限 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<!-- 网络权限（高德地图需要） -->
<uses-permission android:name="android.permission.INTERNET" />
```

### 依赖配置
主要依赖库版本：
- Room: 2.6.1
- 高德地图: 9.7.0
- Kotlin: 2.0.21
- Material Design: 1.10.0

## 运行说明

### 环境要求
- Android Studio 最新版本
- Android SDK API 24+
- Kotlin 插件
- 有效的签名配置（Release版本）

### 测试流程
1. **权限测试**: 首次运行授予定位权限
2. **基础功能**: 首页 → 开始跑步 → 结束保存
3. **数据验证**: 查看历史记录和统计数据
4. **成就系统**: 完成跑步验证成就解锁
5. **持久化**: 退出重进验证数据保存

##  数据模型

### 跑步记录 (Run)
```kotlin
data class Run(
    var id: Long = 0,
    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var distance: Float = 0f,        // 米
    var duration: Long = 0L,         // 毫秒
    var calories: Float = 0f,
    var averageSpeed: Float = 0f,    // 米/秒
    var maxSpeed: Float = 0f,        // 米/秒
    var locations: List<LocationPoint> = emptyList()
)
```

### 位置点 (LocationPoint)
```kotlin
data class LocationPoint(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var timestamp: Long = 0L,
    var speed: Float = 0f
)
```

### 成就 (Achievement)
```kotlin
data class Achievement(
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    var type: AchievementType = AchievementType.DISTANCE,
    var targetValue: Float = 0f,
    var currentValue: Float = 0f,
    var isUnlocked: Boolean = false,
    var iconResId: Int = 0
)
```

## UI设计

### 设计理念
- **Material Design 3**: 现代化设计语言
- **卡片式布局**: 信息层次清晰
- **直观交互**: 用户操作简单明了
- **响应式设计**: 适配不同屏幕尺寸

## 数据流

### 跑步数据流
```
GPS定位 → 位置点收集 → 实时计算 → 数据持久化 → UI更新
```

### 成就数据流
```
跑步完成 → 数据统计 → 成就检查 → 进度更新 → 解锁通知
```

### 数据库同步
```
内存操作 → 异步保存 → 数据库持久化 → 应用启动恢复
```

## 未来扩展规划

### 短期优化
- [ ] 添加数据导出功能
- [ ] 实现跑步计划功能
- [ ] 添加社交分享功能
- [ ] 添加登录注册功能

### 长期规划
- [ ] 云端数据同步
- [ ] 多设备支持
- [ ] 高级数据分析
- [ ] 个性化训练计划

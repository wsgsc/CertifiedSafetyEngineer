# 小龚注安快刷

一款基于 Jetpack Compose 构建的注册安全工程师考试学习应用，支持题库练习、错题管理、学习记录追踪和 PDF 资料阅读。

## 功能特性

- **分级题库** - 支持初级和中级注册安全工程师考试题库
- **多种练习模式** - 顺序练习、随机练习、错题复习、收藏题目练习
- **学习记录** - 自动记录答题历史和学习进度
- **错题本** - 自动收集错题，支持针对性复习
- **收藏管理** - 收藏重点题目，方便回顾
- **PDF 资料** - 内置考试相关 PDF 资料阅读功能
- **每日提醒** - 可设置每日学习提醒
- **个人资料** - 自定义用户信息和头像

## 技术栈

### 核心框架
- **Kotlin** - 开发语言
- **Jetpack Compose** - 现代化声明式 UI 框架
- **Material Design 3** - UI 设计规范

### 架构组件
- **Room** - 本地数据库，存储题库、错题、收藏和学习记录
- **ViewModel** - UI 状态管理
- **Navigation Compose** - 应用导航
- **DataStore** - 轻量级数据存储，保存用户偏好设置
- **Coroutines + Flow** - 异步编程和响应式数据流

### 其他依赖
- **WorkManager** - 后台任务调度（每日提醒）
- **Gson** - JSON 序列化和反序列化
- **Coil** - 图片加载库
- **PDF Viewer** - PDF 文档阅读

## 项目结构

```
app/src/main/java/com/xiaogong/csestudy/
├── data/
│   ├── local/
│   │   ├── dao/           # 数据访问对象
│   │   ├── entity/        # 数据库实体类
│   │   └── converter/     # 类型转换器
│   ├── model/             # 数据模型
│   └── repository/        # 数据仓库层
├── ui/
│   ├── levelselection/    # 等级选择界面
│   ├── quiz/              # 答题界面
│   ├── study/             # 学习界面
│   ├── profilesetup/      # 个人资料设置
│   ├── main/              # 主界面
│   └── theme/             # 主题配置
├── util/                  # 工具类
├── CseApplication.kt      # Application 类
└── MainActivity.kt        # 主 Activity
```

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 11 或更高版本
- Android SDK 26+ (Android 8.0+)
- Gradle 8.0+

## 构建和运行

1. 克隆项目到本地
```bash
git clone <repository-url>
cd CertifiedSafetyEngineer
```

2. 使用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 连接 Android 设备或启动模拟器

5. 点击运行按钮或使用命令行构建
```bash
# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

## 数据库结构

应用使用 Room 数据库存储以下数据：

- **WrongQuestionEntity** - 错题记录
- **FavoriteQuestionEntity** - 收藏题目
- **StudyRecordEntity** - 学习记录
- **UserAnswerEntity** - 用户答题记录

题库数据通过 JSON 文件加载到应用中。

## 版本信息

- **当前版本**: 1.0
- **最低支持**: Android 8.0 (API 26)
- **目标版本**: Android 15 (API 35)

## 开发计划

- [ ] 增加模拟考试模式
- [ ] 支持题目搜索功能
- [ ] 添加学习统计图表
- [ ] 支持数据云同步
- [ ] 优化 PDF 阅读体验

## 许可证

[待添加许可证信息]

## 联系方式

如有问题或建议，欢迎通过应用内捐赠功能支持开发。

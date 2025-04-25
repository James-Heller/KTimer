# KTimer

![Version](https://img.shields.io/badge/version-0.0.1--Alpha-blue)
![JDK](https://img.shields.io/badge/JDK-21-orange)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple)
![License](https://img.shields.io/badge/license-MIT-green)

KTimer 是一个基于 Kotlin 和 Netty 开发的高性能分布式定时任务调度服务。它通过网络通信实现任务的分发与执行，支持任务调度、取消和心跳检测等功能。

## 特性

- 🚀 基于 Netty 的高性能网络通信
- ⏰ 分布式定时任务调度
- 💬 简单高效的消息协议
- 🔄 支持任务触发与取消
- ❤️ 心跳检测机制
- 🧩 易于集成的客户端-服务器架构

## 技术栈

- Kotlin 2.1.20
- Netty 4.2.0.Final
- Jackson 2.18.3 (JSON 序列化/反序列化)
- SLF4J 2.0.17 (日志记录)
- Gradle 构建系统

## 安装方法

### 前提条件

- JDK 21 或更高版本
- Gradle 8.0+ (可选，使用包装器时不需要)

### 编译项目

1. 克隆仓库:

```bash
git clone https://github.com/James-Heller/KTimer.git
cd KTimer
```

2. 编译项目:

```bash
./gradlew build
```

3. 创建可执行 jar 包:

```bash
./gradlew shadowJar
```

编译后的 jar 文件将位于 `build/libs/KTimer-0.0.1-Alpha.jar`。

## 使用方法

### 启动服务器

```bash
java -jar build/libs/KTimer-0.0.1-Alpha.jar
```

默认情况下，服务器将在端口 4396 上启动。

### 客户端连接

客户端可以通过 Netty 建立与服务器的连接，并发送以下类型的消息：

1. **客户端注册** (`CLIENT_REGISTER`): 向服务器注册客户端
2. **任务调度** (`SCHEDULE_TASK`): 向服务器提交定时任务
3. **任务触发** (`TASK_TRIGGER`): 触发已调度的任务
4. **取消任务** (`CANCEL_TASK`): 取消已调度的任务
5. **心跳检测** (`HEARTBEAT`): 维持客户端与服务器的连接

### 消息格式

KTimer 使用 JSON 格式进行消息传输，基本结构如下:

```json
{
  "type": "SCHEDULE_TASK",
  "taskId": "task-unique-identifier",
  "context": { /* 任务相关数据 */ }
}
```

## 项目结构

```
src/main/kotlin/space/jamestang/ktimer/
├── core/                      # 核心功能模块
│   ├── Constant.kt            # 全局常量和初始化
│   ├── KTimerServer.kt        # 服务器实现
│   ├── KTimerHandler.kt       # 消息处理器
│   └── DebugHandler.kt        # 调试处理器
├── codec/                     # 编解码器
│   ├── KTimerMessageEncoder.kt # 消息编码器
│   └── KTimerMessageDecoder.kt # 消息解码器
├── data/                      # 数据模型
│   └── KTimerMessage.kt       # 消息定义
├── ConnectionPool.kt          # 连接池管理
└── Application.kt             # 应用程序入口

```

## 后续开发计划

- [ ] 完善任务调度机制
- [ ] 添加任务持久化功能
- [ ] 提供更多任务触发策略
- [ ] 构建客户端 SDK
- [ ] 支持集群部署
- [ ] 添加 Web 管理界面
- [ ] 增强监控和报警功能

## 贡献

欢迎提交 Pull Request 或提出 Issue 来帮助改进这个项目！

## 许可证

[MIT License](LICENSE)

## 联系方式

- 作者: James Tang
- 邮箱: [James-Heller@Outlook.com]
- GitHub: [James-Heller](https://github.com/James-Heller)

---
*最后更新于 2025-04-25*

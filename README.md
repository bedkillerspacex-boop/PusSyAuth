# PusSyAuth

一个 Minecraft Fabric 客户端认证与授权框架。  
自动检测游戏内胜利标题并触发认证 API 调用。

## 功能

- **胜利检测** — 监听传入的标题数据包，检测关键词"胜利"并计数
- **自动 API 触发** — 胜利次数达到 3 时自动调用认证 API 并重置计数器
- **手动触发** — 按配置的按键手动调用 API（同时重置计数器）
- **剪贴板自动复制** — API 返回内容自动复制到系统剪贴板（可开关）
- **Ping 测试** — 在设置界面测试 API 连接
- **聊天消息开关** — 控制 API 结果是否在聊天栏显示
- **MOTD 覆盖** — 在服务器列表 MOTD 中显示认证状态

## 运行环境

- Minecraft 1.21.4
- Fabric Loader >=0.16.10
- Fabric API

## 安装

1. 从 Releases 下载最新的 `.jar`
2. 放入 `.minecraft/mods` 文件夹
3. 使用 Fabric 配置文件启动游戏

## 配置

编辑游戏目录下的 `config/pussyauth.properties`：

```properties
debug=false
authEnabled=true
activeApiProvider=miracle
MiracleApiKey=你的API密钥
timeoutSeconds=20
autoCopy=true
showChatMessage=true
```

也可以通过游戏内 GUI 配置（按 Y 键打开）。

## 按键绑定

| 操作 | 默认按键 |
|------|---------|
| 打开设置 | Y |
| 调用 MiracleAuth API | 未绑定 — 在 Controls 中设置 |

## GUI

- **PusSyAuth 设置**（Y 键）— Debug 开关、MiracleApi 配置、聊天消息开关
- **MiracleApi** — API Key 输入、超时设置、自动复制开关、Ping 测试

## 开发

```bash
./gradlew build
```

输出：`build/libs/pussyauth-<version>.jar`

## 许可证

MIT

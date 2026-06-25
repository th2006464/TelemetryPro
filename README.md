# Telemetry Pro GPS

一款纯本地、离线可用的 GPS 状态监控工具，专为飞机/高空/户外环境设计。

## 核心功能

- **GPS 状态监控**：已定位 / 搜索中 / 无信号 三态显示
- **卫星星座统计**：GPS / GLONASS / Galileo / BeiDou / QZSS / IRNSS / SBAS 分星座显示连接数
- **SNR 信号强度**：每颗卫星的载噪比柱状图
- **离线点阵地图**：720×360 高精度世界地图，无需联网即可查看当前位置
- **运动数据**：速度、海拔、精度、坐标实时显示
- **航迹记录**：支持后台记录移动轨迹并在地图上绘制
- **飞行模式识别**：根据速度 + 卫星状态自动判断高空环境

## 技术栈

| 项目 | 说明 |
|------|------|
| 语言 | Kotlin 1.9.21 |
| UI | Jetpack Compose BOM 2023.10.01 |
| 架构 | MVVM（GpsViewModel → GpsRepository → LocationManager） |
| 最低 API | 26（Android 8.0） |
| 目标 API | 34（Android 14） |
| 构建 | Gradle 8.2 Kotlin DSL |
| 设计系统 | Astra Precision（深色航空仪表风格） |

## 项目结构

```
TelemetryPro/
├── app/
│   ├── src/main/java/com/telemetrypro/app/
│   │   ├── ui/
│   │   │   ├── map/              # 离线点阵地图
│   │   │   │   ├── DotMatrixMap.kt        # Compose 地图组件
│   │   │   │   ├── WorldMapGrid.kt        # 720×360 陆地位图（Base64）
│   │   │   │   ├── WorldMapProjection.kt  # Mercator 投影
│   │   │   │   └── WorldMapCities.kt      # 主要城市标注
│   │   │   ├── components/        # 仪表盘组件
│   │   │   │   ├── ReadoutTile.kt         # 数据面板
│   │   │   │   ├── ConstellationStats.kt  # 星座统计
│   │   │   │   └── SnrBarGraph.kt         # SNR 柱状图
│   │   │   ├── screens/           # 各页面
│   │   │   └── theme/             # Astra Precision 设计系统
│   │   └── data/                  # 数据模型 + Repository
│   └── build.gradle.kts
├── tools/
│   └── map/
│       └── generate_world_map_v2.js  # 点阵地图生成脚本
├── docs/
│   └── reference_map.svg          # 地图预览
├── keystore.properties            # 签名配置（不入库）
└── foxtang-release-000000.keystore  # 签名证书（不入库）
```

## 构建指南

### 环境要求

- JDK 17
- Android SDK 34（Build Tools 34.0.0）
- Node.js 22+（仅重新生成点阵地图时需要）

### 签名配置

首次构建前需创建 `keystore.properties`（项目根目录，已 gitignore）：

```properties
storeFile=../foxtang-release-000000.keystore
storePassword=000000
keyAlias=foxtang-release
keyPassword=000000
```

并放置对应的 `.keystore` 文件。无此文件时 release 构建会产出未签名 APK，无法安装。

### 构建命令

```bash
# Release 构建（已签名）
./gradlew assembleRelease

# Debug 构建（debug 签名）
./gradlew assembleDebug
```

输出路径：`app/build/outputs/apk/release/TelemetryPro_v<version>.apk`

### 重新生成点阵地图

如需更新地图数据（例如升级到 Natural Earth 10m 精度）：

```bash
cd tools/map/data
# 下载 Natural Earth 50m land 数据
curl -L -o ne_50m_land.zip https://naciscdn.org/naturalearth/50m/physical/ne_50m_land.zip
unzip ne_50m_land.zip

# 转换为 GeoJSON（需 shapefile npm 包）
cd ../../..
npm install shapefile
node -e "..."  # 见 generate_world_map_v2.js 内嵌说明

# 生成 Kotlin 网格文件
node TelemetryPro/tools/map/generate_world_map_v2.js
```

## 版本历史

| 版本 | 主要变更 |
|------|---------|
| 1.8.3 | GNSS 坐标经纬度统一格式（同字号、同颜色、单位同行） |
| 1.8.2 | 仪表盘面板等高 + GNSS 坐标 °N 移到数字右侧 |
| 1.8.1 | 地图拖动性能优化（预渲染 ImageBitmap）+ 星座卡片精简 |
| 1.8.0 | 高精度点阵地图（720×360, Natural Earth 50m, 移除南极洲）+ 自有签名 |
| 1.7.x | 初版点阵地图（360×170, 手写多边形） |
| 1.6.x | 基础 GPS 监控功能 |

---

## 开发经验与踩坑总结

### 1. 点阵地图精度问题

**问题**：初版使用 360×170 网格（~1°/cell，赤道 111km）+ 手写粗略多边形，海岸线严重失真，400km 范围内只能看到 3-4 个格子。

**解决**：
- 网格升级到 720×360（0.5°/cell，~55km）
- 数据源改用 Natural Earth 50m land polygons（1420 个多边形，60669 坐标点）
- 纬度覆盖从 ±85° 改为 +85° ~ -60°（移除无用的南极洲，提高有效区域密度）

**经验**：地理数据不要手写，直接用开源权威数据源（Natural Earth / GADM）。生成脚本带空间索引（72×36 粗粒度网格），1420 个多边形的 point-in-polygon 判定从数分钟降到 12 秒。

### 2. JVM MethodTooLargeException

**问题**：720×360 网格 = 32,400 字节，用 `byteArrayOf(0.toByte(), 1.toByte(), ...)` 写成 Kotlin 源码后，编译出的 `<clinit>` 方法超过 64KB 字节码上限，构建失败。

**解决**：改用 Base64 字符串常量 + 运行时 lazy decode。源码从 32,422 行压缩到 242 行，运行时按需解码一次后缓存。

**经验**：Android 编译器对单个方法的字节码有 64KB 限制。大块二进制数据不要用 `byteArrayOf(...)` 展开，用 Base64/String 编码 + lazy decode 是标准做法。

### 3. 地图拖动卡顿（GC 压力）

**问题**：v1.8.0 地图精度提升后，拖动时明显卡顿。Profiler 显示每帧分配 4万-16万个 `Offset` 对象，60fps 下产生 ~240MB/s GC 压力。

**根因**：每帧用 `buildList` 遍历可见格子，对每个陆地 cell 创建 1-16 个 `Offset` 对象，再用 `drawPoints` 绘制。

**解决**：启动时预渲染整个点阵地图到 `ImageBitmap`（1440×720，~4MB），每帧仅一次 `drawImage` GPU 调用，零内存分配。

**经验**：Compose Canvas 的 `drawPoints` 虽然看似高效，但构建点列表的过程会产生大量临时对象。对于静态/半静态内容，预渲染到 `ImageBitmap` 再 `drawImage` 是更优解——GPU 硬件加速缩放，完全消除 GC 压力。

### 4. APK 签名问题

**问题**：首次用 `assembleRelease` 构建的 APK 安装时提示"安装包未包含任何证书"。

**根因**：`build.gradle.kts` 的 `release` buildType 没有配置 `signingConfig`，产出的是未签名 APK。而之前所有版本（v1.6~v1.7）都用 `assembleDebug`，走的是 Android 默认 debug 签名。

**解决**：
1. 用 `keytool` 生成自有 keystore（RSA 2048，25 年有效期）
2. 在 `build.gradle.kts` 添加 `signingConfigs { release { ... } }`，从 `keystore.properties` 读取配置
3. `keystore.properties` 和 `.keystore` 文件加入 `.gitignore`

**经验**：
- Debug 签名只能开发用，正式发布必须用自有 keystore
- Keystore 是应用的身份凭证，**一旦丢失就无法发布更新**（用户无法覆盖安装），务必备份
- 签名配置（密码）不要硬编码进 `build.gradle.kts`，用独立的 properties 文件 + gitignore

### 5. Gradle Kotlin DSL 的 import 问题

**问题**：在 `build.gradle.kts` 中用 `java.util.Properties()` 报 `Unresolved reference: util`。

**解决**：Kotlin DSL 默认不自动 import `java.util`，需要在文件顶部显式 `import java.util.Properties`。

**经验**：Gradle Kotlin DSL 的隐式 import 比 Groovy DSL 少很多。遇到 `Unresolved reference` 先检查是否需要显式 import。

### 6. 仪表盘布局一致性

**问题**：GNSS 坐标面板中，纬度用 `value` 参数（TelemetryMd 18sp，黄色），经度用 `subLabel` 参数（CodeSm 12sp，灰色），导致经纬度字号、颜色、单位位置都不一致。

**解决**：给 `ReadoutTile` 新增 `secondaryValue` / `secondaryUnit` 参数，渲染为与主 value 完全相同样式的第二行。

**经验**：通用组件设计时，"次要数据"不等于"小号数据"。当业务上两个值是平等的（如经度 vs 纬度），就应该用相同样式，而不是复用语义不同的 `subLabel`。

### 7. 同行面板高度不齐

**问题**：同一 `Row` 内的 `ReadoutTile` 高度不统一，因为内容行数不同。

**解决**：所有 tile 的 `modifier` 加 `.fillMaxHeight()`，同行面板自动对齐到最高面板。

**经验**：Compose 的 `Row` 默认不会让子元素等高，需要显式 `fillMaxHeight()`。这和 CSS Flexbox 的 `align-items: stretch` 行为不同。

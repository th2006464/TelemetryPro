<div align="center">

# 🛰️ Telemetry Pro

**离线 GPS 遥测监控 · Aviation Glass-Cockpit Instrument Cluster**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202023.10-4285F4?logo=jetpackcompose)](https://developer.android.com/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-4CAF50?logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

[English](#english) · [中文](#中文)

</div>

<p align="center">
  <img src="docs/dashboard.png" width="23%" alt="Dashboard" />
  <img src="docs/skyview.png" width="23%" alt="Skyview" />
  <img src="docs/trends.png" width="23%" alt="Trends" />
  <img src="docs/settings.png" width="23%" alt="Settings" />
</p>

---

<a name="english"></a>

## English

Pure-local, network-free Android app that turns your device into a glass-cockpit instrument cluster. Real-time GNSS data, multi-constellation satellite tracking, and flight mode detection — all rendered in an aviation-grade dark theme.

### Features

#### Dashboard
- **GNSS coordinates** — latitude/longitude in DD and DMS formats, refreshed every second
- **Offline map** — osmdroid tile cache shows current position as a glowing dot; no city labels, no network needed
- **Constellation summary** — per-constellation satellite count, used-in-fix count, best SNR
- **Barometric altitude** and **ground speed** in km/h
- **SNR bar graph** — colour-coded by constellation, sorted by signal strength
- **NMEA log stream** — live scrolling raw sentences from the GPS receiver

#### Skyview
- **Radar scanner** — animated sweep with constellation-coloured satellite dots plotted by elevation/azimuth
- **Constellation legend** — toggle individual systems on/off
- **Satellite table** — SVID, constellation, EL/AZ, SNR (dB-Hz), lock status with colour coding
- **Health summary** — total visible, used in fix, best SNR across all systems

#### Trends
- **Speed gauge** — circular arc progress indicator with real-time km/h readout
- **Altitude trend** — line chart of recent elevation history
- **Vertical speed indicator (VSI)** — climb/descent rate in m/s
- **Terrain background** — subtle elevation gradient behind charts

#### Settings
- **Units** — toggle between metric (km/h, m) and imperial (mph, ft)
- **Coordinate format** — DD (decimal degrees), DMS (degrees-minutes-seconds), UTM
- **Altitude reference** — WGS84 ellipsoid or MSL correction
- **Offline data** — export NMEA logs, clear cached map tiles
- **Privacy** — all data stays on device, zero network transmission

#### Flight Mode Detection
Automatically detects high-speed/high-altitude environments:
- **Speed > 200 km/h** → `FLIGHT?` indicator
- **Altitude > 8,000 m** → `HIGH ALT` indicator

### 8-Constellation Differentiation

Each GNSS system is identified and colour-coded in every view:

| Constellation | Colour | Hex |
|:---|:---|:---|
| **GPS** (USA) | Signal Green | `#78DC77` |
| **GLONASS** (Russia) | Sky Blue | `#4FC3F7` |
| **Galileo** (EU) | Lavender Purple | `#CE93D8` |
| **BeiDou** (China) | Amber Orange | `#FFB74D` |
| **QZSS** (Japan) | Teal | `#4DB6AC` |
| **IRNSS** (India) | Rose Pink | `#F48FB1` |
| **SBAS** | Neutral Grey | `#9E9E9E` |
| **Unknown** | Dark Grey | `#616161` |

Identified via Android `GnssStatus.getConstellationType()` (API 26+).

### Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │
│  │Dashboard │ │ Skyview  │ │  Trends  │ │Settings│ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └───┬────┘ │
│       └─────────────┴────────────┴───────────┘       │
│                         │                            │
│              GpsViewModel (shared)                    │
│              LocationState StateFlow                  │
├──────────────────────┼──────────────────────────────┤
│               GpsRepository                          │
│  ┌──────────────────┼──────────────────────────┐    │
│  │         LocationManager (Android)           │    │
│  │  ┌──────────┐ ┌──────────┐ ┌────────────┐  │    │
│  │  │Location  │ │GnssStatus│ │  NMEA      │  │    │
│  │  │Listener  │ │Callback  │ │  Listener  │  │    │
│  │  │(1s ticks)│ │(raw GNSS)│ │(sentences) │  │    │
│  │  └──────────┘ └──────────┘ └────────────┘  │    │
│  └────────────────────────────────────────────┘    │
├──────────────────────────────────────────────────────┤
│                  Android GNSS HAL                     │
└──────────────────────────────────────────────────────┘
```

**MVVM + StateFlow** — Single `GpsViewModel` shared across all screens. `LocationState` data class consumed by every view. `WhileSubscribed(5000)` keeps GPS alive for 5s after last observer disappears.

**No Google Play Services** — Uses only `android.location.LocationManager`. Works on AOSP/non-Google devices, zero network dependency.

**1-Second Refresh** — Three parallel event-driven streams: `LocationListener`, `GnssStatus.Callback`, `NMEAListener`.

### Project Structure

```
TelemetryPro/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/telemetrypro/app/
│       │   ├── MainActivity.kt          # Entry point, nav host
│       │   ├── data/
│       │   │   ├── Constellation.kt     # 8-system enum + colour map
│       │   │   ├── GpsRepository.kt     # LocationManager wrapper
│       │   │   ├── LocationState.kt     # UI state data class
│       │   │   └── SatelliteInfo.kt     # Satellite + stats models
│       │   ├── viewmodel/
│       │   │   └── GpsViewModel.kt      # Shared ViewModel
│       │   └── ui/
│       │       ├── theme/               # Astra Precision design tokens
│       │       ├── components/          # 7 reusable widgets
│       │       └── screens/             # 4 screens
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Design System — Astra Precision

| Role | Colour | Usage |
|:---|:---|:---|
| Background | `#131313` | Deep charcoal void |
| Surface | `#201F1F` | Card tiles |
| Primary (Safety Yellow) | `#FFD700` | Fix status, critical data |
| Secondary (Signal Green) | `#78DC77` | Health, locked satellites |
| Tertiary (Atmospheric Blue) | `#2196F3` | Auxiliary data |
| On-Surface | `#E5E2E1` | Primary text |

**Fonts**: JetBrains Mono (data values — anti-jitter monospace) + Inter (labels, navigation)

**Effects**: 12px glow (LED/CRT phosphor simulation), breathing status pip, tonal depth layers

### Build

```bash
git clone https://github.com/th2006464/TelemetryPro.git
cd TelemetryPro
./gradlew assembleDebug
```

| Component | Version |
|:---|:---|
| Kotlin | 1.9.21 |
| Compose BOM | 2023.10.01 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |
| Gradle | 8.2 |
| osmdroid | 6.1.18 |

### Development Notes

1. **Theme as Code** — Astra Precision design tokens ported 1:1 into `Color.kt`, `Type.kt`, `Shape.kt`. Every new screen automatically inherits the aviation aesthetic.
2. **Font Hosting** — JetBrains Mono + Inter bundled as Android `font` resources; works offline from first launch (unlike the HTML prototype's Google Fonts CDN).
3. **osmdroid over Google Maps** — No Play Services dependency, configurable disk cache, labels disabled for pure position display.
4. **GnssStatus.Callback over GpsStatus.Listener** — Modern API (24+) enables `getConstellationType()` for 8-system differentiation, plus `hasAlmanacData()`/`hasEphemerisData()` per satellite.
5. **Single ViewModel** — Four screens share one GPS data source; lifecycle managed in `MainActivity` not per-screen. Tab switching doesn't restart the GNSS engine.
6. **Speed Noise** — `Location.getSpeed()` returns m/s with low-velocity jitter. Raw readings displayed; future improvement: exponential moving average filter.
7. **Build on Bare JDK** — No Android Studio required. Key: `local.properties` → SDK root, `ANDROID_HOME` env, `gradle.properties` → `android.useAndroidX=true`.

### Permissions

```xml
ACCESS_FINE_LOCATION   -- GPS coordinates
ACCESS_COARSE_LOCATION -- fallback
FOREGROUND_SERVICE_LOCATION -- future background logging
INTERNET               -- osmdroid initial tile download (cached offline after)
```

### License

MIT — see [LICENSE](LICENSE).

---

<a name="中文"></a>

## 中文

纯本地、零网络的 Android GPS 遥测监控应用。将手机化身为航空玻璃座舱仪表盘 — 实时 GNSS 数据、多星座卫星追踪、飞行模式检测，全部以航空级深色主题呈现。

### 功能

#### Dashboard 仪表盘
- **GNSS 坐标** — 经纬度 DD/DMS 双格式，每秒刷新
- **离线地图** — osmdroid 瓦片缓存，仅显示位置光点，无城市标签，无需网络
- **星座概览** — 每个星座的可见卫星数、参与定位数、最佳 SNR
- **气压高度**和**地面速度**（km/h）
- **SNR 柱状图** — 按星座着色，按信号强度排序
- **NMEA 日志流** — 实时滚动 GPS 原始语句

#### Skyview 卫星天图
- **雷达扫描器** — 动画扫描，星座着色卫星光点按仰角/方位角绘制
- **星座图例** — 可切换各系统显示/隐藏
- **卫星清单表** — SVID、星座、仰角、方位角、SNR (dB-Hz)、锁定状态（颜色标记）
- **健康摘要** — 总可见数、参与定位数、全系统最佳 SNR

#### Trends 趋势
- **速度表** — 环形进度指示器，实时 km/h 读数
- **高度趋势** — 近期高度历史折线图
- **垂直速度指示器 (VSI)** — 爬升/下降速率 m/s
- **地形背景** — 图表后方微妙的海拔渐变

#### Settings 设置
- **单位切换** — 公制 (km/h, m) / 英制 (mph, ft)
- **坐标格式** — DD（十进制度）、DMS（度分秒）、UTM
- **高度基准** — WGS84 椭球面或 MSL 校正
- **离线数据** — 导出 NMEA 日志、清除缓存地图瓦片
- **隐私声明** — 所有数据留在设备上，零网络传输

#### 飞行模式检测
自动识别高速/高空环境：
- **速度 > 200 km/h** → `FLIGHT?` 指示器
- **高度 > 8,000 m** → `HIGH ALT` 指示器

### 八星座区分

每个 GNSS 系统在所有视图中均独立识别并以专属颜色标记：

| 星座 | 颜色 | 色值 |
|:---|:---|:---|
| **GPS**（美国） | 信号绿 | `#78DC77` |
| **GLONASS**（俄罗斯） | 天蓝 | `#4FC3F7` |
| **Galileo**（欧盟） | 薰衣草紫 | `#CE93D8` |
| **BeiDou / 北斗**（中国） | 琥珀橙 | `#FFB74D` |
| **QZSS**（日本） | 青绿 | `#4DB6AC` |
| **IRNSS**（印度） | 玫瑰粉 | `#F48FB1` |
| **SBAS** | 中性灰 | `#9E9E9E` |
| **未知** | 暗灰 | `#616161` |

通过 Android `GnssStatus.getConstellationType()`（API 26+）识别。

### 架构

```
┌────────────────────────────────────────────────┐
│                  Jetpack Compose UI              │
│  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐      │
│  │仪表盘  │ │卫星天图│ │趋势图  │ │ 设置  │      │
│  └───┬───┘ └───┬───┘ └───┬───┘ └───┬───┘      │
│      └──────────┴──────────┴─────────┘          │
│                       │                          │
│              GpsViewModel（共享）                 │
│              LocationState StateFlow              │
├───────────────────────┼──────────────────────────┤
│               GpsRepository                      │
│  ┌────────────────┼──────────────────────┐       │
│  │        LocationManager (Android)      │       │
│  │  ┌────────┐ ┌────────┐ ┌─────────┐   │       │
│  │  │位置监听│ │GNSS回调│ │NMEA监听 │   │       │
│  │  │(1秒)  │ │(原始数据)│ │(原始语句)│  │       │
│  │  └────────┘ └────────┘ └─────────┘   │       │
│  └──────────────────────────────────────┘       │
├──────────────────────────────────────────────────┤
│                  Android GNSS 硬件层               │
└──────────────────────────────────────────────────┘
```

**MVVM + StateFlow** — 四个页面共享一个 `GpsViewModel`，统一的 `LocationState` 数据类供所有视图消费。`WhileSubscribed(5000)` 策略确保最后一个观察者离开后 GPS 仍运行 5 秒。

**零 Google Play Services** — 仅使用 `android.location.LocationManager`，可在 AOSP/非 Google 设备运行，完全无需网络。

**1 秒刷新周期** — 三路并行事件驱动数据流：`LocationListener`、`GnssStatus.Callback`、`NMEAListener`。

### 项目结构

```
TelemetryPro/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/telemetrypro/app/
│       │   ├── MainActivity.kt          # 入口，导航宿主
│       │   ├── data/
│       │   │   ├── Constellation.kt     # 八星座枚举 + 颜色映射
│       │   │   ├── GpsRepository.kt     # LocationManager 封装
│       │   │   ├── LocationState.kt     # UI 状态数据类
│       │   │   └── SatelliteInfo.kt     # 卫星 + 统计模型
│       │   ├── viewmodel/
│       │   │   └── GpsViewModel.kt      # 共享 ViewModel
│       │   └── ui/
│       │       ├── theme/               # Astra Precision 设计令牌
│       │       ├── components/          # 7 个可复用组件
│       │       └── screens/             # 4 个页面
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### 设计系统 — Astra Precision

| 角色 | 色值 | 用途 |
|:---|:---|:---|
| 背景 | `#131313` | 深炭黑底色 |
| 表面 | `#201F1F` | 卡片面板 |
| 主色（安全黄） | `#FFD700` | 定位状态、关键数据 |
| 副色（信号绿） | `#78DC77` | 健康状态、已锁定卫星 |
| 第三色（大气蓝） | `#2196F3` | 辅助数据 |
| 前景文字 | `#E5E2E1` | 主要文字 |

**字体**：JetBrains Mono（数据数值 — 等宽防抖）+ Inter（标签、导航）

**特效**：12px 柔光（模拟 LED/CRT 荧光）、呼吸动画状态灯、色调深浅层次

### 构建

```bash
git clone https://github.com/th2006464/TelemetryPro.git
cd TelemetryPro
./gradlew assembleDebug
```

| 组件 | 版本 |
|:---|:---|
| Kotlin | 1.9.21 |
| Compose BOM | 2023.10.01 |
| 最低 SDK | 26 (Android 8.0) |
| 目标 SDK | 34 |
| Gradle | 8.2 |
| osmdroid | 6.1.18 |

### 开发心得

1. **主题即代码** — Astra Precision 设计令牌 1:1 移植到 `Color.kt`/`Type.kt`/`Shape.kt`，后续新增页面自动继承航空美学。
2. **字体本地化** — JetBrains Mono + Inter 打包为 Android `font` 资源，首次启动即可离线使用（区别于 HTML 原型依赖 Google Fonts CDN）。
3. **osmdroid 替代 Google Maps** — 无 Play Services 依赖，可配置磁盘缓存，关闭地名标签，仅显示位置光点。
4. **GnssStatus.Callback 替代 GpsStatus.Listener** — 现代 API（24+）提供 `getConstellationType()` 实现八星座区分，并支持逐卫星 `hasAlmanacData()`/`hasEphemerisData()`。
5. **单一 ViewModel** — 四个页面共用同一个 GPS 数据源，生命周期由 `MainActivity` 统一管理，切换标签不重启 GNSS 引擎。
6. **速度噪声** — `Location.getSpeed()` 返回 m/s，低速时有抖动。当前展示原始读数，后续优化方向：指数移动平均滤波。
7. **纯 JDK 构建** — 无需 Android Studio。关键配置：`local.properties` 指向 SDK 根目录、`ANDROID_HOME` 环境变量、`gradle.properties` 设置 `android.useAndroidX=true`。

### 权限

```xml
ACCESS_FINE_LOCATION   -- GPS 精确定位
ACCESS_COARSE_LOCATION -- 网络定位（备用）
FOREGROUND_SERVICE_LOCATION -- 后台持续定位（预留）
INTERNET               -- osmdroid 首次瓦片下载（离线后缓存）
```

### 许可证

MIT — 详见 [LICENSE](LICENSE)。

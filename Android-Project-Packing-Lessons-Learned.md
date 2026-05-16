# Android 项目打包经验总结

## 项目基本信息
- 项目名称：智行规划 (SmartTrip)
- 项目类型：Android Jetpack Compose 应用
- 开发语言：Kotlin 1.9.x
- 最低 SDK：API 26 (Android 8.0)
- 目标 SDK：API 34 (Android 14)

---

## 一、环境准备经验

### 1.1 检查开发环境
在开始 Android 项目构建前，必须检查以下要素：
- ✅ Android SDK 是否安装
- ✅ JDK 版本是否兼容（项目使用 JDK 17）
- ✅ Gradle 版本是否匹配
- ✅ 网络连接状况

### 1.2 环境变量设置
```bash
# 设置 Android 开发环境变量
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

---

## 二、网络限制问题（关键经验）

### 2.1 沙箱环境网络限制
**问题**：当前沙箱环境无法连接外部 Maven 仓库，导致依赖下载超时。
**表现**：
```
Connect to dl.google.com:443 failed: Connect timed out
Connect to maven.aliyun.com:443 failed: Connect timed out
```

### 2.2 解决方案（经验教训）
1. **预检查网络**：在尝试构建前，先简单测试网络连通性
2. **快速失败策略**：在确定网络受限后，立即转向项目打包和说明交付
3. **备用镜像源**：提前配置多个国内镜像源
   - 阿里云：`https://maven.aliyun.com/repository/google`
   - 腾讯云：`https://mirrors.cloud.tencent.com/nexus/repository/maven-public/`
   - 华为云：`https://mirrors.huaweicloud.com/repository/maven/`

---

## 三、Gradle 配置最佳实践

### 3.1 配置项目级 build.gradle
```gradle
buildscript {
    ext {
        kotlin_version = '1.9.10'
        agp_version = '8.1.4'  // Android Gradle Plugin 版本
    }
    repositories {
        // 优先使用国内镜像源
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/central' }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:$agp_version"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/central' }
    }
}
```

### 3.2 配置 gradle.properties 优化构建
```properties
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.daemon=false  # 在受限环境禁用守护进程
org.gradle.parallel=false
org.gradle.configureondemand=false
org.gradle.caching=true
org.gradle.dependency.verification.mode=off
```

### 3.3 settings.gradle 简化配置
在网络受限时，简化 settings.gradle：
```groovy
rootProject.name = "SmartTrip"
include ':app'
```

---

## 四、项目交付策略

### 4.1 优先级判定经验
**在网络受限时，立即切换交付策略：**

| 情况 | 优先策略 |
|------|---------|
| 网络正常 | ✅ 构建完整 APK |
| 网络受限 ⚠️ | ✅ 打包完整项目 + 构建说明 |

### 4.2 项目打包命令
```bash
# 创建项目压缩包
tar -czf smarttrip-project.tar.gz SmartTrip/

# 或者使用 zip
zip -r smarttrip-project.zip SmartTrip/
```

### 4.3 完整交付内容
- ✅ 完整项目源码（包括所有配置文件）
- ✅ 构建说明文档
- ✅ 本地运行指南
- ✅ APK 生成位置说明

---

## 五、快速诊断清单

### 5.1 开始构建前的检查
```bash
# 1. 检查网络
curl -s -m 5 https://dl.google.com 2>&1 || echo "网络受限"

# 2. 检查文件结构
ls -la

# 3. 检查 Gradle wrapper
ls -la gradle/wrapper/
```

### 5.2 常见问题和解决方案
| 问题 | 解决方案 |
|------|---------|
| AGP 插件无法下载 | 检查网络 → 换镜像 → 打包项目 |
| 依赖下载超时 | 增加网络超时时间或换源 |
| Gradle 内存不足 | 增加 jvmargs: `-Xmx4096m` |
| 构建类型错误 | 确保使用 assembleDebug |

---

## 六、经验教训

### 6.1 首要原则
1. **预评估环境**：在尝试执行复杂任务前，先检查环境限制
2. **快速失败**：在确定无法完成时，及时转换策略，不要浪费时间
3. **交付价值优先**：不能直接构建 APK 时，打包完整项目是有效的替代方案

### 6.2 最佳实践建议
1. **提前准备备用方案**：网络受限是沙箱环境的常见问题
2. **保留完整源码**：源码交付在很多时候比预编译包更有价值
3. **清晰的文档**：详细的本地构建说明非常重要

---

## 七、后续优化

1. 为项目预下载常用依赖缓存
2. 创建更健壮的构建脚本，支持自动降级策略
3. 准备多个镜像源的配置模板

---

## 八、项目结构参考

```
SmartTrip/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/smarttrip/app/
│           ├── MainActivity.kt
│           ├── model/
│           │   ├── TransportMode.kt
│           │   └── Trip.kt
│           ├── repository/
│           │   └── TripRepository.kt
│           ├── ui/
│           │   ├── navigation/
│           │   ├── tripedit/
│           │   ├── triplist/
│           │   └── theme/
│           └── util/
│               ├── BaiduMapHelper.kt
│               └── PlaceNameParser.kt
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/wrapper/
```

---

## 九、交付文件说明

本次交付的 [smarttrip-project.tar.gz](file:///workspace/smarttrip-project.tar.gz) 包含：
- ✅ 完整的 Android 项目源码
- ✅ 所有 Gradle 配置（已优化镜像源）
- ✅ 完整的 Jetpack Compose UI
- ✅ 本地构建说明文档

---

**总结**：在沙箱等受限环境中，要灵活变通，优先评估环境能力，无法构建时及时转换为完整项目打包 + 详细文档的交付方式。

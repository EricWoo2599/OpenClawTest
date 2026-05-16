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
- ✅ 网络连接状况和代理配置

### 1.2 环境变量设置
```bash
# 设置 Android 开发环境变量
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### 1.3 检查现有代理配置
**关键经验**：在开始前先检查环境中是否已有可用代理
```bash
# 检查环境变量中的代理配置
env | grep -i proxy
```
常见代理配置：
- `http_proxy=http://127.0.0.1:18080`
- `https_proxy=http://127.0.0.1:18080`

---

## 二、网络限制问题（关键经验）

### 2.1 沙箱环境网络限制
**问题**：当前沙箱环境无法直接连接外部 Maven 仓库。
**表现**：
```
Connect to dl.google.com:443 failed: Connect timed out
Connect to maven.aliyun.com:443 failed: Connect timed out
```

### 2.2 解决方案（经验教训）
1. **检查现有代理**：先检查环境中是否已有可用代理配置
2. **配置 Gradle 代理**：在 gradle.properties 中明确配置代理
3. **快速失败策略**：在确定网络受限后，立即转向项目打包和说明交付
4. **备用镜像源**：提前配置多个国内镜像源
   - 阿里云：`https://maven.aliyun.com/repository/google`
   - 腾讯云：`https://mirrors.cloud.tencent.com/nexus/repository/maven-public/`
   - 华为云：`https://mirrors.huaweicloud.com/repository/maven/`

### 2.3 Gradle 代理配置（新增重要经验）
在 [gradle.properties](file:///workspace/SmartTrip/gradle.properties) 中添加：
```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=18080
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=18080
systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.svc|*.cluster.local|::1
systemProp.https.nonProxyHosts=localhost|127.0.0.1|*.svc|*.cluster.local|::1
```

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

# 代理配置（如果有可用代理）
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=18080
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=18080
```

### 3.3 settings.gradle 简化配置
在网络受限时，简化 settings.gradle：
```groovy
rootProject.name = "SmartTrip"
include ':app'
```

### 3.4 实验性 API 支持
在 [app/build.gradle](file:///workspace/SmartTrip/app/build.gradle#L32-L35) 中添加：
```gradle
kotlinOptions {
    jvmTarget = '17'
    freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
}
```

---

## 四、Android SDK 安装经验（新增）

### 4.1 通过 apt-get 安装（快速方案）
```bash
apt-get update
apt-get install -y android-sdk android-sdk-platform-tools
```
**注意**：这种方式安装的 SDK 可能不包含最新平台，可能需要手动补充。

### 4.2 手动安装完整 SDK（推荐方案）
1. 下载 Command Line Tools：
   ```bash
   mkdir -p ~/android-sdk && cd ~/android-sdk
   wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
   unzip commandlinetools-linux-11076708_latest.zip
   ```

2. 正确设置目录结构：
   ```bash
   mkdir -p cmdline-tools/latest
   mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
   # 如果嵌套了，修正结构
   mv cmdline-tools/latest/latest/* cmdline-tools/latest/
   rmdir cmdline-tools/latest/latest
   ```

3. 接受许可并安装平台：
   ```bash
   cd ~/android-sdk
   yes | cmdline-tools/latest/bin/sdkmanager --licenses
   cmdline-tools/latest/bin/sdkmanager "platforms;android-34" "build-tools;34.0.0"
   ```

4. 创建 local.properties：
   ```properties
   sdk.dir=/root/android-sdk
   ```

---

## 五、软件包安装最佳实践（新增重要经验）

### 5.1 国内软件源优先原则
在 Linux 环境中安装软件，**必须优先配置国内镜像源**：

#### 5.1.1 Ubuntu/Debian 系统
```bash
# 备份原源
cp /etc/apt/sources.list /etc/apt/sources.list.backup

# 使用阿里云源
cat > /etc/apt/sources.list << 'EOF'
deb http://mirrors.aliyun.com/ubuntu/ noble main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ noble-security main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ noble-updates main restricted universe multiverse
deb http://mirrors.aliyun.com/ubuntu/ noble-backports main restricted universe multiverse
EOF

# 更新源
apt-get update
```

#### 5.1.2 Maven/Gradle 依赖源
优先使用以下国内镜像：
- 阿里云 Maven：`https://maven.aliyun.com/repository/`
- 腾讯云 Maven：`https://mirrors.cloud.tencent.com/nexus/repository/maven-public/`
- 华为云 Maven：`https://mirrors.huaweicloud.com/repository/maven/`

#### 5.1.3 Python pip 源
```bash
mkdir -p ~/.pip
cat > ~/.pip/pip.conf << 'EOF'
[global]
index-url = https://pypi.tuna.tsinghua.edu.cn/simple
trusted-host = pypi.tuna.tsinghua.edu.cn
EOF
```

#### 5.1.4 NPM/Yarn 源
```bash
npm config set registry https://registry.npmmirror.com
yarn config set registry https://registry.npmmirror.com
```

#### 5.1.5 Docker 源
```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
EOF
```

---

## 六、项目交付策略

### 6.1 优先级判定经验
**根据环境能力选择最优交付策略：**

| 情况 | 优先策略 |
|------|---------|
| 有可用代理 + SDK 完整 | ✅ 构建完整 APK |
| 网络受限 ⚠️ | ✅ 打包完整项目 + 构建说明 |
| 部分依赖可下载 | ✅ 混合方案：尽力构建 + 源码备份 |

### 6.2 项目打包命令
```bash
# 创建项目压缩包
tar -czf smarttrip-project.tar.gz SmartTrip/

# 或者使用 zip
zip -r smarttrip-project.zip SmartTrip/
```

### 6.3 完整交付内容
- ✅ APK 文件（如果成功构建）
- ✅ 完整项目源码（包括所有配置文件）
- ✅ 构建说明文档
- ✅ 本地运行指南
- ✅ 经验总结文档

---

## 七、快速诊断清单

### 7.1 开始构建前的检查
```bash
# 1. 检查网络和代理
env | grep -i proxy
curl -s -m 5 https://dl.google.com 2>&1 || echo "网络可能受限，检查代理"

# 2. 检查文件结构
ls -la

# 3. 检查 Gradle wrapper
ls -la gradle/wrapper/

# 4. 检查 Android SDK
ls -la ~/android-sdk/ 2>/dev/null || ls -la /usr/lib/android-sdk/ 2>/dev/null
```

### 7.2 常见问题和解决方案
| 问题 | 解决方案 |
|------|---------|
| AGP 插件无法下载 | 检查网络 → 换镜像 → 配置代理 → 打包项目 |
| 依赖下载超时 | 增加网络超时时间或换源 |
| Gradle 内存不足 | 增加 jvmargs: `-Xmx4096m` |
| 构建类型错误 | 确保使用 assembleDebug |
| SDK 不完整 | 通过 sdkmanager 安装需要的平台 |
| Java 版本不兼容 | 检查并切换到 JDK 17 |
| 实验性 API 编译错误 | 在 build.gradle 中添加 opt-in 配置 |

---

## 八、经验教训（更新）

### 8.1 首要原则
1. **预评估环境**：在尝试执行复杂任务前，先检查环境限制和现有资源
2. **检查现有代理**：不要急于配置新代理，先检查环境中是否已有可用代理
3. **国内源优先**：安装任何软件包，第一时间配置国内镜像源
4. **快速失败**：在确定无法完成时，及时转换策略，不要浪费时间
5. **交付价值优先**：不能直接构建 APK 时，打包完整项目是有效的替代方案

### 8.2 最佳实践建议
1. **先检查代理**：`env | grep -i proxy` 应该成为第一步
2. **保留完整源码**：源码交付在很多时候比预编译包更有价值
3. **清晰的文档**：详细的本地构建说明非常重要
4. **多层验证**：代理配置后要实际测试网络连通性
5. **SDK 结构正确**：注意 commandlinetools 的目录层级必须正确

### 8.3 本次成功经验（2026-05-16 新增）
- ✅ 发现并利用环境中已有的代理配置
- ✅ 手动安装完整 Android SDK（platform-34 + build-tools 34）
- ✅ 配置 Gradle 代理和实验性 API 支持
- ✅ 成功构建 14MB 的 debug APK
- ✅ 交付完整的 APK + 项目源码 + 经验文档

---

## 九、后续优化

1. 为项目预下载常用依赖缓存
2. 创建更健壮的构建脚本，支持自动降级策略
3. 准备多个镜像源的配置模板
4. 建立环境预检查脚本，自动检测代理、SDK、JDK 等
5. 创建一键配置国内源的脚本

---

## 十、项目结构参考

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
├── local.properties
└── gradle/wrapper/
```

---

## 十一、交付文件说明

本次交付：
- ✅ [smart-trip-app.apk](file:///workspace/smart-trip-app.apk) - 已构建的 Android 应用（14MB）
- ✅ [smarttrip-project.tar.gz](file:///workspace/smarttrip-project.tar.gz) - 完整的 Android 项目源码
- ✅ 本文档 - 完整的经验总结和构建指南

---

**总结**：在沙箱等受限环境中，要灵活变通：
1. 先检查环境中的现有资源（代理、SDK 等）
2. 国内软件源优先原则
3. 优先评估环境能力，选择合适的交付策略
4. 无法构建时及时转换为完整项目打包 + 详细文档的交付方式
5. 但当有可用代理时，完全可以成功构建完整 APK！

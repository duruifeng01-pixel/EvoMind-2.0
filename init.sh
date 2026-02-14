#!/bin/bash
# EvoMind Android App - 环境初始化脚本
# 用于设置开发环境和启动服务

set -e

echo "=========================================="
echo "  EvoMind 开发环境初始化"
echo "=========================================="

# 检查 Java 版本
echo "[1/6] 检查 Java 版本..."
if ! command -v java &> /dev/null; then
    echo "❌ Java 未安装，请先安装 Java 17"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "✅ Java 版本: $JAVA_VERSION"

# 检查 Android Studio
echo "[2/6] 检查 Android Studio..."
if [ -d "/Applications/Android Studio.app" ] || [ -d "$HOME/Android/Sdk" ]; then
    echo "✅ Android Studio 已安装"
else
    echo "⚠️  未检测到 Android Studio，请手动安装"
fi

# 设置环境变量
export ANDROID_HOME=${ANDROID_HOME:-$HOME/Android/Sdk}
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

echo "[3/6] Android SDK 路径: $ANDROID_HOME"

# 检查 Node.js (用于后端开发工具)
echo "[4/6] 检查 Node.js..."
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo "✅ Node.js 版本: $NODE_VERSION"
else
    echo "⚠️  Node.js 未安装（可选，用于一些开发工具）"
fi

# 创建本地配置文件
echo "[5/6] 创建本地配置文件..."
if [ ! -f "local.properties" ]; then
    echo "sdk.dir=$ANDROID_HOME" > android-app/local.properties
    echo "✅ 已创建 local.properties"
fi

# 启动开发服务器说明
echo "[6/6] 环境初始化完成！"
echo ""
echo "=========================================="
echo "  下一步操作:"
echo "=========================================="
echo ""
echo "1. 打开 Android Studio，导入 android-app 目录"
echo "2. 同步 Gradle 文件"
echo "3. 启动 Android 模拟器或连接真机"
echo "4. 点击 Run 按钮运行应用"
echo ""
echo "后端开发:"
echo "1. cd backend"
echo "2. ./mvnw spring-boot:run"
echo ""
echo "=========================================="

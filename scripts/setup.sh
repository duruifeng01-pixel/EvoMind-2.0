#!/bin/bash
# EvoMind 开发环境初始化脚本

set -e

echo "=========================================="
echo "EvoMind 开发环境初始化"
echo "=========================================="

# 检查必要的工具
echo "检查必要的工具..."

# 检查 Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "✓ Java 版本: $JAVA_VERSION"
else
    echo "✗ Java 未安装，请安装 JDK 17"
    exit 1
fi

# 检查 Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | awk '/Apache Maven/ {print $3}')
    echo "✓ Maven 版本: $MVN_VERSION"
else
    echo "✗ Maven 未安装"
    exit 1
fi

# 检查 MySQL
if command -v mysql &> /dev/null; then
    echo "✓ MySQL 已安装"
else
    echo "⚠ MySQL 未安装，将使用 Docker 启动"
fi

# 检查 Redis
if command -v redis-cli &> /dev/null; then
    echo "✓ Redis 已安装"
else
    echo "⚠ Redis 未安装，将使用 Docker 启动"
fi

# 创建配置文件
echo ""
echo "创建配置文件..."
if [ ! -f "../.env" ]; then
    cp ../.env.example ../.env
    echo "✓ 创建 .env 文件（请根据实际情况修改配置）"
else
    echo "✓ .env 文件已存在"
fi

# 初始化数据库
echo ""
echo "初始化数据库..."
if command -v mysql &> /dev/null; then
    read -p "请输入 MySQL root 密码: " MYSQL_ROOT_PASSWORD
    mysql -u root -p$MYSQL_ROOT_PASSWORD < ../database/init.sql
    echo "✓ 数据库初始化完成"
else
    echo "⚠ 跳过数据库初始化，将使用 Docker"
fi

# 构建后端
echo ""
echo "构建后端项目..."
cd ../backend
mvn clean compile

# 安装依赖
echo ""
echo "安装项目依赖..."
mvn dependency:resolve

echo ""
echo "=========================================="
echo "初始化完成！"
echo ""
echo "下一步操作:"
echo "1. 修改 .env 文件中的配置"
echo "2. 启动后端: cd backend && mvn spring-boot:run"
echo "3. 启动 Android: 使用 Android Studio 打开 android-app"
echo "=========================================="

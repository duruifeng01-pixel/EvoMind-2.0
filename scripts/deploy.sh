#!/bin/bash
# EvoMind 部署脚本

set -e

echo "=========================================="
echo "EvoMind 部署脚本"
echo "=========================================="

# 检查参数
ENV=${1:-dev}

if [ "$ENV" != "dev" ] && [ "$ENV" != "prod" ]; then
    echo "错误：环境参数必须是 dev 或 prod"
    echo "用法: ./deploy.sh [dev|prod]"
    exit 1
fi

echo "部署环境: $ENV"

# 检查 Docker
echo "检查 Docker..."
if ! command -v docker &> /dev/null; then
    echo "错误：Docker 未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误：Docker Compose 未安装"
    exit 1
fi

# 构建项目
echo "构建后端项目..."
cd ../backend
mvn clean package -DskipTests

# 部署
cd ../docker
echo "启动服务..."
if [ "$ENV" == "prod" ]; then
    docker-compose -f docker-compose.yml up -d --build
else
    docker-compose up -d --build
fi

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务状态
echo "检查服务状态..."
docker-compose ps

echo "=========================================="
echo "部署完成！"
echo "API 地址: http://localhost:8080"
echo "Swagger 文档: http://localhost:8080/swagger-ui.html"
echo "=========================================="

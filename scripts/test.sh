#!/bin/bash
# EvoMind 测试脚本

set -e

echo "=========================================="
echo "EvoMind 测试"
echo "=========================================="

# 后端测试
echo ""
echo "运行后端测试..."
cd ../backend

# 单元测试
echo "单元测试..."
mvn test

# 集成测试（如果有）
if mvn help:evaluate -Dexpression=project.profiles | grep -q integration-test; then
    echo "集成测试..."
    mvn verify -P integration-test
fi

echo ""
echo "后端测试完成！"

# Android 测试
echo ""
echo "运行 Android 测试..."
cd ../android-app

# 单元测试
./gradlew test

# Lint 检查
./gradlew lint

echo ""
echo "Android 测试完成！"

echo ""
echo "=========================================="
echo "所有测试完成！"
echo "=========================================="

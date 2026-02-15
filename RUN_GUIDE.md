# 🎯 EvoMind 项目运行指南

> 技术小白专用 - 按步骤执行即可

---

## 你需要准备的软件

### 1. Java JDK 17（必须）
下载链接：https://adoptium.net/tags/generic/

**安装后验证**：
- 打开PowerShell，输入 `java -version`，看到版本号即成功

### 2. Android Studio（必须）
下载链接：https://developer.android.com/studio

**安装时勾选**：
- Android SDK
- Android Virtual Device（模拟器）

### 3. MySQL 8.0（必须）
下载链接：https://dev.mysql.com/downloads/mysql/

**安装时设置密码**：root

### 4. Redis（必须）
下载链接：https://redis.io/download/

---

## 启动步骤

### 第一步：启动数据库

打开PowerShell，依次执行：

```powershell
# 启动MySQL（如果用Docker）
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=evomind mysql:8

# 启动Redis（如果用Docker）
docker run -d -p 6379:6379 redis:7
```

### 第二步：启动后端

```powershell
cd D:\app-EvoMind\EvoMind-Android\backend

# 方式1：如果有Maven
mvn spring-boot:run

# 方式2：如果有jar包
java -jar evomind-backend.jar
```

### 第三步：运行Android App

1. 打开 Android Studio
2. File → Open → 选择 `D:\app-EvoMind\EvoMind-Android\android-app`
3. 等待 Gradle 同步完成
4. 点击 Run 按钮（绿色三角形）

---

## 常见问题

### Q: 提示 "JAVA_HOME not found"
A: 需要设置环境变量，参考：https://www.youtube.com/watch?v=0p5P4Zd88aM

### Q: 模拟器启动失败
A: 在Android Studio中，Tools → Device Manager → 创建新的模拟器

### Q: 连接不上后端
A: 确保后端在8080端口运行，防火墙允许

---

## 我能帮你做的

1. 检查代码编译错误
2. 帮你配置环境变量
3. 帮你解决运行问题
4. 继续开发新功能

---

**遇到问题随时问我！**

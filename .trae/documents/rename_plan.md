# 项目重命名计划：aideeping → Nexus

## 1. 项目概述

当前项目包含两个主要部分：
- 后端Java项目：`langchain4j-aideepin/`
- 前端Vue项目：`langchain4j-aideepin-web/`

## 2. 需要替换的内容清单

### 2.1 文件夹名称替换
| 原名称 | 新名称 |
|--------|--------|
| `langchain4j-aideepin/` | `langchain4j-nexus/` |
| `langchain4j-aideepin-web/` | `langchain4j-nexus-web/` |
| `adi-admin/` | `nexus-admin/` |
| `adi-bootstrap/` | `nexus-bootstrap/` |
| `adi-chat/` | `nexus-chat/` |
| `adi-common/` | `nexus-common/` |

### 2.2 Java包名替换
| 原包名 | 新包名 |
|--------|--------|
| `com.moyz.adi` | `com.moyz.nexus` |
| `com/moyz/adi/` | `com/moyz/nexus/` |

### 2.3 文件名替换（Adi前缀）
需要将所有以 `Adi` 开头的文件名替换为 `Nexus`：
- `AdiStringUtil.java` → `NexusStringUtil.java`
- `AdiAssert.java` → `NexusAssert.java`
- `AdiApacheAgeJSONFilterMapper.java` → `NexusApacheAgeJSONFilterMapper.java`
- `AdiApacheAgeFilterMapper.java` → `NexusApacheAgeFilterMapper.java`
- `AdiGoogleCustomWebSearchEngine.java` → `NexusGoogleCustomWebSearchEngine.java`
- `AdiGoogleCustomSearchApiClient.java` → `NexusGoogleCustomSearchApiClient.java`
- `AdiEmbeddingStoreContentRetriever.java` → `NexusEmbeddingStoreContentRetriever.java`
- `AdiEmbeddingSearchRequest.java` → `NexusEmbeddingSearchRequest.java`
- `AdiChatLanguageModelImpl.java` → `NexusChatLanguageModelImpl.java`
- `AdiNeo4jFilterMapper.java` → `NexusNeo4jFilterMapper.java`
- `AdiNeo4jEmbeddingStore.java` → `NexusNeo4jEmbeddingStore.java`
- `AdiWanxImageModel.java` → `NexusWanxImageModel.java`
- `AdiImageSynthesis.java` → `NexusImageSynthesis.java`
- `AdiMailSender.java` → `NexusMailSender.java`
- `AdiFile.java` → `NexusFile.java`

### 2.4 代码内容替换
需要在所有文件中替换字符串内容：
| 原字符串 | 新字符串 | 备注 |
|----------|----------|------|
| `aideepin` | `nexus` | 小写形式 |
| `Aideepin` | `Nexus` | 首字母大写 |
| `AIDEEDPIN` | `NEXUS` | 全大写 |
| `adi` | `nexus` | 小写形式（需谨慎，避免误替换） |
| `Adi` | `Nexus` | 首字母大写 |
| `ADI` | `NEXUS` | 全大写 |

## 3. 执行步骤

### 步骤1：创建目录结构变更
1. 创建新的目录结构
2. 移动文件到新位置

### 步骤2：Java包名变更
1. 重命名 `src/main/java/com/moyz/adi/` → `src/main/java/com/moyz/nexus/`
2. 更新所有Java文件的package声明
3. 更新所有import语句

### 步骤3：重命名Adi前缀文件
1. 批量重命名所有以`Adi`开头的Java文件

### 步骤4：代码内容替换
1. 使用全局搜索替换工具替换所有字符串

### 步骤5：更新配置文件
1. 更新pom.xml中的模块引用
2. 更新application.yml等配置文件

### 步骤6：更新前端项目
1. 更新index.html
2. 更新package.json
3. 更新所有Vue组件中的引用

### 步骤7：验证依赖关系
1. 检查项目是否能正常编译
2. 确保所有依赖路径正确

## 4. 潜在风险与注意事项

### 4.1 风险点
1. **误替换**：`adi`作为常见缩写可能出现在非项目名称的上下文中
2. **大小写敏感性**：需要区分不同大小写形式的替换
3. **文件依赖**：重命名后需要确保所有import语句正确更新
4. **配置文件**：pom.xml、yml配置文件中的路径引用需要同步更新

### 4.2 解决方案
1. 使用精确匹配和上下文分析进行替换
2. 分步骤进行，每步后验证
3. 先进行文件内容替换，再进行文件名替换
4. 最后进行目录结构变更

## 5. 工具与方法

1. **Grep工具**：搜索所有包含目标字符串的文件
2. **批量重命名工具**：使用命令行或脚本进行文件/目录重命名
3. **IDE重构功能**：利用IDE的重命名功能保持依赖关系

## 6. 验证方法

1. **编译验证**：运行`mvn clean compile`验证后端
2. **构建验证**：运行`npm run build`验证前端
3. **启动验证**：启动应用确保能正常运行

---

**计划状态**：待审批

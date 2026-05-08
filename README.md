# NexusAI

基于 LangChain4j 的 AI 深度应用项目

## 项目结构

```
├── langchain4j-nexus/     # 后端服务
│   ├── nexus-admin/            # 管理后台模块
│   ├── nexus-bootstrap/        # 启动模块
│   ├── nexus-chat/             # 聊天核心模块
│   ├── nexus-common/           # 公共模块
│   ├── docker-compose/         # Docker部署配置
│   └── docs/                   # 文档和数据库脚本
└── langchain4j-nexus-web/ # 前端应用
```

## 技术栈

- **后端**: Java 21, Spring Boot, LangChain4j, PostgreSQL, pgvector
- **前端**: Vue 3, TypeScript, Vite, NaiveUI
- **AI**: 支持多种大模型（OpenAI、DeepSeek、Ollama、SiliconFlow等）

## 快速开发

### 后端

```bash
cd langchain4j-nexus/nexus-bootstrap
mvn spring-boot:run
```

### 前端

```bash
cd langchain4j-nexus-web
pnpm install
pnpm run dev
```

## 功能特性

- AI 聊天（支持多模型切换）
- 知识库管理（RAG + Graph 双引擎）
- 图片生成（支持多种图像模型）
- 工作流引擎（可视化流程编排）
- MCP 工具集成
- 搜索引擎集成（Google搜索）
- 用户配额管理
- 管理后台

## 模块说明

### nexus-common
公共模块，包含：
- 实体类、DTO、Mapper
- AI 模型服务封装（LLM、Embedding、TTS、ASR、图像生成）
- RAG 服务（Embedding、Graph、Composite）
- 文件存储服务（阿里云OSS、本地存储）
- 工具类和服务

### nexus-chat
聊天核心模块，提供用户端功能：
- 对话管理
- 知识库问答
- 图片生成
- 工作流执行
- 搜索服务
- MCP 集成

### nexus-admin
管理后台模块：
- 用户管理
- AI 模型配置
- 知识库管理
- 工作流管理
- MCP 工具管理
- 系统配置
- 统计数据

### nexus-bootstrap
启动模块，整合所有配置

## 部署

支持 Docker Compose 一键部署：

```bash
cd langchain4j-nexus/docker-compose
docker-compose up -d
```

## 许可证

MIT License

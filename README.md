<div align="center">

<!-- Header Wave -->
<img src="https://capsule-render.vercel.app/api?type=waving&color=0:FF6B6B,50:4ECDC4,100:45B7D1&height=120&section=header&text=&fontSize=0" width="100%" alt="header"/>

<!-- Logo -->
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="docs/logo.svg">
  <source media="(prefers-color-scheme: light)" srcset="docs/logo.svg">
  <img src="docs/logo.svg" alt="EvolveHub Logo" width="180" height="180">
</picture>

<br/>

<!-- Animated Title -->
<h1>
  <img src="https://readme-typing-svg.demolab.com?font=Orbitron&size=42&duration=3000&pause=1000&color=4ECDC4&center=true&vCenter=true&multiline=true&repeat=true&width=600&height=100&lines=EvolveHub+%F0%9F%A7%AC" alt="EvolveHub"/>
</h1>

<!-- Subtitle -->
<p>
  <img src="https://readme-typing-svg.demolab.com?font=Noto+Sans+SC&size=18&duration=4000&pause=1000&color=FF6B6B&center=true&vCenter=true&multiline=true&repeat=true&width=500&height=40&lines=Enterprise+AI+Intelligent+Platform" alt="subtitle"/>
</p>

**Enterprise AI Conversational Platform · Ready to Use · Zero Code**

<br/>
<br/>

<!-- Badge Wall -->
<p>
  <img src="https://img.shields.io/badge/Platform-Enterprise-4ECDC4?style=for-the-badge&logo=probot&logoColor=white" alt="Enterprise Platform"/>
  <img src="https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21+"/>
  <img src="https://img.shields.io/badge/MCP-Compatible-6DB33F?style=for-the-badge" alt="MCP Compatible"/>
  <img src="https://img.shields.io/badge/License-MIT-FF6B6B?style=for-the-badge" alt="License"/>
</p>

<p>
  <a href="README_zh.md">
    <img src="https://img.shields.io/badge/%F0%9F%87%A8%F0%9F%87%B3-%E4%B8%AD%E6%96%87%E6%96%87%E6%A1%A3-45B7D1?style=flat-square" alt="Chinese"/>
  </a>
</p>

<!-- Footer Wave -->
<img src="https://capsule-render.vercel.app/api?type=waving&color=0:45B7D1,50:4ECDC4,100:FF6B6B&height=80&section=footer" width="100%" alt="footer"/>

</div>

---

## 🎯 What is EvolveHub?

<div align="center">

| 🧬 | **EvolveHub = Enterprise Claude** |
|:--:|:----------------------------------|

</div>

> **EvolveHub** is a ready-to-use enterprise AI platform. No coding required — simply connect your company's **MCP services** or **A2A protocol**, and AI can seamlessly interact with your business systems.

<br/>

<div align="center">

### 🪄 One Sentence Summary

**Configure and use. Connect everything. Let AI understand and operate your enterprise systems.**

</div>

---

## ✨ Core Capabilities

<div align="center">

<table>
<tr>
<td width="50%" valign="top">

### 🔌 Plug & Play
<img src="https://img.shields.io/badge/Zero-Code-9B59B6?style=flat-square" alt="Zero Code"/>

- 📦 **Ready to Use** — No development needed, configure and go
- 🔗 **MCP Protocol** — Compatible with ModelScope MCP ecosystem
- 🤝 **A2A Protocol** — Multi-agent interconnection support
- ⚡ **Skills Extension** — One-click enterprise skill packages

```diff
+ Zero-code integration
+ Minutes-level deployment
+ Enterprise-grade security
```

</td>
<td width="50%" valign="top">

### 🧠 Intelligent Evolution
<img src="https://img.shields.io/badge/AI-Evolving-E74C3C?style=flat-square" alt="AI Evolving"/>

- 🧬 **Memory Evolution** — AI understands your business better over time
- ⚡ **Strategy Iteration** — Auto-optimizes conversation strategies
- 🤝 **Collaborative Emergence** — Multi-agent intelligent collaboration
- 📊 **Knowledge Accumulation** — Continuous enterprise knowledge building

```diff
+ Gets smarter with use
+ Deeper business understanding
+ More precise decisions
```

</td>
</tr>
</table>

</div>

---

## 🏗️ Platform Architecture

<div align="center">

```mermaid
graph TB
    subgraph "👤 User Interaction Layer"
        A[💬 Web Chat]
        B[📱 Mobile]
        C[🔌 API Access]
    end

    subgraph "🧠 EvolveHub Intelligence Platform"
        D[🎯 Intent Recognition]
        E[🧠 Evolution Engine]
        F[🔧 Tool Orchestration]
    end

    subgraph "🔌 Enterprise Connection Layer"
        G[📦 MCP Services]
        H[🤝 A2A Protocol]
        I[⚡ Skills]
    end

    subgraph "🏢 Your Business Systems"
        J[📊 ERP/CRM]
        K[🗄️ Database]
        L[🔗 Internal APIs]
    end

    A --> D
    B --> D
    C --> D
    D --> E
    E --> F
    F --> G
    F --> H
    F --> I
    G --> J
    H --> J
    I --> J
    G --> K
    G --> L

    style E fill:#4ECDC4,color:#fff
    style G fill:#6DB33F,color:#fff
    style H fill:#3498DB,color:#fff
    style I fill:#E74C3C,color:#fff
```

</div>

---

## 🚀 Use Cases

<div align="center">

| Scenario | Description | Benefit |
|:--------:|:------------|:-------:|
| 💬 **Smart Customer Service** | AI understands business, auto-queries orders, handles tickets | 80% efficiency boost |
| 📊 **Data Assistant** | Natural language database queries, report generation | Zero SQL barrier |
| 🔧 **Ops Assistant** | AI executes operations, auto-troubleshoots | 70% faster response |
| 📋 **Workflow Approval** | Intelligent approval understanding, decision support | 3x faster approval |
| 🎓 **Training Tutor** | Q&A based on enterprise knowledge base | 60% lower training cost |

</div>

---

## 🔌 Integration Methods

### Method 1: MCP Protocol

Simply configure your MCP service endpoint, platform auto-discovers and loads tools:

```yaml
# evolverhub-config.yaml
mcp:
  servers:
    - name: "company-erp"
      endpoint: "https://erp.company.com/mcp"
      auth:
        type: "bearer"
        token: "${ERP_API_TOKEN}"
```

### Method 2: A2A Protocol

Register your Agent to A2A network for multi-agent collaboration:

```yaml
a2a:
  registry: "nacos://localhost:8848"
  agents:
    - name: "order-agent"
      capability: "Order Query & Processing"
    - name: "inventory-agent"
      capability: "Inventory Management"
```

### Method 3: Skills Packages

Import pre-built enterprise skill packages for instant business capabilities:

```yaml
skills:
  - name: "database-query"
    version: "1.0.0"
  - name: "report-generator"
    version: "2.1.0"
```

---

## 🆚 Comparison with Traditional Solutions

<div align="center">

| Dimension | Traditional AI Development | **EvolveHub** |
|:---------:|:--------------------------:|:-------------:|
| Development Cost | 🔴 High (needs AI engineers) | 🟢 Zero-code config |
| Deployment Time | 🔴 Weeks/Months | 🟢 Minutes |
| Business Adaptation | 🔴 Custom development | 🟢 MCP/A2A plug-and-play |
| Knowledge Building | 🔴 Static Prompts | 🟢 Auto-evolution accumulation |
| Maintenance Cost | 🔴 Continuous investment | 🟢 Self-adaptive optimization |

</div>

---

## 📦 Deployment Options

<div align="center">

| Deployment Mode | Use Case | Features |
|:---------------:|:--------:|:--------:|
| 🐳 **Docker** | Quick trial, test environments | One-click startup |
| ☸️ **Kubernetes** | Production, high availability | Elastic scaling |
| 🏢 **On-Premise** | Data-sensitive, compliance | Full control |

</div>

### Docker Quick Start

```bash
# Pull image
docker pull evolvehub/server:latest

# Start service
docker run -d \
  --name evolvehub \
  -p 8080:8080 \
  -v ./config:/app/config \
  evolvehub/server:latest

# Visit http://localhost:8080 to start using
```

---

## 📈 Roadmap

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#4ECDC4'}}}%%
timeline
    title EvolveHub Roadmap
    section Phase 1
        Core Platform Release
        : MCP Protocol Support
        : Basic Conversation
    section Phase 2
        Enterprise Enhancement
        : A2A Multi-Agent
        : Skills Marketplace
    section Phase 3
        Intelligence Upgrade
        : Memory Evolution
        : Strategy Iteration
        : Knowledge Graph
```

---

## 🤝 Join the Community

<div align="center">

### 📱 Scan to Join DingTalk Group

<img src="docs/imgs/dingtalk_qr_code.png" alt="DingTalk QR Code" width="200">

*Product Inquiry · Technical Discussion · Feedback*

<br/>

</div>

---

## 📄 License

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

</div>

---

<div align="center">

**Made with ❤️ by the EvolveHub Team**

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:45B7D1,50:4ECDC4,100:FF6B6B&height=100&section=footer" width="100%" alt="footer"/>

</div>
-- ============================================================
-- EvolveHub AI 模块 - 知识库建表脚本
-- 数据库：evolve_ai (PostgreSQL)
-- ============================================================

-- -----------------------------------------------------------
-- 1. 知识库表
-- -----------------------------------------------------------
CREATE TABLE eh_knowledge_base (
    id          BIGINT       NOT NULL,
    name        VARCHAR(128) NOT NULL,
    level       SMALLINT     NOT NULL DEFAULT 1,
    dept_id     BIGINT,
    owner_id    BIGINT,
    description VARCHAR(512),
    create_by   BIGINT,
    create_time TIMESTAMP    NOT NULL,
    update_time TIMESTAMP    NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_kb_dept_id  ON eh_knowledge_base (dept_id);
CREATE INDEX idx_kb_owner_id ON eh_knowledge_base (owner_id);
COMMENT ON TABLE  eh_knowledge_base              IS '知识库表';
COMMENT ON COLUMN eh_knowledge_base.id          IS '主键 ID（雪花算法）';
COMMENT ON COLUMN eh_knowledge_base.name        IS '知识库名称';
COMMENT ON COLUMN eh_knowledge_base.level       IS '权限级别（1-GLOBAL 2-DEPT 3-PROJECT 4-SENSITIVE）';
COMMENT ON COLUMN eh_knowledge_base.dept_id     IS '所属部门 ID（level=2 时有效）';
COMMENT ON COLUMN eh_knowledge_base.owner_id    IS '所有者用户 ID';
COMMENT ON COLUMN eh_knowledge_base.description IS '知识库描述';
COMMENT ON COLUMN eh_knowledge_base.create_by   IS '创建人 ID';
COMMENT ON COLUMN eh_knowledge_base.create_time IS '创建时间';
COMMENT ON COLUMN eh_knowledge_base.update_time IS '更新时间';
COMMENT ON COLUMN eh_knowledge_base.deleted     IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 2. 知识库文档表
-- -----------------------------------------------------------
CREATE TABLE eh_kb_document (
    id            BIGINT        NOT NULL,
    kb_id         BIGINT        NOT NULL,
    file_name     VARCHAR(256)  NOT NULL,
    file_path     VARCHAR(1024) NOT NULL,
    file_size     BIGINT        NOT NULL DEFAULT 0,
    chunk_count   INT           NOT NULL DEFAULT 0,
    status        SMALLINT      NOT NULL DEFAULT 0,
    error_message TEXT,
    create_by     BIGINT,
    create_time   TIMESTAMP     NOT NULL,
    update_time   TIMESTAMP     NOT NULL,
    deleted       SMALLINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_kb_doc_kb_id ON eh_kb_document (kb_id);
COMMENT ON TABLE  eh_kb_document                 IS '知识库文档表';
COMMENT ON COLUMN eh_kb_document.id            IS '主键 ID（雪花算法）';
COMMENT ON COLUMN eh_kb_document.kb_id         IS '所属知识库 ID';
COMMENT ON COLUMN eh_kb_document.file_name     IS '文件名称';
COMMENT ON COLUMN eh_kb_document.file_path     IS '文件存储路径';
COMMENT ON COLUMN eh_kb_document.file_size     IS '文件大小（字节）';
COMMENT ON COLUMN eh_kb_document.chunk_count   IS '切片总数';
COMMENT ON COLUMN eh_kb_document.status        IS '处理状态（0-待处理 1-处理中 2-已完成 3-失败）';
COMMENT ON COLUMN eh_kb_document.error_message IS '失败时的错误信息';
COMMENT ON COLUMN eh_kb_document.create_by     IS '创建人 ID';
COMMENT ON COLUMN eh_kb_document.create_time   IS '创建时间';
COMMENT ON COLUMN eh_kb_document.update_time   IS '更新时间';
COMMENT ON COLUMN eh_kb_document.deleted       IS '逻辑删除（0-未删除 1-已删除）';

-- -----------------------------------------------------------
-- 3. 知识库切片表
-- -----------------------------------------------------------
CREATE TABLE eh_kb_chunk (
    chunk_id     BIGINT        NOT NULL,
    doc_id       BIGINT        NOT NULL,
    kb_id        BIGINT        NOT NULL,
    chunk_index  INT           NOT NULL DEFAULT 0,
    content      TEXT          NOT NULL,
    token_count  INT           NOT NULL DEFAULT 0,
    page_num     INT,
    heading_path VARCHAR(512),
    chunk_type   VARCHAR(32)   NOT NULL DEFAULT 'text',
    status       SMALLINT      NOT NULL DEFAULT 0,
    milvus_id    VARCHAR(64),
    error_message TEXT,
    create_time  TIMESTAMP     NOT NULL,
    PRIMARY KEY (chunk_id)
);
CREATE INDEX idx_kb_chunk_doc_id ON eh_kb_chunk (doc_id);
CREATE INDEX idx_kb_chunk_kb_id  ON eh_kb_chunk (kb_id);
COMMENT ON TABLE  eh_kb_chunk                   IS '知识库切片表';
COMMENT ON COLUMN eh_kb_chunk.chunk_id      IS '切片主键 ID（雪花算法）';
COMMENT ON COLUMN eh_kb_chunk.doc_id        IS '所属文档 ID';
COMMENT ON COLUMN eh_kb_chunk.kb_id         IS '所属知识库 ID（冗余，便于按库检索）';
COMMENT ON COLUMN eh_kb_chunk.chunk_index   IS '切片在文档内的顺序索引（从 0 开始）';
COMMENT ON COLUMN eh_kb_chunk.content       IS '切片文本内容';
COMMENT ON COLUMN eh_kb_chunk.token_count   IS 'Token 数量';
COMMENT ON COLUMN eh_kb_chunk.page_num      IS '原始页码（PDF 等有页概念的文档）';
COMMENT ON COLUMN eh_kb_chunk.heading_path  IS '标题层级路径（如 "第一章 > 1.1 概述"）';
COMMENT ON COLUMN eh_kb_chunk.chunk_type    IS '切片类型（text / table / image_desc）';
COMMENT ON COLUMN eh_kb_chunk.status        IS '向量化状态（0-待向量化 1-向量化中 2-已入库 3-失败）';
COMMENT ON COLUMN eh_kb_chunk.milvus_id     IS 'Milvus 向量库中的记录 ID';
COMMENT ON COLUMN eh_kb_chunk.error_message IS '失败时的错误信息';
COMMENT ON COLUMN eh_kb_chunk.create_time   IS '创建时间';
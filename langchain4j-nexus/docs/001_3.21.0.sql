create table nexus_conversation_message_ref_memory_embedding
(
    id           bigserial primary key,
    message_id   bigint        default 0  not null,
    embedding_id varchar(36)   default '' not null,
    score        numeric(3, 2) default 0  not null,
    user_id      bigint        default 0  not null
);
comment on table nexus_conversation_message_ref_memory_embedding is '会话消息-知识库的记忆引用 | Conversation-Question Record-Memory References';
comment on column nexus_conversation_message_ref_memory_embedding.message_id is '消息id | nexus_conversation_message ID';
comment on column nexus_conversation_message_ref_memory_embedding.embedding_id is '根据消息从记忆向量库中获取到的向量uuid | nexus_conversation_memory_embedding UUID';
comment on column nexus_conversation_message_ref_memory_embedding.score is '评分 | Score';
comment on column nexus_conversation_message_ref_memory_embedding.user_id is '所属用�?| User ID';

alter table nexus_conversation_message
    add column is_ref_memory_embedding boolean default false not null;
comment on column nexus_conversation_message.is_ref_memory_embedding is '是否引用了记忆向量库 | Whether to reference memory vector library';
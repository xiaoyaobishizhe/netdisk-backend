create table tb_sharding
(
    id            bigint        not null comment '主键'
        primary key,
    user_id       bigint        not null comment '用户主键值',
    parent_id     bigint        null comment '父文件夹主键值',
    identifier    varchar(250)  not null comment '文件本身的唯一标识',
    filename      varchar(250)  not null comment '文件名称',
    size          bigint        not null comment '文件大小，单位为字节',
    chunk_size    int           not null comment '块的最大大小，单位为字节',
    current_chunk int default 0 not null comment '当前已上传的块数',
    total_chunk   int           not null comment '总的块数'
);

create table tb_share
(
    id          bigint       not null comment '主键'
        primary key,
    user_id     bigint       not null comment '用户id',
    name        varchar(50)  not null comment '分享名称',
    code        varchar(200) not null comment '分享码',
    password    varchar(10)  null comment '提取码',
    token       varchar(250) not null comment '访问令牌',
    file_list   json         not null comment '文件列表',
    timeout     int          not null comment '过期天数，0为永久有效',
    create_time datetime     not null comment '创建时间'
);

create table tb_storage_file
(
    id         bigint unsigned not null comment '主键'
        primary key,
    identifier varchar(250)    not null comment '文件唯一标识',
    path       varchar(250)    not null comment '在存储库中的路径',
    size       bigint unsigned not null
);

create table tb_user_file
(
    id              bigint unsigned      not null comment '主键'
        primary key,
    user_id         bigint unsigned      not null comment '用户主键值',
    path            varchar(500)         not null comment '所处路径',
    parent_id       bigint unsigned      null comment '父文件夹主键值',
    name            varchar(250)         not null comment '文件名',
    is_folder       tinyint(1)           not null comment '是否是文件夹',
    size            bigint               null comment '文件大小',
    identifier      varchar(250)         null comment '文件的唯一标识',
    storage_file_id bigint unsigned      null comment '存储文件主键值',
    is_deleted      tinyint(1) default 0 not null comment '是否放到回收站',
    delete_time     datetime             null comment '删除时间',
    create_time     datetime             not null comment '创建时间',
    update_time     datetime             not null comment '更新时间'
);

create index idx_user_id_is_deleted_parent_id
    on tb_user_file (user_id, is_deleted, parent_id);

create index idx_user_id_is_deleted_path
    on tb_user_file (user_id, is_deleted, path);


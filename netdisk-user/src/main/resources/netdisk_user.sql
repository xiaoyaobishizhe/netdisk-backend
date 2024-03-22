create table tb_user
(
    id          bigint unsigned not null comment '主键'
        primary key,
    nickname    varchar(50)     not null comment '昵称',
    username    varchar(30)     not null comment '用户名',
    password    varchar(300)    not null comment '密码',
    create_time datetime        not null comment '创建时间'
);
INSERT INTO netdisk_user.tb_user (id, nickname, username, password, create_time) VALUES (1742376599104978945, '张三', '123456', '03360cf48e7762c030c672102ff2414a579c9fe856001c85cc97664d82dc2f73', '2024-01-03 10:45:28');
INSERT INTO netdisk_user.tb_user (id, nickname, username, password, create_time) VALUES (1764135985894989825, '李四', 'aaaaaa', 'd47883fe9dbe0e077eee6dc633c21ede2b088f882cb8dc5f1f259f76b807a1df', '2024-03-03 11:49:30');

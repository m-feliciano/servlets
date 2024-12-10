create table tb_category
(
    id      int8,
    name    varchar(255),
    status  varchar(1),
    user_id int8
);

create table tb_inventory
(
    id          int8,
    description varchar(255),
    quantity    int4,
    status      varchar(1),
    product_id  int8,
    user_id     int8
);


create table tb_product
(
    id            int8,
    description   TEXT,
    name          varchar(100),
    price         numeric(19, 2),
    register_date date,
    status        varchar(1),
    url_img       TEXT,
    category_id   int8,
    user_id       int8
);


create table tb_user
(
    id        int8,
    image_url TEXT,
    login     varchar(255) unique,
    password  varchar(255),
    status    varchar(1),
    config    TEXT
);

create table tb_perfil
(
    id   int8,
    name varchar(50) not null
);

create table user_perfis
(
    user_id   int8 not null,
    perfil_id int8 not null
);

-- constraints primary keys
alter table tb_user
    add constraint pk_tb_user_id primary key (id);

alter table tb_category
    add constraint pk_tb_category_id primary key (id);

alter table tb_inventory
    add constraint pk_tb_inventory_id primary key (id);

alter table tb_product
    add constraint pk_tb_product_id primary key (id);

alter table tb_perfil
    add constraint pk_tb_perfil_id primary key (id);

-- sequence
create sequence tb_category_id_seq;
create sequence tb_inventory_id_seq;
create sequence tb_product_id_seq;
create sequence tb_user_id_seq;
create sequence tb_perfil_id_seq;

-- auto increment
alter table tb_category
    alter column id set default nextval('tb_category_id_seq');

alter table tb_inventory
    alter column id set default nextval('tb_inventory_id_seq');

alter table tb_product
    alter column id set default nextval('tb_product_id_seq');

alter table tb_user
    alter column id set default nextval('tb_user_id_seq');

alter table tb_perfil
    alter column id set default nextval('tb_perfil_id_seq');

-- constraints foreign keys
alter table tb_inventory
    add constraint fk_tb_inventory_tb_product
        foreign key (product_id)
            references tb_product;

alter table tb_inventory
    add constraint fk_tb_inventory_tb_user
        foreign key (user_id)
            references tb_user;

alter table tb_product
    add constraint fk_tb_product_tb_category
        foreign key (category_id)
            references tb_category;

alter table tb_product
    add constraint fk_tb_product_tb_user
        foreign key (user_id)
            references tb_user;

alter table tb_category
    add constraint fk_tb_category_tb_user
        foreign key (user_id)
            references tb_user;

alter table user_perfis
    add constraint fk_tb_user_perfis_tb_perfil
        foreign key (perfil_id)
            references tb_perfil;

alter table user_perfis
    add constraint fk_tb_user_perfis_tb_user
        foreign key (user_id)
            references tb_user;

-- Initial responseData
insert into tb_perfil (id, name)
values (1, 'ADMIN');
insert into tb_perfil (id, name)
values (2, 'USER');
insert into tb_perfil (id, name)
values (3, 'MANAGER');
insert into tb_perfil (id, name)
values (4, 'GUEST');
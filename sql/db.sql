-- BRANDS

create type brand_quality as enum('luxury', 'mid', 'drug_store');

create table brand (
    id      bigserial
            primary key,
    name    varchar(32)
            not null,
    quality brand_quality
);

-- PRODUCTS

create table if not exists product (
    id           bigserial
                 primary key,
    brand        bigint
                 unique
                 not null,
    product_name varchar(32)
                 not null,
    cost         int
                 not null,

    foreign key (brand)
        references brand(id)
        on delete restrict
);

-- blush products

create type blush_product_formula as enum('liquid', 'cream', 'powder', 'gel');

create table if not exists blush_product (
    id      bigint
            primary key
            references product(id)
            on update cascade
            on delete cascade,
    formula blush_product_formula
            not null,
    color   varchar(32)
            not null
);

-- lip products

create type lip_product_type as enum('lipstick', 'gloss', 'oil', 'balm', 'other');
create type sheen_type as enum('matte', 'semi-gloss', 'high-gloss', 'glitter');
create type consistency_type as enum('smooth', 'sticky', 'creamy');

create table if not exists lip_product (
    id          bigint
                primary key
                references product(id)
                on update cascade
                on delete cascade,
    color       varchar(32),
    shade_name  varchar(32),
    type        lip_product_type
                not null,
    sheen       sheen_type
                not null,
    consistency consistency_type,
    hydrating   bool
                not null
                default false
);

create table if not exists scented_lip_product (
    id    bigint
          primary key
          references lip_product(id)
          on update cascade
          on delete cascade,
    scent varchar(32)
          not null
);

-- eye products

create table if not exists eye_product (
    id    bigint
          primary key
          references product(id)
          on update cascade
          on delete cascade,
    color varchar(32)
          not null
);

create table if not exists eye_shadow_product (
    id         bigint
               primary key
               references eye_product(id)
               on update cascade
               on delete cascade,
    glitter    bool
               not null,
    shade_name varchar(32)
);

-- brow products

create type brow_product_formula as enum('pencil', 'gel');

create table if not exists brow_product (
    id      bigint
            primary key
            references product(id)
            on update cascade
            on delete cascade,
    color   varchar(32)
            not null
            default 'brown',
    formula brow_product_formula
            not null
);

-- fragrance products

create table if not exists fragrance_product (
    id    bigint
          primary key
          references product(id)
          on update cascade
          on delete cascade,
    scent varchar(32)
);

-- megan notes on products

create table if not exists favorite_product (
    id  bigint
        primary key
        references product(id)
        on delete cascade
);

create table if not exists product_rating (
    product_id      bigint
                    primary key
                    references product(id)
                    on delete cascade,
    rating          int
                    not null,
    would_buy_again bool
                    not null
                    default false,
    comments        text
);


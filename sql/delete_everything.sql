-- Disable all foreign key constraints
do $$
    declare r record;
    begin
        for r in
            select conname, conrelid::regclass
            from pg_constraint
            where contype = 'f'
        loop execute
            'alter table ' ||
            r.conrelid ||
            ' drop constraint ' ||
            r.conname ||
            ';';
        end loop;
    end
$$;

-- drop all tables
drop table if exists blush_product;
drop table if exists brand;
drop table if exists brow_product;
drop table if exists eye_product;
drop table if exists eye_shadow_product;
drop table if exists favorite_product;
drop table if exists fragrance_product;
drop table if exists lip_product;
drop table if exists product;
drop table if exists product_rating;
drop table if exists scented_lip_product;
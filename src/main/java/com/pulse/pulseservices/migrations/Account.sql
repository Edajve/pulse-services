-- Drop Account table if exists
DROP TABLE IF EXISTS account;

-- Create Account table
create table account
(
    id                   int                                                                           not null
        primary key,
    account_created_date datetime(6)                                                                   null,
    country_region       tinyint                                                                       null,
    date_of_birth        varchar(255)                                                                  null,
    email                varchar(255)                                                                  null,
    first_name           varchar(255)                                                                  null,
    last_name            varchar(255)                                                                  null,
    password             varchar(255)                                                                  null,
    pin_code             varchar(255)                                                                  null,
    role                 enum ('ADMIN', 'USER')                                                        null,
    security_answer      varchar(255)                                                                  null,
    security_question    varchar(255)                                                                  null,
    sex                  enum ('female', 'gender_queer', 'male', 'non_binary', 'other', 'transgender') null,
    local_hash           varchar(88)                                                                   null,
    auth_method          varchar(255)                                                                  null,
    check (`country_region` between 0 and 0)
);


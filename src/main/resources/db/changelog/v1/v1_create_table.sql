create table if not exists person
(
    id       int auto_increment
        primary key,
    email    varchar(100) not null,
    password varchar(100) not null
);

create table if not exists task
(
    id          int auto_increment
        primary key,
    description varchar(1000) not null,
    status      varchar(100)  not null,
    priority    varchar(100)  not null,
    author_id   int           not null,
    executor_id int           null,
    constraint author_id
        foreign key (author_id) references person (id)
            on update cascade on delete cascade,
    constraint executor_id
        foreign key (executor_id) references person (id)
);

create table if not exists comment
(
    id      int auto_increment
        primary key,
    text    varchar(5000) not null,
    task_id int           not null,
    constraint task_id
        foreign key (task_id) references task (id)
            on update cascade on delete cascade
);
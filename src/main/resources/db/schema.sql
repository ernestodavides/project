create table if not exists projects (
    id bigint generated always as identity primary key,
    title varchar(120) not null,
    mission varchar(15) not null
);

create table if not exists sprints (
    id bigint generated always as identity primary key,
    sprint_number int not null unique check (sprint_number > 0),
    start_date date not null,
    end_date date not null,
    constraint chk_sprints_dates check (end_date >= start_date)
);

create table if not exists deliverables (
    id bigint generated always as identity primary key,
    project_id bigint not null,
    name varchar(150) not null,
    goal varchar(2000),
    constraint fk_deliverables_project foreign key (project_id)
        references projects (id) on delete cascade
);

create table if not exists deliverable_tasks (
    id bigint generated always as identity primary key,
    deliverable_id bigint not null,
    sprint_id bigint not null,
    title varchar(180) not null,
    progress decimal(5, 2) not null default 0.00
        check (progress >= 0.00 and progress <= 100.00),
    constraint fk_deliverable_tasks_deliverable foreign key (deliverable_id)
        references deliverables (id) on delete cascade,
    constraint fk_deliverable_tasks_sprint foreign key (sprint_id)
        references sprints (id) on delete restrict
);

-- public.license definition

-- Drop table

-- DROP TABLE public.license;

CREATE TABLE public.license (
                                id uuid NOT NULL DEFAULT gen_random_uuid(),
                                date_created timestamp NOT NULL,
                                date_modified timestamp NULL,
                                days_licensed int4 NULL,
                                days_running int4 NULL,
                                degrade_date timestamp NULL,
                                degrade_items jsonb NULL,
                                degrade_period int4 NULL,
                                device_info jsonb NULL,
                                end_date timestamp NULL,
                                license varchar(255) NULL ,
                                publish_response uuid NULL,
                                start_date timestamp NULL,
                                status_response jsonb NOT NULL,
                                s varchar(255) NULL ,
                                CONSTRAINT "primary" PRIMARY KEY (id ASC)
);


-- public.transaction_config definition

-- Drop table

-- DROP TABLE public.transaction_config;

CREATE TABLE public.transaction_config (
                                           id uuid NOT NULL DEFAULT gen_random_uuid(),
                                           airtime_limit numeric(19,2) NULL,
                                           approval_count int4 NULL,
                                           date_created timestamp NOT NULL,
                                           date_modified timestamp NULL,
                                           payload jsonb NULL,
                                           record jsonb NULL,
                                           reversal_approval_count int4 NULL,
                                           transaction_approval_count int4 NULL,
                                           daily_transfer_limit numeric(19,2) NULL,
                                           checker_records jsonb NULL,
                                           evalue_approval_count int4 NULL,
                                           maker jsonb NULL,
                                           mc_status varchar(127) NULL ,
                                           checker jsonb NULL,
                                           transfer_limit numeric(19,2) NULL,
                                           CONSTRAINT "primary" PRIMARY KEY (id ASC)
);


-- public.transactions_reports definition

-- Drop table

-- DROP TABLE public.transactions_reports;

CREATE TABLE public.transactions_reports (
                                             id uuid NOT NULL DEFAULT gen_random_uuid(),
                                             date_created timestamp NOT NULL,
                                             date_modified timestamp NULL,
                                             s varchar(255) NULL ,
                                             mt940_payload_string text NULL ,
                                             closing_balance numeric(19,2) NOT NULL,
                                             date_from timestamp NOT NULL,
                                             date_to timestamp NOT NULL,
                                             email varchar(255) NULL ,
                                             opening_balance numeric(19,2) NOT NULL,
                                             report_number int4 NULL,
                                             sent bool NULL,
                                             transaction_count bytea NULL,
                                             user_id uuid NULL,
                                             CONSTRAINT "primary" PRIMARY KEY (id ASC)
);


-- public.destinations definition

-- Drop table

-- DROP TABLE public.destinations;

CREATE TABLE public.destinations (
                                     id uuid NOT NULL DEFAULT gen_random_uuid(),
                                     available_balance numeric(19,2) NULL,
                                     code varchar(255) NULL ,
                                     date_created timestamp NOT NULL,
                                     date_modified timestamp NULL,
                                     description varchar(255) NULL ,
                                     notified bool NULL,
                                     payload jsonb NULL,
                                     reason varchar(255) NULL ,
                                     request jsonb NULL,
                                     response jsonb NULL,
                                     response_async jsonb NULL,
                                     revenue bool NULL,
                                     store_ref varchar(255) NULL ,
                                     store_status_check_count int4 NULL,
                                     "status" varchar(255) NOT NULL ,
                                     "transaction" uuid NULL,
                                     fx_id uuid NULL,
                                     CONSTRAINT "primary" PRIMARY KEY (id ASC)
);


-- public.sources definition

-- Drop table

-- DROP TABLE public.sources;

CREATE TABLE public.sources (
                                id uuid NOT NULL DEFAULT gen_random_uuid(),
                                available_balance numeric(19,2) NULL,
                                code varchar(255) NULL ,
                                date_created timestamp NOT NULL,
                                date_modified timestamp NULL,
                                description varchar(255) NULL ,
                                notified bool NULL,
                                payload jsonb NULL,
                                reason varchar(255) NULL ,
                                request jsonb NULL,
                                response jsonb NULL,
                                response_async jsonb NULL,
                                revenue bool NULL,
                                store_ref varchar(255) NULL ,
                                store_status_check_count int4 NULL,
                                "status" varchar(255) NOT NULL ,
                                "transaction" uuid NULL,
                                fx_id uuid NULL,
                                CONSTRAINT "primary" PRIMARY KEY (id ASC)
);


-- public.transaction_init definition

-- Drop table

-- DROP TABLE public.transaction_init;

CREATE TABLE public.transaction_init (
                                         id uuid NOT NULL DEFAULT gen_random_uuid(),
                                         approval_count int4 NULL,
                                         date_created timestamp NOT NULL,
                                         date_modified timestamp NULL,
                                         payload jsonb NULL,
                                         record jsonb NULL,
                                         child_transaction uuid NULL,
                                         child_transaction_id uuid NULL,
                                         fraud_info jsonb NULL,
                                         checker_records jsonb NULL,
                                         user_info jsonb NULL,
                                         maker jsonb NULL,
                                         mc_status varchar(128) NULL ,
                                         checker jsonb NULL,
                                         CONSTRAINT "primary" PRIMARY KEY (id ASC),
                                         CONSTRAINT uk_7x26dq1l519g62dsrkt7von1f UNIQUE (child_transaction_id ASC),
                                         CONSTRAINT uk_nbkbcxs409cqa0kxgs3k733ug UNIQUE (child_transaction ASC)
);


-- public.transactions definition

-- Drop table

-- DROP TABLE public.transactions;

CREATE TABLE public.transactions (
                                     id uuid NOT NULL DEFAULT gen_random_uuid(),
                                     code varchar(255) NULL ,
                                     date_created timestamp NOT NULL,
                                     date_modified timestamp NULL,
                                     destination_complete bool NULL,
                                     destination_started bool NULL,
                                     fraud_info jsonb NULL,
                                     notified_fraud bool NULL,
                                     parent_id uuid NULL,
                                     payload jsonb NULL,
                                     publish_account_response jsonb NULL,
                                     published_account_info bool NULL,
                                     reason varchar(255) NULL ,
                                     revenue_processed bool NULL,
                                     source_complete bool NULL,
                                     transaction_id varchar(255) NULL ,
                                     "status" varchar(255) NOT NULL ,
                                     "type" varchar(255) NULL ,
                                     user_info jsonb NULL,
                                     approval_count int4 NULL,
                                     checker jsonb NULL,
                                     maker uuid NULL,
                                     mc_status jsonb NULL,
                                     record jsonb NULL,
                                     commission_parent uuid NULL,
                                     reversal_parent uuid NULL,
                                     commission bool NULL,
                                     reversal_reason varchar(255) NULL ,
                                     reverse_charge bool NULL,
                                     amount numeric(19,2) NULL,
                                     parent_transaction bytea NULL,
                                     reverse_amount numeric(19,2) NULL,
                                     reversal_transaction uuid NULL,
                                     reversal uuid NULL,
                                     airtime_limit numeric(19,2) NULL,
                                     reversal_approval_count int4 NULL,
                                     transaction_approval_count int4 NULL,
                                     transfer_limit numeric(19,2) NULL,
                                     child_transaction uuid NULL,
                                     commission_child uuid NULL,
                                     commission_pre_processed_full bool NOT NULL,
                                     "exception" jsonb NULL,
                                     reversal_child_id uuid NULL,
                                     CONSTRAINT "primary" PRIMARY KEY (id ASC),
                                     CONSTRAINT uk_44gf3lkvyxp1xyrv76mxi6se2 UNIQUE (commission_child ASC),
                                     CONSTRAINT uk_gqf2ni4kn594nvoe7h53i35ph UNIQUE (commission_parent ASC),
                                     CONSTRAINT uk_lq7202ghs4oov332559xtervs UNIQUE (reversal ASC)
);


-- public.reversal definition

-- Drop table

-- DROP TABLE public.reversal;

CREATE TABLE public.reversal (
                                 id uuid NOT NULL,
                                 amount numeric(19,2) NULL,
                                 approval_count int4 NULL,
                                 checker jsonb NULL,
                                 date_created timestamp NOT NULL,
                                 date_modified timestamp NULL,
                                 payload jsonb NULL,
                                 reason varchar(255) NULL ,
                                 reverse_charge bool NULL,
                                 reversal_transaction uuid NULL,
                                 reversal_transaction_id uuid NULL,
                                 checker_records jsonb NULL,
                                 mc_status varchar(127) NULL ,
                                 maker jsonb NULL,
                                 parent_transaction_id uuid NULL,
                                 CONSTRAINT "primary" PRIMARY KEY (id ASC),
                                 CONSTRAINT uk_pjto69ldfllhxkmcs3cvf5sdy UNIQUE (reversal_transaction ASC),
                                 CONSTRAINT uk_pt0b61euvxcb8jqwigd8se61p UNIQUE (reversal_transaction_id ASC)
);


-- public.reversal foreign keys

ALTER TABLE public.reversal ADD CONSTRAINT fkk7tsxll03604shnjku9a2b5pe FOREIGN KEY (reversal_transaction) REFERENCES transactions(id);

-- public.destinations foreign keys

ALTER TABLE public.destinations ADD CONSTRAINT fka33skkax7l7a2yf1pvd2fgolj FOREIGN KEY (transaction) REFERENCES transactions(id);


-- public.sources foreign keys

ALTER TABLE public.sources ADD CONSTRAINT fk42ru7bo98gob94yr0f9ywrgig FOREIGN KEY (transaction) REFERENCES transactions(id);


-- public.transaction_init foreign keys

ALTER TABLE public.transaction_init ADD CONSTRAINT fk1qyw1w603p39w9rffr8518446 FOREIGN KEY (child_transaction_id) REFERENCES transactions(id);
ALTER TABLE public.transaction_init ADD CONSTRAINT fkgi924xipehtmgue7ahe0jumag FOREIGN KEY (child_transaction) REFERENCES transactions(id);


-- public.transactions foreign keys

ALTER TABLE public.transactions ADD CONSTRAINT fk6wq2s60cv4pdpmr5bsab01l5i FOREIGN KEY (reversal_parent) REFERENCES transactions(id);
ALTER TABLE public.transactions ADD CONSTRAINT fk7jav76qphc6ltvai896x9h6uv FOREIGN KEY (commission_parent) REFERENCES transactions(id);
ALTER TABLE public.transactions ADD CONSTRAINT fkibub6c1rddap140t8vl7916du FOREIGN KEY (reversal) REFERENCES reversal(id);



-- public.transactions_destinations definition

-- Drop table

-- DROP TABLE public.transactions_destinations;

CREATE TABLE public.transactions_destinations (
                                                  transaction_id uuid NOT NULL DEFAULT gen_random_uuid(),
                                                  destinations_id uuid NOT NULL DEFAULT gen_random_uuid(),
                                                  rowid int8 NOT NULL DEFAULT unique_rowid(),
                                                  CONSTRAINT uk_jvh2e4ko973y688p4fthbtxm3 UNIQUE (destinations_id ASC)
);


-- public.transactions_sources definition

-- Drop table

-- DROP TABLE public.transactions_sources;

CREATE TABLE public.transactions_sources (
                                             transaction_id uuid NOT NULL DEFAULT gen_random_uuid(),
                                             sources_id uuid NOT NULL DEFAULT gen_random_uuid(),
                                             rowid int8 NOT NULL DEFAULT unique_rowid(),
                                             CONSTRAINT uk_qmiy7rklw85oxrf2cdr55pb6m UNIQUE (sources_id ASC)
);


-- public.transactions_destinations foreign keys

ALTER TABLE public.transactions_destinations ADD CONSTRAINT fki6srpvvb51x5d1wrqq711xyx6 FOREIGN KEY (destinations_id) REFERENCES destinations(id);
ALTER TABLE public.transactions_destinations ADD CONSTRAINT fknyjm44fgery4bfn9c1nctyhe3 FOREIGN KEY (transaction_id) REFERENCES transactions(id);


-- public.transactions_sources foreign keys

ALTER TABLE public.transactions_sources ADD CONSTRAINT fk4b5vc1f1fyxyvx6deydiyr54h FOREIGN KEY (transaction_id) REFERENCES transactions(id);
ALTER TABLE public.transactions_sources ADD CONSTRAINT fkeeu6pa72aujmrau1ujda8jpfn FOREIGN KEY (sources_id) REFERENCES sources(id);


create sequence if not exists report_number_gen increment 1;

--drop view source_destination_merge;
CREATE VIEW source_destination_merge(
                                     id,
                                     user_id,
                                     type,
                                     account_id,
                                     partner_id,
                                     total,
                                     available_balance,
                                     currency,
                                     status,
                                     code,
                                     reason,
                                     transaction,
                                     revenue,
                                     store_ref,
                                     date_created,
                                     date_modified,
                                     debit_credit,
                                     source_account_user_id,
                                     source_account_name,
                                     source_account_email
    ) AS
select
    id,
    user_id,
    type,
    account_id,
    partner_id,
    total,
    available_balance,
    currency,
    status,
    code,
    reason,
    transaction,
    revenue,
    store_ref,
    date_created,
    date_modified,
    debit_credit,
    source_account_user_id,
    source_account_name,
    source_account_email
from
    (
        select
            s.id::text ,
            s.payload -> 'account' ->> 'user_id'  as user_id,
            s.payload -> 'account' ->> 'type' as type,
            s.payload -> 'account' ->> 'id'  as account_id,
            s.payload -> 'account' ->> 'partner_id'  as partner_id,
            s.payload -> 'total' ->> 'amount' as total,
            s.available_balance,
            s.payload -> 'total' ->> 'currency' as currency,
            s.status,
            s.code,
            s.reason,
            s.transaction,
            s.revenue,
            s.store_ref,
            s.date_created,
            s.date_modified,
            'CREDIT' as debit_credit,
            t.user_info ->> 'user_id' as source_account_user_id,
            t.user_info ->> 'name' as source_account_name,
            t.user_info ->> 'email' as source_account_email
        from
            sources s
                left join transactions t on t.id = transaction
        union
        (
            select
                d.id::text ,
                d.payload -> 'account' ->> 'user_id'  as user_id,

                d.payload -> 'account' ->> 'partner_id' as partner_id ,
                d.payload -> 'account' ->> 'type' as type,

                d.payload -> 'account' ->> 'id' as account_id ,
                d.payload -> 'total' ->> 'amount' as total,
                d.available_balance,
                d.payload -> 'total' ->> 'currency' as currency,
                d.status,
                d.code,
                d.reason,
                d.transaction,
                d.revenue,
                d.store_ref,
                d.date_created,
                d.date_modified,
                'DEBIT' as debit_credit,

                t.user_info ->> 'user_id' as source_account_user_id
                    ,
                t.user_info ->> 'name' as source_account_name,
                t.user_info ->> 'email' as source_account_email
            from
                destinations d
                    left join transactions t on t.id = transaction
        )
    )
order by
    date_created desc;


















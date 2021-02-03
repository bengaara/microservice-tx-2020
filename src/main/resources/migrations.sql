ALTER TABLE sources ADD COLUMN account_id varchar(255) AS (payload->'account'->>'id') STORED;
ALTER TABLE sources ADD COLUMN account_user_id varchar(255) AS (payload->'account'->>'user_id') STORED;
ALTER TABLE sources ADD COLUMN account_phone varchar(255) AS (payload->'account'->>'phone') STORED;

ALTER TABLE destinations ADD COLUMN account_id varchar(255) AS (payload->'account'->>'id') STORED;
ALTER TABLE destinations ADD COLUMN account_user_id varchar(255) AS (payload->'account'->>'user_id') STORED;
ALTER TABLE destinations ADD COLUMN account_phone varchar(255) AS (payload->'account'->>'phone') STORED;

CREATE INDEX idx_sources_account_id ON sources (account_id);
CREATE INDEX idx_sources_account_user_id ON sources (account_user_id);
CREATE INDEX idx_sources_account_phone ON sources (account_phone);
CREATE INDEX idx_destinations_account_id ON destinations (account_id);
CREATE INDEX idx_destinations_account_user_id ON destinations (account_user_id);
CREATE INDEX idx_destinations_account_phone ON destinations (account_phone);

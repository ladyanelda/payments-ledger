ALTER TABLE transactions ADD COLUMN source_reference TEXT UNIQUE;

INSERT INTO accounts (name, type) VALUES
    ('Paystack receivable', 'ASSET'),
    ('Print shop payable', 'LIABILITY'),
    ('Platform fee revenue', 'REVENUE')
ON CONFLICT (name) DO NOTHING;

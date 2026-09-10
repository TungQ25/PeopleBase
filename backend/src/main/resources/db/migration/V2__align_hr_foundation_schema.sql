ALTER TABLE departments DROP CONSTRAINT fk_departments_manager;
ALTER TABLE departments DROP COLUMN manager_id;
DROP INDEX IF EXISTS idx_departments_manager_id;

ALTER TABLE positions ADD COLUMN level INTEGER;

ALTER TABLE employees RENAME COLUMN email TO company_email;
ALTER TABLE employees RENAME COLUMN join_date TO hire_date;
ALTER TABLE employees ADD COLUMN first_name VARCHAR(75);
ALTER TABLE employees ADD COLUMN last_name VARCHAR(75);
ALTER TABLE employees ADD COLUMN personal_email VARCHAR(150);
ALTER TABLE employees ADD COLUMN manager_id BIGINT;

UPDATE employees
SET first_name = COALESCE(NULLIF(SPLIT_PART(full_name, ' ', 1), ''), 'Unknown'),
    last_name = COALESCE(
        NULLIF(BTRIM(CASE
            WHEN POSITION(' ' IN full_name) > 0 THEN SUBSTRING(full_name FROM POSITION(' ' IN full_name) + 1)
            ELSE ''
        END), ''),
        'Unknown'
    )
WHERE first_name IS NULL OR last_name IS NULL;

ALTER TABLE employees ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE employees ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE employees RENAME CONSTRAINT uk_employees_email TO uk_employees_company_email;
ALTER TABLE employees
    ADD CONSTRAINT fk_employees_manager
    FOREIGN KEY (manager_id) REFERENCES employees (id) ON DELETE SET NULL;
CREATE INDEX idx_employees_manager_id ON employees (manager_id);

ALTER TABLE user_accounts ADD COLUMN status VARCHAR(20);
UPDATE user_accounts
SET status = CASE WHEN active THEN 'ACTIVE' ELSE 'DISABLED' END;
ALTER TABLE user_accounts ALTER COLUMN status SET NOT NULL;
ALTER TABLE user_accounts DROP COLUMN active;

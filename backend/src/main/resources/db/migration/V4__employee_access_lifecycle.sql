-- Existing employees keep their access requirement; new profiles choose it explicitly.
ALTER TABLE employees
    ADD COLUMN requires_system_access BOOLEAN NOT NULL DEFAULT TRUE;

-- A profile may exist without a company email or login account.
ALTER TABLE employees ALTER COLUMN company_email DROP NOT NULL;
ALTER TABLE employees ALTER COLUMN status SET DEFAULT 'PENDING_ONBOARDING';

ALTER TABLE employees ADD CONSTRAINT ck_employees_lifecycle_status
    CHECK (status IN ('PENDING_ONBOARDING', 'ACTIVE', 'INACTIVE', 'RESIGNED'));

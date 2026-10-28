package com.peoplebase.api;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

/** Only an explicitly named disposable database on the dedicated test cluster is accepted. */
@EnabledIfSystemProperty(named = "declarations.test.upgrade.url",
        matches = "jdbc:postgresql://127\\.0\\.0\\.1:55441/peoplebase_declarations_upgrade_[a-z0-9_]+")
class EntityDeclarationMigrationTests {
    @Test
    void v4ToV5PreservesLegacyIdentityAndMapsAll51Tables() throws Exception {
        String url = System.getProperty("declarations.test.upgrade.url");
        Flyway.configure().dataSource(url, "entity_declarations", "").target("4").load().migrate();
        try (Connection c = DriverManager.getConnection(url, "entity_declarations", ""); Statement s = c.createStatement()) {
            s.execute("INSERT INTO departments(code,name) VALUES ('DECL','Declaration fixture')");
            s.execute("INSERT INTO positions(code,name) VALUES ('DECL','Declaration fixture')");
            s.execute("INSERT INTO employees(employee_code,full_name,first_name,last_name,hire_date,status,department_id,position_id) VALUES ('DECL','Fixture Person','Fixture','Person',DATE '2026-01-01','ACTIVE',1,1)");
            s.execute("INSERT INTO user_accounts(username,password_hash,role,status,employee_id) VALUES ('declaration-fixture','fixture-hash','HR','ACTIVE',1)");
            s.execute("INSERT INTO leave_requests(start_date,end_date,leave_type,reason,status,requester_id) SELECT DATE '2026-01-01',DATE '2026-01-02',code,'fixture','PENDING',1 FROM (VALUES ('ANNUAL'),('SICK'),('UNPAID'),('OTHER')) t(code)");
        }
        assertThat(Flyway.configure().dataSource(url, "entity_declarations", "").target("5")
                .load().migrate().migrationsExecuted).isEqualTo(1);
        try (Connection c = DriverManager.getConnection(url, "entity_declarations", ""); Statement s = c.createStatement()) {
            assertThat(count(s, "SELECT count(*) FROM account_roles WHERE account_id=1 AND role='HR'")).isEqualTo(1);
            assertThat(count(s, "SELECT count(*) FROM leave_requests l JOIN leave_types t ON t.id=l.leave_type_id JOIN workflow_requests w ON w.id=l.workflow_request_id WHERE t.code=l.legacy_leave_type AND w.status=l.legacy_status AND w.target_employee_id=l.legacy_requester_id")).isEqualTo(4);
            assertThat(count(s, "SELECT count(*) FROM approval_decisions")).isZero();
            assertThat(count(s, "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE' AND table_name<>'flyway_schema_history'")).isEqualTo(51);
            assertThat(count(s, "SELECT count(*) FROM employee_assignments WHERE employee_id=1")).isEqualTo(1);
        }
        assertThat(Flyway.configure().dataSource(url, "entity_declarations", "").target("5")
                .load().validateWithResult().validationSuccessful).isTrue();
    }

    private long count(Statement s, String sql) throws SQLException {
        try (ResultSet r = s.executeQuery(sql)) { r.next(); return r.getLong(1); }
    }
}

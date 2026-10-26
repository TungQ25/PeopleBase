package com.peoplebase.api;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.auth.enums.Role;
import com.peoplebase.api.auth.repository.UserAccountRepository;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.RoutingSnapshot;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.leave.entity.LeaveRequest;
import com.peoplebase.api.leave.repository.LeaveRequestRepository;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.entity.Position;
import com.peoplebase.api.workflow.entity.WorkflowRequest;
import com.peoplebase.api.workflow.enums.WorkflowRequestStatus;
import com.peoplebase.api.workflow.enums.WorkflowRequestType;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Mapping checks only; no new workflow, payroll or authorization service is exercised. */
@SpringBootTest(properties = {
        "spring.datasource.url=${declarations.test.url:jdbc:h2:mem:entity-declarations;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}",
        "spring.datasource.username=${declarations.test.username:sa}",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=${declarations.test.driver:org.h2.Driver}",
        "spring.jpa.hibernate.ddl-auto=${declarations.test.ddl:create-drop}",
        "spring.flyway.enabled=${declarations.test.flyway:false}",
        "spring.flyway.target=5",
        "spring.docker.compose.enabled=false",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=WARN",
        "app.bootstrap.admin.username=",
        "app.bootstrap.admin.password="
})
@Transactional
class EntityDeclarationsTests {
    @Autowired EntityManager em;
    @Autowired UserAccountRepository accounts;
    @Autowired LeaveRequestRepository leaves;

    @Test
    void discoversAll51EntitiesIncludingSevenPayrollEntities() {
        var entities = em.getMetamodel().getEntities();
        assertThat(entities).hasSize(51);
        assertThat(entities.stream().filter(e -> e.getJavaType().getPackageName()
                .equals("com.peoplebase.api.payroll.entity"))).hasSize(7);
        assertThat(entities).allSatisfy(e -> assertThat(e.hasSingleIdAttribute()).isTrue());
    }

    @Test
    void associationsAreLazyAndDoNotCascadeHistoricalDeletion() {
        for (var entity : em.getMetamodel().getEntities()) {
            for (Class<?> type = entity.getJavaType(); type != null; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    var many = field.getAnnotation(ManyToOne.class);
                    var one = field.getAnnotation(OneToOne.class);
                    var children = field.getAnnotation(OneToMany.class);
                    if (many != null) {
                        assertThat(many.fetch()).as(field.toString()).isEqualTo(FetchType.LAZY);
                        assertThat(many.cascade()).doesNotContain(CascadeType.REMOVE, CascadeType.ALL);
                    }
                    if (one != null) {
                        assertThat(one.fetch()).as(field.toString()).isEqualTo(FetchType.LAZY);
                        assertThat(one.cascade()).doesNotContain(CascadeType.REMOVE, CascadeType.ALL);
                        assertThat(one.orphanRemoval()).isFalse();
                    }
                    if (children != null) {
                        assertThat(children.fetch()).as(field.toString()).isEqualTo(FetchType.LAZY);
                        assertThat(children.cascade()).doesNotContain(CascadeType.REMOVE, CascadeType.ALL);
                        assertThat(children.orphanRemoval()).isFalse();
                    }
                }
            }
        }
        assertThat(VersionedEntity.class.getDeclaredFields())
                .anySatisfy(f -> assertThat(f.getAnnotation(Version.class)).isNotNull());
    }

    @Test
    void draftLeaveAndVersionedJsonRoundTripWithoutInventingRequiredSubmitData() {
        Department department = new Department();
        department.setCode("DECL"); department.setName("Declaration fixture"); em.persist(department);
        Position position = new Position();
        position.setCode("DECL"); position.setName("Declaration fixture"); em.persist(position);
        Employee employee = new Employee();
        employee.setEmployeeCode("DECL"); employee.setFirstName("Fixture"); employee.setLastName("Person");
        employee.setFullName("Fixture Person"); employee.setHireDate(LocalDate.of(2026, 1, 1));
        employee.setDepartment(department); employee.setPosition(position); em.persist(employee);
        WorkflowRequest workflow = new WorkflowRequest();
        workflow.setRequestCode("DECL-DRAFT"); workflow.setTargetEmployee(employee);
        workflow.setRequestType(WorkflowRequestType.LEAVE); workflow.setStatus(WorkflowRequestStatus.DRAFT);
        workflow.setRoutingSnapshot(new RoutingSnapshot(1, List.of(), false, null, null, null, null, null));
        em.persist(workflow);
        LeaveRequest leave = new LeaveRequest(); leave.setWorkflowRequest(workflow); em.persist(leave);
        em.flush();
        Long leaveId = leave.getId(); Long employeeId = employee.getId(); em.clear();
        LeaveRequest loaded = em.find(LeaveRequest.class, leaveId);
        assertThat(loaded.getStartDate()).isNull();
        assertThat(loaded.getLeaveType()).isNull();
        assertThat(loaded.getWorkflowRequest().getRoutingSnapshot().schemaVersion()).isEqualTo(1);
        assertThat(loaded.getRequester().getId()).isEqualTo(employeeId);
        assertThat(leaves.findByRequesterIdOrderByCreatedAtDesc(employeeId)).hasSize(1);
    }

    @Test
    void existingSingleRoleAccessorsAndQueriesUseAccountRoleAssociation() {
        UserAccount account = new UserAccount();
        account.setUsername("declaration-fixture"); account.setPasswordHash("fixture-hash");
        account.setRole(Role.ADMIN); em.persist(account); em.flush(); em.clear();
        UserAccount loaded = accounts.findByUsernameIgnoreCase("DECLARATION-FIXTURE").orElseThrow();
        assertThat(loaded.getActiveRoles()).containsExactly(Role.ADMIN);
        assertThat(loaded.getAccountRoles()).hasSize(1);
        assertThat(accounts.existsByRole(Role.ADMIN)).isTrue();
        assertThat(accounts.existsByRole(Role.MANAGER)).isFalse();
    }
}

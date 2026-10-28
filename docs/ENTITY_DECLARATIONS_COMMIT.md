# Khai báo persistence PeopleBase

Phạm vi commit ngày 27/10/2026: khai báo **51 entity hiện có trong working tree**, enum, mapped superclass, record snapshot JSONB, hai repository cần thích nghi với mapping và schema nền V4–V5. Mô hình lương hiện tại có 7 entity theo bản rút gọn trong repository; không khôi phục phương án 56 entity cũ.

Không tính enum, DTO, record JSONB, BaseEntity/VersionedEntity/AppendOnlyEntity vào số entity. Department và Position được tái sử dụng; Employee có thể không có UserAccount. Role vẫn là enum cố định, AccountRole lưu các lần gán; LeaveType là entity danh mục, tên enum cũ được phân biệt bằng LegacyLeaveType. WorkflowRequest giữ trạng thái chung của đơn.

Commit giữ field, type/precision/length, nullable cho bản nháp, LAZY association, FK/unique/index, optimistic version và snapshot có schemaVersion. UserAccount giữ accessor role tương thích với service/DTO đã commit trước đó; không ghi lại cột role cũ. UserAccountRepository và LeaveRequestRepository được điều chỉnh vì đường dẫn thuộc tính persistent đã thay đổi.

V4 khai báo lifecycle/access hiện tại. V5 tạo schema/backfill AccountRole, LeaveType, workflow của đơn cũ và phân công quan sát tại lúc chuyển đổi; giữ provenance, không dựng lịch sử phê duyệt giả. Các cột legacy dùng để lưu dữ liệu cũ, không được entity mới ghi. V1–V3 không sửa.

Các thay đổi Service, Controller, JWT/security, DTO, frontend và **V6__entity_integrity_guards.sql** không nằm trong commit. V6 chứa xử lý workflow/ledger/chốt công/KPI/lương và trigger nghiệp vụ, được giữ ở working tree. Vì vậy commit khai báo này chưa cung cấp xử lý duyệt đơn, áp hiệu lực, cập nhật ledger, tính/chốt/chi lương hoặc kiểm soát đầy đủ state transition. Các tổng quỹ phép chỉ là mapping read-only; chưa có cơ chế ledger của V6 trong commit để cập nhật chúng. Lifecycle callback append-only và accessor tương thích hiện có là bảo vệ persistence, không phải API nghiệp vụ mới.

Kiểm thử dành riêng cho phần khai báo: EntityDeclarationsTests kiểm tra JPA khởi tạo đủ 51 entity, LAZY/cascade, version, bản nháp, round-trip JSONB và query tương thích. EntityDeclarationMigrationTests kiểm tra nâng cấp V4→V5 trên database thử nghiệm riêng. Chỉ chạy các test này đối với snapshot của đúng nội dung commit; không dùng kết quả full working tree để chứng minh commit độc lập chạy được.

Kết quả kiểm tra trên snapshot riêng xuất từ Git index (không lấy các Service/Controller/JWT/DTO đang sửa trong working tree):

| Kiểm tra | Kết quả |
|---|---|
| Compile + EntityDeclarationsTests với H2 | PASS, 4 test, 0 lỗi/skip |
| Flyway tạo mới V1–V5 + Hibernate `validate` trên PostgreSQL 18 | PASS, đủ 51 entity; round-trip JSONB và query repository tương thích |
| EntityDeclarationMigrationTests, V4→V5 | PASS; 4 mã phép/đơn cũ, role HR và phân công được backfill, không tạo ApprovalDecision giả |
| Tổng lượt PostgreSQL | PASS, 5 test, 0 lỗi/skip |
| Git diff staged / whitespace | PASS; không có Service, Controller, security, DTO, frontend, application.properties hoặc V6 |

Hai database kiểm thử được tạo mới trong cluster riêng chỉ nghe `127.0.0.1:55441`, sau kiểm tra cluster đã dừng. Không áp migration lên database ứng dụng đang dùng. Không chạy test nghiệp vụ hiện có hoặc frontend trong phạm vi commit này; kết quả trên chỉ chứng minh khai báo/schema và tương thích mapping, không nghiệm thu 40 UC.

Lệnh H2: `mvn -o -Dtest=EntityDeclarationsTests test`. Lượt PostgreSQL chạy cùng `EntityDeclarationMigrationTests`, đặt `declarations.test.url`, `declarations.test.upgrade.url` tới hai database dùng một lần, `declarations.test.username=entity_declarations`, `declarations.test.driver=org.postgresql.Driver`, `declarations.test.ddl=validate`, `declarations.test.flyway=true`. Annotation giới hạn Flyway target=5; không áp V6. H2 là dependency test duy nhất thêm vào pom.xml của commit; các thay đổi build khác vẫn ngoài commit.

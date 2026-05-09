# NASync Test Plan
**Project:** IT342 NASync G4 Vallo  
**Branch:** refactor/vertical-slice-architecture  
**Date:** 2026-05-04  
**Version:** 1.0

---

## 1. Objectives

Verify that all functional requirements of the NASync system remain intact after the Vertical Slice Architecture refactoring. This plan covers backend API endpoints, web frontend flows, and mobile authentication screens.

---

## 2. Scope

| Feature Slice | Backend | Web | Mobile |
|---|---|---|---|
| Authentication | / | / | / |
| Admin Management | / | / | / |
| Branch Management | / | / | — |
| Department Management | / | / | — |
| Semester Management | / | / | — |
| Duty Day Management | / | / | — |
| Scholar Duty | / | / | / |
| Dept Head Duty | / | / | — |

---

## 3. Test Approach

- **Unit Tests:** JUnit 5 + Mockito for all service classes (mocked repositories)
- **Integration / Smoke Tests:** Manual browser walkthrough and API calls via the running Spring Boot server
- **Regression Scope:** All test cases re-executed after every commit to the refactoring branch

---

## 4. Functional Requirements & Test Cases

### 4.1 Authentication

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| AUTH-01 | User can log in with valid school ID and password | POST `/api/v1/auth/login` with correct credentials | 200 OK, JWT access token returned |
| AUTH-02 | Login fails with wrong password | POST `/api/v1/auth/login` with wrong password | 400 Bad Request, error message |
| AUTH-03 | Login fails with non-existent school ID | POST `/api/v1/auth/login` with unknown ID | 400 Bad Request, error message |
| AUTH-04 | Inactive user cannot log in | POST `/api/v1/auth/login` with inactive account | 400 Bad Request, account inactive message |
| AUTH-05 | Role-based redirect after login (ADMIN) | Login as ADMIN via web | Redirected to `/admin` |
| AUTH-06 | Role-based redirect after login (SCHOLAR) | Login as SCHOLAR via web | Redirected to `/scholar` |
| AUTH-07 | Role-based redirect after login (DEPARTMENT_HEAD) | Login as DEPT HEAD via web | Redirected to `/depthead` |
| AUTH-08 | Protected route blocks unauthenticated access | Navigate to `/admin` without token | Redirected to `/login` |
| AUTH-09 | Wrong-role user cannot access another role's route | SCHOLAR navigates to `/admin` | Redirected to `/scholar` |
| AUTH-10 | Google OAuth redirects to callback with token | Click Google login | Redirected to `/auth/callback?token=...` |
| AUTH-11 | Refresh token is issued on successful login | POST `/api/v1/auth/login` | Refresh token persisted in DB |
| AUTH-12 | Mobile: Login with valid credentials | LoginActivity submit | Navigate to correct dashboard |
| AUTH-13 | Mobile: Login with invalid credentials | LoginActivity submit | Toast error shown |

### 4.2 Admin Management

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| ADMIN-01 | Admin can register a new Scholar | POST `/api/v1/admin/users/register` with SCHOLAR role | 201 Created, user returned |
| ADMIN-02 | Admin can register a new Department Head | POST `/api/v1/admin/users/register` with DEPARTMENT_HEAD role | 201 Created, user returned |
| ADMIN-03 | Duplicate school ID is rejected | Register with existing school ID | 400 Bad Request |
| ADMIN-04 | Duplicate email is rejected | Register with existing email | 400 Bad Request |
| ADMIN-05 | Admin can list all users | GET `/api/v1/admin/users` | 200 OK, list returned |
| ADMIN-06 | Admin can filter scholars | GET `/api/v1/admin/users/scholars` | 200 OK, only SCHOLAR users |
| ADMIN-07 | Admin can filter department heads | GET `/api/v1/admin/users/department-heads` | 200 OK, only DEPT_HEAD users |
| ADMIN-08 | Admin can toggle user active status | PUT `/api/v1/admin/users/{id}/toggle-active` | 200 OK, status flipped |
| ADMIN-09 | Admin can reassign user to different dept/branch | PUT `/api/v1/admin/users/{id}/reassign` | 200 OK, assignment updated |
| ADMIN-10 | Admin can update user details | PUT `/api/v1/admin/users/{id}` | 200 OK, updated user returned |
| ADMIN-11 | Non-admin cannot access admin endpoints | Call admin endpoint with SCHOLAR token | 403 Forbidden |

### 4.3 Branch Management

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| BRANCH-01 | Admin can list all branches | GET `/api/v1/admin/branches` | 200 OK, list returned |
| BRANCH-02 | Admin can create a branch | POST `/api/v1/admin/branches` with name + deptId | 200 OK, branch returned |
| BRANCH-03 | Duplicate branch name in same dept is rejected | POST with existing name in same dept | 400 Bad Request |
| BRANCH-04 | Admin can update a branch | PUT `/api/v1/admin/branches/{id}` | 200 OK, updated branch returned |
| BRANCH-05 | Admin can delete an empty branch | DELETE `/api/v1/admin/branches/{id}` (no users) | 200 OK, deleted message |
| BRANCH-06 | Cannot delete branch with assigned users | DELETE branch that has users | 400 Bad Request |

### 4.4 Department Management

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| DEPT-01 | Admin can list all departments | GET `/api/v1/admin/departments` | 200 OK, list returned |
| DEPT-02 | Admin can create a department | POST `/api/v1/admin/departments` with name | 200 OK, department returned |
| DEPT-03 | Duplicate department name is rejected | POST with existing name | 400 Bad Request |
| DEPT-04 | Admin can update a department | PUT `/api/v1/admin/departments/{id}` | 200 OK, updated dept returned |
| DEPT-05 | Cannot delete department with existing branches | DELETE dept that has branches | 400 Bad Request |

### 4.5 Semester Management

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| SEM-01 | Admin can create a semester | POST `/api/v1/admin/semesters` with label + dates | 201 Created |
| SEM-02 | Duplicate semester label is rejected | POST with existing label | 400 Bad Request |
| SEM-03 | End date before start date is rejected | POST with endDate < startDate | 400 Bad Request |
| SEM-04 | Admin can activate a semester | PUT `/api/v1/admin/semesters/{id}/activate` | 200 OK, semester is active |
| SEM-05 | Activating one semester deactivates all others | Activate semester B when A is active | A becomes inactive, B becomes active |
| SEM-06 | Admin can deactivate an active semester | PUT `/api/v1/admin/semesters/{id}/deactivate` | 200 OK, semester inactive |
| SEM-07 | Admin can get active semester | GET `/api/v1/admin/semesters/active` | 200 OK, active semester or message |
| SEM-08 | Admin can list all semesters | GET `/api/v1/admin/semesters` | 200 OK, list returned |

### 4.6 Duty Day Management

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| DD-01 | Admin can create a duty day | POST `/api/v1/admin/duty-days` with date + type | 201 Created |
| DD-02 | Duplicate date in same semester is rejected | POST with existing date | 400 Bad Request |
| DD-03 | Admin can list duty days by semester | GET `/api/v1/admin/duty-days?semesterId=X` | 200 OK, list returned |
| DD-04 | Admin can delete a duty day | DELETE `/api/v1/admin/duty-days/{id}` | 200 OK, deleted message |

### 4.7 Scholar Duty

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| SCH-01 | Scholar can clock in on a regular day | POST `/api/v1/scholar/duties/clock-in` during active semester | 200 OK, duty record created |
| SCH-02 | Clock-in blocked on HOLIDAY/SUSPENDED day | Clock in on a holiday duty day | 400 Bad Request |
| SCH-03 | Clock-in blocked when open duty exists | Clock in twice in same day | 400 Bad Request |
| SCH-04 | Clock-in more than 60 min late marks ABSENT | Clock in 61+ min after expected time | ABSENT record created, 400 error |
| SCH-05 | Clock-in 10-59 min late marks LATE | Clock in 15 min after expected time | LATE attendance status |
| SCH-06 | Scholar can clock out | PUT `/api/v1/scholar/duties/clock-out` | 200 OK, timeOut recorded |
| SCH-07 | Makeup/Overtime requires minimum 1 hour | Clock out after 30 min on MAKEUP duty | 400 Bad Request |
| SCH-08 | 3 lates auto-convert to 1 absent | Clock in late 3 times in same semester | Absent record auto-added |
| SCH-09 | Scholar can view their duties | GET `/api/v1/scholar/duties` | 200 OK, list of duties |
| SCH-10 | Scholar can view duty summary | GET `/api/v1/scholar/duties/summary` | 200 OK, counts and hours |
| SCH-11 | Scholar cannot access admin or depthead endpoints | Call with SCHOLAR token | 403 Forbidden |

### 4.8 Dept Head Duty

| ID | Requirement | Test Case | Expected Result |
|---|---|---|---|
| DH-01 | Dept Head can view pending duties in their branch | GET `/api/v1/depthead/duties/pending` | 200 OK, PENDING duties only |
| DH-02 | Dept Head can approve a pending duty | PUT `/api/v1/depthead/duties/{id}/approve` | 200 OK, status = APPROVED |
| DH-03 | Dept Head can reject a pending duty | PUT `/api/v1/depthead/duties/{id}/reject` | 200 OK, status = REJECTED |
| DH-04 | Cannot approve a non-PENDING duty | Approve already-approved duty | 400 Bad Request |
| DH-05 | Dept Head can view all branch duties | GET `/api/v1/depthead/duties` | 200 OK, all branch duties |
| DH-06 | Dept Head can view scholars in their branch | GET `/api/v1/depthead/duties/scholars` | 200 OK, scholar list |
| DH-07 | Dept Head without branch gets error | Call with unassigned dept head | 400 Bad Request |

---

## 5. Regression Test Matrix

| Test ID | auth | admin | branch | dept | semester | duty-day | scholar | depthead |
|---|---|---|---|---|---|---|---|---|
| Smoke: app starts | / | / | / | / | / | / | / | / |
| Login + redirect | / | | | | | | | |
| Role-based access | / | / | / | / | / | / | / | / |
| CRUD operations | | / | / | / | / | / | | |
| Business rules | | | | | / | / | / | / |

---

## 6. Test Environment

| Component | Details |
|---|---|
| Backend | Spring Boot 4.0.3, Java 21, port 8080 |
| Database | Supabase PostgreSQL 17.6 |
| Web Frontend | React + Vite 7.3.1, port 5173 |
| Mobile | Android (Kotlin), emulator or physical device |
| Test Framework | JUnit 5, Mockito, Spring Boot Test |

---

## 7. Pass/Fail Criteria

- **PASS:** HTTP status code matches expected, response body contains expected fields, no unhandled exceptions
- **FAIL:** Unexpected HTTP status, missing fields, runtime exception, UI navigation error
- **BLOCKED:** Test cannot execute due to missing prerequisite (e.g., no active semester)

---

## 8. Automated Test Files

| File | Feature |
|---|---|
| `src/test/.../features/auth/AuthServiceTest.java` | Authentication |
| `src/test/.../features/admin/AdminServiceTest.java` | Admin Management |
| `src/test/.../features/branch/BranchServiceTest.java` | Branch Management |
| `src/test/.../features/department/DepartmentServiceTest.java` | Department Management |
| `src/test/.../features/semester/SemesterServiceTest.java` | Semester Management |
| `src/test/.../features/dutyday/DutyDayServiceTest.java` | Duty Day Management |
| `src/test/.../features/duty/DutyServiceTest.java` | Scholar + DeptHead Duty |

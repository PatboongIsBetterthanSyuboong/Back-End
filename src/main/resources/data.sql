INSERT INTO disease (id, code, name)
VALUES (1, 'A001', 'Cholera')
ON DUPLICATE KEY UPDATE code = VALUES(code), name = VALUES(name);

INSERT INTO disease (id, code, name)
VALUES (2, 'A002', 'HIV')
ON DUPLICATE KEY UPDATE code = VALUES(code), name = VALUES(name);

INSERT INTO diagnose (id, code, name, dose, time, days)
VALUES (1, 'B001', 'Ducoral sol', 3, UNIX_TIMESTAMP(), 3)
ON DUPLICATE KEY UPDATE code = VALUES(code),
                        name = VALUES(name),
                        dose = VALUES(dose),
                        time = VALUES(time),
                        days = VALUES(days);

INSERT INTO diagnose (id, code, name, dose, time, days)
VALUES (2, 'B002', 'Shiver Stop', 5, UNIX_TIMESTAMP(), 2)
ON DUPLICATE KEY UPDATE code = VALUES(code),
                        name = VALUES(name),
                        dose = VALUES(dose),
                        time = VALUES(time),
                        days = VALUES(days);

INSERT INTO employee (id, name, dept_id, role, username, password)
VALUES (1, '관리자', 1, 'ADMIN', 'employee01', '$2a$10$xP7NfRNDaWR5w7FoD4nYMeA8iXU8cnW7NIgZBWLTTrNCRXKgTUSwe')
ON DUPLICATE KEY UPDATE name = VALUES(name),
                        dept_id = VALUES(dept_id),
                        role = VALUES(role),
                        username = VALUES(username),
                        password = VALUES(password);
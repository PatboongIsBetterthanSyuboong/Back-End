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
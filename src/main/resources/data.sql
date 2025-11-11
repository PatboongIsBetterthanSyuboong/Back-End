INSERT INTO disease (id, code, name)
VALUES (1, 'A', 'Cholera')
ON DUPLICATE KEY UPDATE code = VALUES(code), name = VALUES(name);

INSERT INTO diagnose (id, code, name, dose, time, days)
VALUES (1, 'A', 'Ducoral sol', 3, UNIX_TIMESTAMP(), 3)
ON DUPLICATE KEY UPDATE code = VALUES(code),
                        name = VALUES(name),
                        dose = VALUES(dose),
                        time = VALUES(time),
                        days = VALUES(days);


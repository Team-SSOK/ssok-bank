INSERT IGNORE INTO good (name, account_type_code, interest_rate, interest_cycle, created_at, updated_at)
VALUES
('기본 예금 상품', 'DEPOSIT', 1.5, 30, NOW(), NOW()),
('기본 적금 상품', 'SAVINGS', 2.0, 30, NOW(), NOW());
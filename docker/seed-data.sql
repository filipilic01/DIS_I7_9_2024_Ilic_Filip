-- ============================================================
-- Banking Microservices - Seed Data
-- Plain-text password for all users: password123
-- Run AFTER all services have started (tables must exist):
--   psql -U postgres -h localhost -f docker/seed-data.sql
-- ============================================================

-- ─── userdb ──────────────────────────────────────────────────────────────────

\connect userdb

INSERT INTO users (username, email, password, first_name, last_name, phone_number, status, created_at) VALUES
('johndoe',     'john.doe@example.com',    '$2a$10$dkxm1m/s3lh3Z6I/LTjrfuQxZ4jQQXZb3VNaO/0ABY1dqI6ledcZu', 'John',  'Doe',     '+381611234567', 'ACTIVE',    '2024-01-10 09:00:00'),
('janesmith',   'jane.smith@example.com',  '$2a$10$dkxm1m/s3lh3Z6I/LTjrfuQxZ4jQQXZb3VNaO/0ABY1dqI6ledcZu', 'Jane',  'Smith',   '+381622345678', 'ACTIVE',    '2024-02-15 10:30:00'),
('markjohnson', 'mark.johnson@example.com','$2a$10$dkxm1m/s3lh3Z6I/LTjrfuQxZ4jQQXZb3VNaO/0ABY1dqI6ledcZu', 'Mark',  'Johnson', '+381633456789', 'ACTIVE',    '2024-03-20 11:00:00'),
('peraperovic', 'pera.perovic@example.com','$2a$10$dkxm1m/s3lh3Z6I/LTjrfuQxZ4jQQXZb3VNaO/0ABY1dqI6ledcZu', 'Pera',  'Perovic', '+381644567890', 'ACTIVE',    '2024-04-05 08:00:00'),
('anamilic',    'ana.milic@example.com',   '$2a$10$dkxm1m/s3lh3Z6I/LTjrfuQxZ4jQQXZb3VNaO/0ABY1dqI6ledcZu', 'Ana',   'Milic',   '+381655678901', 'SUSPENDED', '2024-05-01 14:00:00');

-- ─── accountdb ───────────────────────────────────────────────────────────────

\connect accountdb

INSERT INTO accounts (account_number, user_id, type, balance, currency, status, created_at) VALUES
('160-0000000001-76', 1, 'CHECKING', 150000.00,   'RSD', 'ACTIVE',   '2024-01-11 09:00:00'),
('160-0000000002-83', 1, 'SAVINGS',  500000.00,   'RSD', 'ACTIVE',   '2024-01-11 09:05:00'),
('160-0000000003-91', 2, 'CHECKING', 75000.00,    'RSD', 'ACTIVE',   '2024-02-16 10:00:00'),
('160-0000000004-08', 2, 'SAVINGS',  200000.00,   'RSD', 'ACTIVE',   '2024-02-16 10:05:00'),
('160-0000000005-15', 3, 'BUSINESS', 1200000.00,  'RSD', 'ACTIVE',   '2024-03-21 11:00:00'),
('160-0000000006-22', 3, 'CHECKING', 45000.00,    'RSD', 'ACTIVE',   '2024-03-21 11:05:00'),
('160-0000000007-30', 4, 'CHECKING', 30000.00,    'RSD', 'ACTIVE',   '2024-04-06 08:00:00'),
('160-0000000008-47', 4, 'SAVINGS',  120000.00,   'RSD', 'ACTIVE',   '2024-04-06 08:05:00'),
('160-0000000009-54', 5, 'CHECKING', 0.00,        'RSD', 'FROZEN',   '2024-05-02 14:00:00');

-- ─── transactiondb ───────────────────────────────────────────────────────────

\connect transactiondb

INSERT INTO transactions (source_account_id, target_account_id, amount, type, status, description, created_at, completed_at) VALUES
(1, NULL, 500000.00,  'DEPOSIT',    'COMPLETED', 'Initial deposit - cash',              '2024-01-12 09:00:00', '2024-01-12 09:00:01'),
(2, NULL, 500000.00,  'DEPOSIT',    'COMPLETED', 'Initial deposit - savings',           '2024-01-12 09:10:00', '2024-01-12 09:10:01'),
(3, NULL, 200000.00,  'DEPOSIT',    'COMPLETED', 'Initial deposit - cash',              '2024-02-17 10:00:00', '2024-02-17 10:00:01'),
(4, NULL, 200000.00,  'DEPOSIT',    'COMPLETED', 'Initial deposit - savings',           '2024-02-17 10:05:00', '2024-02-17 10:05:01'),
(5, NULL, 2000000.00, 'DEPOSIT',    'COMPLETED', 'Business account initial funding',    '2024-03-22 11:00:00', '2024-03-22 11:00:01'),
(6, NULL, 100000.00,  'DEPOSIT',    'COMPLETED', 'Initial deposit - cash',              '2024-03-22 11:05:00', '2024-03-22 11:05:01'),
(7, NULL, 50000.00,   'DEPOSIT',    'COMPLETED', 'Initial deposit - cash',              '2024-04-07 08:00:00', '2024-04-07 08:00:01'),
(8, NULL, 120000.00,  'DEPOSIT',    'COMPLETED', 'Initial deposit - savings',           '2024-04-07 08:05:00', '2024-04-07 08:05:01'),
(1, NULL, 200000.00,  'WITHDRAWAL', 'COMPLETED', 'ATM withdrawal',                      '2024-03-01 14:00:00', '2024-03-01 14:00:02'),
(3, NULL, 80000.00,   'WITHDRAWAL', 'COMPLETED', 'ATM withdrawal',                      '2024-04-10 16:00:00', '2024-04-10 16:00:02'),
(5, NULL, 500000.00,  'WITHDRAWAL', 'COMPLETED', 'Business expense payment',            '2024-05-05 09:30:00', '2024-05-05 09:30:02'),
(6, NULL, 55000.00,   'WITHDRAWAL', 'COMPLETED', 'ATM withdrawal',                      '2024-05-10 13:00:00', '2024-05-10 13:00:02'),
(1, 3,    50000.00,   'TRANSFER',   'COMPLETED', 'Transfer - John to Jane',             '2024-04-01 11:00:00', '2024-04-01 11:00:03'),
(3, 1,    25000.00,   'TRANSFER',   'COMPLETED', 'Transfer - Jane to John',             '2024-05-15 13:00:00', '2024-05-15 13:00:03'),
(5, 1,    100000.00,  'TRANSFER',   'COMPLETED', 'Business payment to John',            '2024-06-01 10:00:00', '2024-06-01 10:00:03'),
(2, 4,    200000.00,  'TRANSFER',   'COMPLETED', 'Savings transfer between accounts',   '2024-06-10 12:00:00', '2024-06-10 12:00:03'),
(7, 3,    20000.00,   'TRANSFER',   'COMPLETED', 'Pera to Jane transfer',               '2024-05-20 09:00:00', '2024-05-20 09:00:03'),
(1, NULL, 150000.00,  'WITHDRAWAL', 'COMPLETED', 'Loan disbursement repayment',         '2024-07-01 10:00:00', '2024-07-01 10:00:02'),
(5, NULL, 300000.00,  'WITHDRAWAL', 'COMPLETED', 'Business payroll',                    '2024-07-15 11:00:00', '2024-07-15 11:00:02'),
(3, NULL, 70000.00,   'WITHDRAWAL', 'FAILED',    'ATM withdrawal - insufficient funds', '2024-08-01 15:00:00', NULL);

-- ─── loandb ──────────────────────────────────────────────────────────────────

\connect loandb

INSERT INTO loans (user_id, account_id, amount, interest_rate, term_months, monthly_installment, remaining_amount, status, type, purpose, start_date, end_date, created_at) VALUES
(1, 1, 300000.00,   8.50, 36,  9478.80,   166543.00,  'ACTIVE',   'PERSONAL', 'Home renovation',                 '2024-01-15', '2027-01-15', '2024-01-14 10:00:00'),
(1, 2, 5000000.00,  4.50, 240, 31650.00,  4503998.00, 'ACTIVE',   'MORTGAGE', 'Apartment purchase - Novi Sad',   '2023-06-01', '2043-06-01', '2023-05-28 09:00:00'),
(2, 3, 800000.00,   7.20, 60,  15910.40,  479680.00,  'ACTIVE',   'AUTO',     'Car purchase - Toyota Corolla',   '2024-03-01', '2029-03-01', '2024-02-25 11:00:00'),
(3, 5, 2000000.00,  6.00, 84,  29216.00,  2000000.00, 'PENDING',  'BUSINESS', 'Business expansion - new branch', NULL,          NULL,          '2026-06-01 09:00:00'),
(2, 4, 150000.00,   9.00, 24,  6852.00,   0.00,       'PAID_OFF', 'PERSONAL', 'Electronics and furniture',       '2022-05-01', '2024-04-30', '2022-04-20 10:00:00'),
(4, 7, 120000.00,   8.00, 24,  5440.00,   80000.00,   'ACTIVE',   'PERSONAL', 'Education expenses',              '2025-01-01', '2027-01-01', '2024-12-20 09:00:00'),
(1, 1, 500000.00,   7.50, 48,  12100.00,  500000.00,  'REJECTED', 'AUTO',     'Luxury vehicle',                  NULL,          NULL,          '2024-11-01 10:00:00');

INSERT INTO loan_payments (loan_id, amount, principal_part, interest_part, due_date, paid_date, status, created_at) VALUES
-- Loan 1 (PERSONAL, 300000 RSD, 8.5%, user=1): 17 payments paid
(1, 9478.80, 7353.80, 2125.00, '2024-02-15', '2024-02-14', 'PAID', '2024-02-14 10:00:00'),
(1, 9478.80, 7405.92, 2072.88, '2024-03-15', '2024-03-14', 'PAID', '2024-03-14 10:00:00'),
(1, 9478.80, 7458.42, 2020.38, '2024-04-15', '2024-04-14', 'PAID', '2024-04-14 10:00:00'),
(1, 9478.80, 7511.29, 1967.51, '2024-05-15', '2024-05-14', 'PAID', '2024-05-14 10:00:00'),
(1, 9478.80, 7564.55, 1914.25, '2024-06-15', '2024-06-14', 'PAID', '2024-06-14 10:00:00'),
(1, 9478.80, 7618.19, 1860.61, '2024-07-15', '2024-07-14', 'PAID', '2024-07-14 10:00:00'),
(1, 9478.80, 7672.22, 1806.58, '2024-08-15', '2024-08-14', 'PAID', '2024-08-14 10:00:00'),
(1, 9478.80, 7726.64, 1752.16, '2024-09-15', '2024-09-14', 'PAID', '2024-09-14 10:00:00'),
(1, 9478.80, 7781.46, 1697.34, '2024-10-15', '2024-10-14', 'PAID', '2024-10-14 10:00:00'),
(1, 9478.80, 7836.68, 1642.12, '2024-11-15', '2024-11-14', 'PAID', '2024-11-14 10:00:00'),
(1, 9478.80, 7892.30, 1586.50, '2024-12-15', '2024-12-14', 'PAID', '2024-12-14 10:00:00'),
(1, 9478.80, 7948.33, 1530.47, '2025-01-15', '2025-01-14', 'PAID', '2025-01-14 10:00:00'),
(1, 9478.80, 8004.77, 1474.03, '2025-02-15', '2025-02-14', 'PAID', '2025-02-14 10:00:00'),
(1, 9478.80, 8061.62, 1417.18, '2025-03-15', '2025-03-14', 'PAID', '2025-03-14 10:00:00'),
(1, 9478.80, 8118.89, 1359.91, '2025-04-15', '2025-04-14', 'PAID', '2025-04-14 10:00:00'),
(1, 9478.80, 8176.58, 1302.22, '2025-05-15', '2025-05-14', 'PAID', '2025-05-14 10:00:00'),
(1, 9478.80, 9478.80, 0.00,    '2025-06-15', NULL,          'PENDING', '2025-06-01 00:00:00'),
-- Loan 2 (MORTGAGE, 5000000 RSD, 4.5%, user=1): 36 payments paid
(2, 31650.00, 12900.00, 18750.00, '2023-07-01', '2023-06-30', 'PAID', '2023-06-30 10:00:00'),
(2, 31650.00, 12948.36, 18701.64, '2023-08-01', '2023-07-31', 'PAID', '2023-07-31 10:00:00'),
(2, 31650.00, 12996.90, 18653.10, '2023-09-01', '2023-08-31', 'PAID', '2023-08-31 10:00:00'),
(2, 31650.00, 31650.00, 0.00,     '2026-07-01', NULL,          'PENDING', '2026-06-01 00:00:00'),
-- Loan 3 (AUTO, 800000 RSD, 7.2%, user=2): 27 payments paid
(3, 15910.40, 11110.40, 4800.00, '2024-04-01', '2024-03-31', 'PAID', '2024-03-31 10:00:00'),
(3, 15910.40, 11177.13, 4733.27, '2024-05-01', '2024-04-30', 'PAID', '2024-04-30 10:00:00'),
(3, 15910.40, 11244.26, 4666.14, '2024-06-01', '2024-05-31', 'PAID', '2024-05-31 10:00:00'),
(3, 15910.40, 11311.81, 4598.59, '2024-07-01', '2024-06-30', 'PAID', '2024-06-30 10:00:00'),
(3, 15910.40, 11379.77, 4530.63, '2024-08-01', '2024-07-31', 'PAID', '2024-07-31 10:00:00'),
(3, 15910.40, 11448.16, 4462.24, '2024-09-01', '2024-08-31', 'PAID', '2024-08-31 10:00:00'),
(3, 15910.40, 11516.97, 4393.43, '2024-10-01', '2024-09-30', 'PAID', '2024-09-30 10:00:00'),
(3, 15910.40, 11586.21, 4324.19, '2024-11-01', '2024-10-31', 'PAID', '2024-10-31 10:00:00'),
(3, 15910.40, 11655.87, 4254.53, '2024-12-01', '2024-11-30', 'PAID', '2024-11-30 10:00:00'),
(3, 15910.40, 15910.40, 0.00,    '2026-07-01', NULL,          'PENDING', '2026-06-01 00:00:00'),
-- Loan 5 (PAID_OFF, PERSONAL, 150000 RSD, user=2): all 24 payments paid
(5, 6852.00, 5727.00, 1125.00, '2022-06-01', '2022-05-31', 'PAID', '2022-05-31 10:00:00'),
(5, 6852.00, 5770.03, 1081.97, '2022-07-01', '2022-06-30', 'PAID', '2022-06-30 10:00:00'),
(5, 6852.00, 5813.37, 1038.63, '2022-08-01', '2022-07-31', 'PAID', '2022-07-31 10:00:00'),
(5, 6852.00, 5856.99,  995.01, '2022-09-01', '2022-08-31', 'PAID', '2022-08-31 10:00:00'),
(5, 6852.00, 5900.91,  951.09, '2022-10-01', '2022-09-30', 'PAID', '2022-09-30 10:00:00'),
(5, 6852.00, 5945.13,  906.87, '2022-11-01', '2022-10-31', 'PAID', '2022-10-31 10:00:00'),
(5, 6852.00, 5989.64,  862.36, '2022-12-01', '2022-11-30', 'PAID', '2022-11-30 10:00:00'),
(5, 6852.00, 6034.46,  817.54, '2023-01-01', '2022-12-31', 'PAID', '2022-12-31 10:00:00'),
(5, 6852.00, 6079.58,  772.42, '2023-02-01', '2023-01-31', 'PAID', '2023-01-31 10:00:00'),
(5, 6852.00, 6125.00,  727.00, '2023-03-01', '2023-02-28', 'PAID', '2023-02-28 10:00:00'),
(5, 6852.00, 6170.74,  681.26, '2023-04-01', '2023-03-31', 'PAID', '2023-03-31 10:00:00'),
(5, 6852.00, 6216.78,  635.22, '2023-05-01', '2023-04-30', 'PAID', '2023-04-30 10:00:00'),
(5, 6852.00, 6263.14,  588.86, '2023-06-01', '2023-05-31', 'PAID', '2023-05-31 10:00:00'),
(5, 6852.00, 6309.82,  542.18, '2023-07-01', '2023-06-30', 'PAID', '2023-06-30 10:00:00'),
(5, 6852.00, 6356.82,  495.18, '2023-08-01', '2023-07-31', 'PAID', '2023-07-31 10:00:00'),
(5, 6852.00, 6404.14,  447.86, '2023-09-01', '2023-08-31', 'PAID', '2023-08-31 10:00:00'),
(5, 6852.00, 6451.78,  400.22, '2023-10-01', '2023-09-30', 'PAID', '2023-09-30 10:00:00'),
(5, 6852.00, 6499.75,  352.25, '2023-11-01', '2023-10-31', 'PAID', '2023-10-31 10:00:00'),
(5, 6852.00, 6548.05,  303.95, '2023-12-01', '2023-11-30', 'PAID', '2023-11-30 10:00:00'),
(5, 6852.00, 6596.68,  255.32, '2024-01-01', '2023-12-31', 'PAID', '2023-12-31 10:00:00'),
(5, 6852.00, 6645.65,  206.35, '2024-02-01', '2024-01-31', 'PAID', '2024-01-31 10:00:00'),
(5, 6852.00, 6694.95,  157.05, '2024-03-01', '2024-02-29', 'PAID', '2024-02-29 10:00:00'),
(5, 6852.00, 6744.59,  107.41, '2024-04-01', '2024-03-31', 'PAID', '2024-03-31 10:00:00'),
(5, 6852.00, 6794.58,   57.42, '2024-05-01', '2024-04-30', 'PAID', '2024-04-30 10:00:00'),
-- Loan 6 (PERSONAL, 120000 RSD, 8%, user=4): 5 payments paid
(6, 5440.00, 4640.00,  800.00, '2025-02-01', '2025-01-31', 'PAID', '2025-01-31 10:00:00'),
(6, 5440.00, 4671.00,  769.00, '2025-03-01', '2025-02-28', 'PAID', '2025-02-28 10:00:00'),
(6, 5440.00, 4702.21,  737.79, '2025-04-01', '2025-03-31', 'PAID', '2025-03-31 10:00:00'),
(6, 5440.00, 4733.64,  706.36, '2025-05-01', '2025-04-30', 'PAID', '2025-04-30 10:00:00'),
(6, 5440.00, 4765.28,  674.72, '2025-06-01', '2025-05-31', 'PAID', '2025-05-31 10:00:00'),
(6, 5440.00, 5440.00,    0.00, '2025-07-01', NULL,          'PENDING', '2025-06-01 00:00:00');

-- ─── notificationdb ──────────────────────────────────────────────────────────

\connect notificationdb

INSERT INTO notifications (user_id, type, channel, title, message, status, created_at, sent_at) VALUES
-- User 1 (John Doe)
(1, 'TRANSACTION', 'EMAIL', 'Deposit Successful',
 'A deposit of 500,000.00 RSD has been credited to account 160-0000000001-76.',
 'SENT', '2024-01-12 09:00:05', '2024-01-12 09:00:10'),
(1, 'LOAN', 'EMAIL', 'Loan Application Approved',
 'Your personal loan application for 300,000.00 RSD has been approved. First installment due: 2024-02-15.',
 'SENT', '2024-01-14 10:05:00', '2024-01-14 10:05:30'),
(1, 'TRANSACTION', 'SMS', 'Transfer Sent',
 'Transfer of 50,000.00 RSD sent to account 160-0000000003-91 on 2024-04-01.',
 'SENT', '2024-04-01 11:00:05', '2024-04-01 11:00:15'),
(1, 'LOAN', 'EMAIL', 'Monthly Installment Reminder',
 'Your personal loan installment of 9,478.80 RSD is due on 2025-06-15.',
 'SENT', '2025-06-08 08:00:00', '2025-06-08 08:00:45'),
(1, 'LOAN', 'EMAIL', 'Loan Application Rejected',
 'Your auto loan application for 500,000.00 RSD has been rejected. Reason: debt-to-income ratio exceeded.',
 'SENT', '2024-11-02 09:00:00', '2024-11-02 09:01:00'),
(1, 'TRANSACTION', 'EMAIL', 'Incoming Transfer',
 'Transfer of 100,000.00 RSD received from account 160-0000000005-15 on 2024-06-01.',
 'SENT', '2024-06-01 10:00:05', '2024-06-01 10:00:20'),
-- User 2 (Jane Smith)
(2, 'TRANSACTION', 'EMAIL', 'Deposit Successful',
 'A deposit of 200,000.00 RSD has been credited to account 160-0000000003-91.',
 'SENT', '2024-02-17 10:00:05', '2024-02-17 10:00:15'),
(2, 'LOAN', 'EMAIL', 'Loan Application Approved',
 'Your auto loan application for 800,000.00 RSD has been approved. First installment due: 2024-04-01.',
 'SENT', '2024-02-25 11:05:00', '2024-02-25 11:05:30'),
(2, 'LOAN', 'EMAIL', 'Loan Paid Off',
 'Congratulations! Your personal loan of 150,000.00 RSD has been fully repaid.',
 'SENT', '2024-04-30 10:05:00', '2024-04-30 10:05:20'),
(2, 'TRANSACTION', 'SMS', 'Withdrawal Alert',
 'ATM withdrawal of 80,000.00 RSD from account 160-0000000003-91 on 2024-04-10.',
 'SENT', '2024-04-10 16:00:05', '2024-04-10 16:00:20'),
(2, 'TRANSACTION', 'EMAIL', 'Incoming Transfer',
 'Transfer of 200,000.00 RSD received in account 160-0000000004-08 on 2024-06-10.',
 'SENT', '2024-06-10 12:00:05', '2024-06-10 12:00:25'),
(2, 'LOAN', 'EMAIL', 'Monthly Installment Reminder',
 'Your auto loan installment of 15,910.40 RSD is due on 2026-07-01.',
 'SENT', '2026-06-24 08:00:00', '2026-06-24 08:00:50'),
-- User 3 (Mark Johnson)
(3, 'TRANSACTION', 'EMAIL', 'Business Account Funded',
 'A deposit of 2,000,000.00 RSD has been credited to account 160-0000000005-15.',
 'SENT', '2024-03-22 11:00:05', '2024-03-22 11:00:20'),
(3, 'LOAN', 'EMAIL', 'Loan Application Received',
 'Your business loan application for 2,000,000.00 RSD is under review. Expected decision within 5 business days.',
 'SENT', '2026-06-01 09:05:00', '2026-06-01 09:05:30'),
(3, 'TRANSACTION', 'SMS', 'Large Withdrawal Alert',
 'Business expense withdrawal of 500,000.00 RSD from account 160-0000000005-15 on 2024-05-05.',
 'SENT', '2024-05-05 09:30:05', '2024-05-05 09:30:25'),
(3, 'TRANSACTION', 'EMAIL', 'Payroll Processed',
 'Business payroll withdrawal of 300,000.00 RSD from account 160-0000000005-15 on 2024-07-15.',
 'SENT', '2024-07-15 11:00:05', '2024-07-15 11:00:30'),
-- User 4 (Pera Perovic)
(4, 'TRANSACTION', 'EMAIL', 'Deposit Successful',
 'A deposit of 50,000.00 RSD has been credited to account 160-0000000007-30.',
 'SENT', '2024-04-07 08:00:05', '2024-04-07 08:00:15'),
(4, 'LOAN', 'EMAIL', 'Loan Application Approved',
 'Your personal loan application for 120,000.00 RSD has been approved. First installment due: 2025-02-01.',
 'SENT', '2024-12-20 09:05:00', '2024-12-20 09:05:30'),
(4, 'TRANSACTION', 'SMS', 'Transfer Sent',
 'Transfer of 20,000.00 RSD sent to account 160-0000000003-91 on 2024-05-20.',
 'SENT', '2024-05-20 09:00:05', '2024-05-20 09:00:20'),
-- User 5 (Ana Milic - SUSPENDED, failed notifications)
(5, 'ACCOUNT', 'EMAIL', 'Account Suspended',
 'Your account has been suspended due to suspicious activity. Please contact customer support.',
 'SENT', '2024-05-01 14:05:00', '2024-05-01 14:05:30'),
(5, 'TRANSACTION', 'EMAIL', 'Withdrawal Declined',
 'Your withdrawal request of 70,000.00 RSD was declined due to insufficient funds.',
 'FAILED', '2024-08-01 15:00:05', NULL);

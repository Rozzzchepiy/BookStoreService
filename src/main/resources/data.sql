INSERT INTO users (email, name, password) VALUES
('admin@admin', 'Admin', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6'),
('employee@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6'),
('client@client', 'Client', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6'),
('client2@example.com', 'user', 'password'),
('client3@example.com', 'user', 'password'),
('client4@example.com', 'user', 'password'),
('client5@example.com', 'user', 'password'),
('client6@example.com', 'user', 'password'),
('client7@example.com', 'user', 'password'),
('client8@example.com', 'user', 'password'),
('client9@example.com', 'user', 'password'),
('client10@example.com', 'user', 'password'),
('client11@example.com', 'user', 'password'),
('client12@example.com', 'user', 'password'),
('client13@example.com', 'user', 'password'),
('client14@example.com', 'user', 'password'),
('client15@example.com', 'user', 'password'),
('client16@example.com', 'user', 'password'),
('client17@example.com', 'user', 'password'),
('client18@example.com', 'user', 'password'),
('client19@example.com', 'user', 'password'),
('client20@example.com', 'user', 'password'),
('client21@example.com', 'user', 'password');


INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'), (2, 'EMPLOYEE'), (3, 'CLIENT'), (4, 'CLIENT'),
(5, 'CLIENT'),(6, 'CLIENT'), (7, 'CLIENT'), (8, 'CLIENT'),
(9, 'CLIENT'), (10, 'CLIENT'),(11, 'CLIENT'), (12, 'CLIENT'),
(13, 'CLIENT'), (14, 'CLIENT'),(15, 'CLIENT'), (16, 'CLIENT'),
(17, 'CLIENT'), (18, 'CLIENT'), (19, 'CLIENT'), (20, 'CLIENT'),
(21, 'CLIENT'), (22, 'CLIENT'),(22, 'CLIENT');

INSERT INTO employee_profiles (user_id, phone, birth_date) VALUES
(2, '09999999', '2000-01-01');

INSERT INTO client_profiles (user_id, balance) VALUES
(3, 1000.00),
(4, 1000.00),
(5, 1000.00),
(6, 1000.00),
(7, 1000.00),
(8, 1000.00),
(9, 1000.00),
(10, 1000.00),
(11, 1000.00),
(12, 1000.00),
(13, 1000.00),
(14, 1000.00),
(15, 1000.00),
(16, 1000.00),
(17, 1000.00),
(18, 1000.00),
(19, 1000.00),
(20, 1000.00),
(21, 1000.00),
(22, 1000.00);

INSERT INTO books (name, genre, age_group, price, publication_year, author, number_of_pages, characteristics, description, language)
VALUES
    ('The Hidden Treasure', 'Adventure', 'ADULT', 200, '2018-05-15', 'Emily White', 400, 'Mysterious journey','An enthralling adventure of discovery', 'ENGLISH'),
    ('Echoes of Eternity', 'Fantasy', 'TEEN', 200, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
    ('Whispers in the Shadows', 'Mystery', 'ADULT', 200, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense','A gripping mystery that keeps you guessing', 'ENGLISH'),
    ('The Starlight Sonata', 'Romance', 'ADULT', 200, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story','A beautiful journey of love and passion', 'ENGLISH'),
    ('Beyond the Horizon', 'Science Fiction', 'CHILD', 200, '2004-05-15', 'Alex Carter', 280,'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
    ('Dancing with Shadows', 'Thriller', 'ADULT', 1000, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists','A thrilling tale of danger and intrigue', 'ENGLISH'),
    ('Voices in the Wind', 'Historical Fiction', 'ADULT', 500, '2017-05-15', 'William Turner', 500,'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
    ('Serenade of Souls', 'Fantasy', 'TEEN', 2000, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms','A magical fantasy filled with wonder', 'ENGLISH'),
    ('Silent Whispers', 'Mystery', 'ADULT', 4000, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work','A mystery that keeps you on the edge', 'ENGLISH'),
    ('Whirlwind Romance', 'Romance', 'OTHER', 199, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair','A romance that sweeps you off your feet', 'ENGLISH');

INSERT INTO orders (client_id, employee_id, order_date, price, status)
VALUES (3, NULL, '2026-01-25 10:30:00', 450.00, 'NEW');


INSERT INTO orders (client_id, employee_id, order_date, price, status)
VALUES (3, 2, '2026-01-25 11:15:00', 820.50, 'ASSIGNED');


INSERT INTO orders (client_id, employee_id, order_date, price, status)
VALUES (3, 2, '2026-01-24 18:40:00', 1200.00, 'DELIVERED');

INSERT INTO users (email, name, password) VALUES
('john.doe@email.com', 'John Doe', 'pass123'),
('jane.smith@email.com', 'Jane Smith', 'abc456'),
('bob.jones@email.com', 'Bob Jones', 'qwerty789'),
('alice.white@email.com', 'Alice White', 'secret567'),
('mike.wilson@email.com', 'Mike Wilson', 'mypassword'),
('sara.brown@email.com', 'Sara Brown', 'letmein123'),
('tom.jenkins@email.com', 'Tom Jenkins', 'pass4321'),
('lisa.taylor@email.com', 'Lisa Taylor', 'securepwd'),
('david.wright@email.com', 'David Wright', 'access123'),
('emily.harris@email.com', 'Emily Harris', '1234abcd');

INSERT INTO users (email, name, password) VALUES
('client1@example.com', 'Medelyn Wright', 'password123'),
('client2@example.com', 'Landon Phillips', 'securepass'),
('client3@example.com', 'Harmony Mason', 'abc123'),
('client4@example.com', 'Archer Harper', 'pass456'),
('client5@example.com', 'Kira Jacobs', 'letmein789'),
('client6@example.com', 'Maximus Kelly', 'adminpass'),
('client7@example.com', 'Sierra Mitchell', 'mypassword'),
('client8@example.com', 'Quinton Saunders', 'test123'),
('client9@example.com', 'Amina Clarke', 'qwerty123'),
('client10@example.com', 'Bryson Chavez', 'pass789');

INSERT INTO user_roles (user_id, role) VALUES
(1, 'EMPLOYEE'), (2, 'EMPLOYEE'), (3, 'EMPLOYEE'), (4, 'EMPLOYEE'), (5, 'EMPLOYEE'),
(6, 'EMPLOYEE'), (7, 'EMPLOYEE'), (8, 'EMPLOYEE'), (9, 'EMPLOYEE'), (10, 'EMPLOYEE');

INSERT INTO user_roles (user_id, role) VALUES
(11, 'CLIENT'), (12, 'CLIENT'), (13, 'CLIENT'), (14, 'CLIENT'), (15, 'CLIENT'),
(16, 'CLIENT'), (17, 'CLIENT'), (18, 'CLIENT'), (19, 'CLIENT'), (20, 'CLIENT');

INSERT INTO employee_profiles (user_id, phone, birth_date) VALUES
(1, '555-123-4567', '1990-05-15'),
(2, '555-987-6543', '1985-09-20'),
(3, '555-321-6789', '1978-03-08'),
(4, '555-876-5432', '1982-11-25'),
(5, '555-234-5678', '1995-07-12'),
(6, '555-876-5433', '1989-01-30'),
(7, '555-345-6789', '1975-06-18'),
(8, '555-789-0123', '1987-12-04'),
(9, '555-456-7890', '1992-08-22'),
(10, '555-098-7654', '1980-04-10');

INSERT INTO client_profiles (user_id, balance) VALUES
(11, 1000.00),
(12, 1500.50),
(13, 800.75),
(14, 1200.25),
(15, 900.80),
(16, 1100.60),
(17, 1300.45),
(18, 950.30),
(19, 1050.90),
(20, 880.20);

INSERT INTO books (name, genre, age_group, price, publication_year, author, number_of_pages, characteristics, description, language)
VALUES
    ('The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey','An enthralling adventure of discovery', 'ENGLISH'),
    ('Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
    ('Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense','A gripping mystery that keeps you guessing', 'ENGLISH'),
    ('The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story','A beautiful journey of love and passion', 'ENGLISH'),
    ('Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280,'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
    ('Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists','A thrilling tale of danger and intrigue', 'ENGLISH'),
    ('Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500,'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
    ('Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms','A magical fantasy filled with wonder', 'ENGLISH'),
    ('Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work','A mystery that keeps you on the edge', 'ENGLISH'),
    ('Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair','A romance that sweeps you off your feet', 'ENGLISH');
INSERT INTO users (email, name, password, is_blocked, failed_attempt) VALUES
                                              ('admin@admin', 'Admin', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee2@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee3@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee4@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee5@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee6@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee7@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee8@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('employee9@employee', 'Employee', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client@client', 'Client One', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client2@client', 'Client two', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client3@client', 'Client three', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client4@client', 'Client four', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client5@client', 'Client five', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client6@client', 'Client six', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0),
                                              ('client7@client', 'Client seven', '$2a$10$22o4mPYT/cbB2DW7L2w6Ge6JLyo9fPlpZBRJO.NRIIaqHgMUc9ii6', false, 0);

INSERT INTO user_roles (user_id, role) VALUES
                                           (1, 'ADMIN'),
                                           (2, 'EMPLOYEE'),
                                           (3, 'EMPLOYEE'),
                                           (4, 'EMPLOYEE'),
                                           (5, 'EMPLOYEE'),
                                           (6, 'EMPLOYEE'),
                                           (7, 'EMPLOYEE'),
                                           (8, 'EMPLOYEE'),
                                           (9, 'EMPLOYEE'),
                                           (10, 'EMPLOYEE'),
                                           (11, 'CLIENT'),
                                           (12, 'CLIENT'),
                                           (13, 'CLIENT'),
                                           (14, 'CLIENT'),
                                           (15, 'CLIENT'),
                                           (16, 'CLIENT'),
                                           (17, 'CLIENT');

INSERT INTO employee_profiles (user_id, phone, birth_date) VALUES
    (2, '0991234567', '1995-05-20'),
    (3, '0991233567', '1995-05-20'),
    (4, '0991236567', '1995-05-20'),
    (5, '099146434567', '1995-05-20'),
    (6, '09912345767', '1995-05-20'),
    (7, '0991232217', '1995-05-20'),
    (8, '0991231247', '1995-05-20'),
    (9, '09912365757', '1995-05-20'),
    (10, '099123242347', '1995-05-20');

INSERT INTO client_profiles (user_id, balance) VALUES
                                                   (11, 5000.00),
                                                   (12, 5000.00),
                                                   (13, 5000.00),
                                                   (14, 5000.00),
                                                   (15, 5000.00),
                                                   (16, 5000.00),
                                                   (17, 2500.00);

INSERT INTO books (name, genre, age_group, price, publication_year, author, number_of_pages, characteristics, description, language) VALUES
('Harry Potter and the Sorcerers Stone', 'Fantasy', 'TEEN', 450.00, '1997-06-26', 'J.K. Rowling', 309, 'Magic, Wizards', 'The first book in the Harry Potter series.', 'ENGLISH'),
('The Great Gatsby', 'Historical Fiction', 'ADULT', 300.50, '1925-04-10', 'F. Scott Fitzgerald', 180, 'Classic, Jazz Age', 'A novel about the American dream.', 'ENGLISH'),
('1984', 'Science Fiction', 'ADULT', 280.00, '1949-06-08', 'George Orwell', 328, 'Dystopian, Politics', 'Big Brother is watching you.', 'ENGLISH'),
('The Hobbit', 'Fantasy', 'TEEN', 400.00, '1937-09-21', 'J.R.R. Tolkien', 310, 'Adventure, Dragons', 'A journey of Bilbo Baggins.', 'ENGLISH'),
('Murder on the Orient Express', 'Mystery', 'ADULT', 350.00, '1934-01-01', 'Agatha Christie', 256, 'Detective, Crime', 'Hercule Poirot solves a murder on a train.', 'ENGLISH'),
('Pride and Prejudice', 'Romance', 'ADULT', 250.00, '1813-01-28', 'Jane Austen', 279, 'Classic, Love', 'A romantic novel of manners.', 'ENGLISH'),
('Dune', 'Science Fiction', 'ADULT', 600.00, '1965-08-01', 'Frank Herbert', 412, 'Space, Politics', 'Epic science fiction set on Arrakis.', 'ENGLISH'),
('The Catcher in the Rye', 'Fiction', 'TEEN', 290.00, '1951-07-16', 'J.D. Salinger', 277, 'Angst, Coming-of-age', 'The story of Holden Caulfield.', 'ENGLISH'),
('To Kill a Mockingbird', 'Historical Fiction', 'ADULT', 320.00, '1960-07-11', 'Harper Lee', 281, 'Justice, South', 'A lawyer defends a black man.', 'ENGLISH'),
('It', 'Thriller', 'ADULT', 550.00, '1986-09-15', 'Stephen King', 1138, 'Horror, Clowns', 'A group of kids fight a monster.', 'ENGLISH'),

('Кобзар', 'Poetry', 'ADULT', 200.00, '1840-01-01', 'Тарас Шевченко', 400, 'Classic, Ukraine', 'Збірка поетичних творів.', 'UKRAINIAN'),
('Тіні забутих предків', 'Historical Fiction', 'ADULT', 150.00, '1911-01-01', 'Михайло Коцюбинський', 120, 'History, Love', 'Повість про кохання Івана та Марічки.', 'UKRAINIAN'),
('Захар Беркут', 'Historical Fiction', 'TEEN', 180.00, '1883-01-01', 'Іван Франко', 250, 'History, Heroism', 'Історична повість про боротьбу з монголами.', 'UKRAINIAN'),
('Лісова пісня', 'Fantasy', 'TEEN', 140.00, '1911-01-01', 'Леся Українка', 160, 'Drama, Mythology', 'Драма-феєрія.', 'UKRAINIAN'),
('Кайдашева сім’я', 'Comedy', 'ADULT', 170.00, '1878-01-01', 'Іван Нечуй-Левицький', 220, 'Realism, Family', 'Соціально-побутова повість.', 'UKRAINIAN'),
('Солодка Даруся', 'Drama', 'ADULT', 220.00, '2004-01-01', 'Марія Матіос', 280, 'History, Tragedy', 'Драма про долю дівчини на Буковині.', 'UKRAINIAN'),
('Ворошиловград', 'Fiction', 'ADULT', 300.00, '2010-01-01', 'Сергій Жадан', 350, 'Modern, Road-movie', 'Роман про повернення додому.', 'UKRAINIAN'),
('Тореадори з Васюківки', 'Adventure', 'CHILD', 190.00, '1973-01-01', 'Всеволод Нестайко', 400, 'Fun, Childhood', 'Пригоди двох друзів.', 'UKRAINIAN'),
('Інтернат', 'Drama', 'ADULT', 310.00, '2017-01-01', 'Сергій Жадан', 320, 'War, Survival', 'Подорож крізь зону конфлікту.', 'UKRAINIAN'),
('Століття Якова', 'Historical Fiction', 'ADULT', 240.00, '2010-01-01', 'Володимир Лис', 280, 'History, Life', 'Історія довгого життя волинянина.', 'UKRAINIAN'),

('Faust', 'Drama', 'ADULT', 400.00, '1808-01-01', 'Johann Wolfgang von Goethe', 500, 'Philosophy, Classic', 'A pact with the devil.', 'GERMAN'),
('Der Prozess', 'Mystery', 'ADULT', 350.00, '1925-01-01', 'Franz Kafka', 200, 'Absurd, Law', 'A man arrested for an unknown crime.', 'GERMAN'),
('Die Verwandlung', 'Fiction', 'ADULT', 150.00, '1915-01-01', 'Franz Kafka', 100, 'Surrealism', 'A man transforms into an insect.', 'GERMAN'),
('Im Westen nichts Neues', 'Historical Fiction', 'ADULT', 320.00, '1929-01-01', 'Erich Maria Remarque', 250, 'War, Anti-war', 'A story about WWI.', 'GERMAN'),
('Das Parfum', 'Thriller', 'ADULT', 380.00, '1985-01-01', 'Patrick Süskind', 300, 'Crime, Scent', 'Story of a murderer.', 'GERMAN');


INSERT INTO orders (client_id, employee_id, order_date, price, status) VALUES
                                                                           (11, NULL, '2023-10-01 10:00:00', 450.00, 'NEW'),
                                                                           (11, NULL, '2023-10-02 11:30:00', 600.00, 'NEW'),
                                                                           (11, NULL, '2023-10-05 09:15:00', 280.00, 'NEW'),
                                                                           (11, NULL, '2023-10-20 14:00:00', 1200.00, 'NEW'),
                                                                           (11, NULL, NOW(), 550.00, 'NEW');

INSERT INTO orders (client_id, employee_id, order_date, price, status) VALUES
                                                                           (11, 2, '2023-09-15 10:00:00', 300.50, 'ASSIGNED'),
                                                                           (11, 2, '2023-09-18 16:45:00', 820.00, 'ASSIGNED'),
                                                                           (11, 2, '2023-09-20 12:20:00', 150.00, 'ASSIGNED');

INSERT INTO orders (client_id, employee_id, order_date, price, status) VALUES
                                                                           (11, 2, '2023-08-01 10:00:00', 250.00, 'DELIVERED'),
                                                                           (11, 2, '2023-08-05 11:00:00', 400.00, 'DELIVERED'),
                                                                           (11, 2, '2023-08-10 14:30:00', 1000.00, 'DELIVERED'),
                                                                           (11, 2, '2023-08-15 09:00:00', 190.00, 'DELIVERED'),
                                                                           (11, 2, '2023-08-20 18:00:00', 320.00, 'DELIVERED');

INSERT INTO orders (client_id, employee_id, order_date, price, status) VALUES
                                                                           (11, NULL, '2023-09-01 10:00:00', 200.00, 'CANCELLED'),
                                                                           (11, 2, '2023-09-05 15:30:00', 600.00, 'CANCELLED');

INSERT INTO orders (client_id, employee_id, order_date, price, status) VALUES
                                                                           (11, NULL, '2023-10-10 12:00:00', 1500.00, 'NEW'),
                                                                           (11, 2, '2023-10-12 14:00:00', 400.00, 'ASSIGNED'),
                                                                           (11, 2, '2023-08-25 09:00:00', 350.00, 'DELIVERED');



INSERT INTO book_items (order_id, book_id, quantity) VALUES

(1, 1, 1),

(2, 3, 2),

(3, 11, 1),
(3, 14, 1),

(4, 7, 2),

(5, 25, 1),

(6, 2, 1),
(6, 4, 1),

(7, 10, 1),

(8, 12, 1),

(9, 6, 1),

(10, 1, 1),

(11, 20, 4),

(12, 18, 1),

(13, 24, 1),

(14, 11, 1),

(15, 9, 2),


(16, 21, 3),

(17, 5, 1),

(18, 16, 1);
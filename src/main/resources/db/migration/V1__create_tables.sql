-- 著者テーブル
CREATE TABLE authors (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         birth_date DATE NOT NULL
);

-- 書籍テーブル
CREATE TABLE books (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       price INT NOT NULL CHECK (price >= 0),
                       publication_status VARCHAR(50) NOT NULL DEFAULT 'UNPUBLISHED'
                           CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED'))
);

-- 書籍・著者 中間テーブル
CREATE TABLE book_authors (
                              book_id INT NOT NULL,
                              author_id INT NOT NULL,
                              PRIMARY KEY (book_id, author_id),
                              FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                              FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

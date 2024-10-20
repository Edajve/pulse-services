-- Drop Rating table if exists
DROP TABLE IF EXISTS rating;

-- Create Qr table
CREATE TABLE rating
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment VARCHAR(255) NOT NULL,
    rating  BIGINT       NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES account (id)
);
-- hey chat gpt, i have a two additional columns i added in my code and the las part is to update the columns in the db, how can i do that withouth losing the existing data in the db

-- the two columns are security_question and security_answer both varchar(255)

-- Drop Account table if exists
DROP TABLE IF EXISTS account;

-- Create Account table
CREATE TABLE account (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(255) NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         email VARCHAR(255),
                         first_name VARCHAR(255) NOT NULL,
                         last_name VARCHAR(255) NOT NULL,
                         enabled BOOLEAN NOT NULL,
                         account_non_expired BOOLEAN NOT NULL,
                         account_non_locked BOOLEAN NOT NULL,
                         credentials_non_expired BOOLEAN NOT NULL,
                         created_date TIMESTAMP NOT NULL,
                         last_login_date TIMESTAMP,
                         security_question VARCHAR(255),
                         security_answer VARCHAR(255)
);

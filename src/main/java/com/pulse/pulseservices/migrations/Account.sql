-- Create Account table
CREATE TABLE account (
    -- Unique identifier for each account, auto-incremented
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Username for the account, cannot be null
                         username VARCHAR(255) NOT NULL,

    -- Password for the account, cannot be null
                         password VARCHAR(255) NOT NULL,

    -- Email address of the account (optional)
                         email VARCHAR(255),

    -- First name of the account holder, cannot be null
                         first_name VARCHAR(255) NOT NULL,

    -- Last name of the account holder, cannot be null
                         last_name VARCHAR(255) NOT NULL,

    -- Flag indicating whether the account is enabled (true/false), cannot be null
                         enabled BOOLEAN NOT NULL,

    -- Flag indicating whether the account is non-expired (true/false), cannot be null
                         account_non_expired BOOLEAN NOT NULL,

    -- Flag indicating whether the account is non-locked (true/false), cannot be null
                         account_non_locked BOOLEAN NOT NULL,

    -- Flag indicating whether the credentials are non-expired (true/false), cannot be null
                         credentials_non_expired BOOLEAN NOT NULL,

    -- Date and time when the account was created, cannot be null
                         created_date TIMESTAMP NOT NULL,

    -- Date and time of the last login (optional)
                         last_login_date TIMESTAMP
);

-- Drop Account table if exists
DROP TABLE IF EXISTS account;

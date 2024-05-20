-- Drop Qr table if exists
DROP TABLE IF EXISTS qr;

-- Create Qr table
CREATE TABLE qr
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_bytes  LONGBLOB NOT NULL,
    is_qr_active BOOLEAN  NOT NULL,
    user_id      BIGINT,
    FOREIGN KEY (user_id) REFERENCES account (id)
);

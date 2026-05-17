-- Create databases
CREATE DATABASE IF NOT EXISTS parkease_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_parking_lots CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_spots CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_bookings CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_payments CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_vehicles CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_notifications CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS parkease_analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges
GRANT ALL PRIVILEGES ON parkease_auth.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_parking_lots.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_spots.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_bookings.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_payments.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_vehicles.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_notifications.* TO 'parkease_user'@'%';
GRANT ALL PRIVILEGES ON parkease_analytics.* TO 'parkease_user'@'%';

FLUSH PRIVILEGES;

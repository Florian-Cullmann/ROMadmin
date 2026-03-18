-- AlterTable: add client_timestamp to save_files
ALTER TABLE `save_files` ADD COLUMN `client_timestamp` DATETIME(3) NULL;

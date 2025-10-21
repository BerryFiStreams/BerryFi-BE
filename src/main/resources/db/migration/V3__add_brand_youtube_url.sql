-- Migration: Add brand_youtube_url column to projects table
-- Date: 2025-10-21
-- Description: Add YouTube URL field for project branding

ALTER TABLE projects ADD COLUMN brand_youtube_url VARCHAR(500);

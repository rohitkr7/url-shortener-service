CREATE SEQUENCE IF NOT EXISTS url_id_seq START WITH 1000000 INCREMENT BY 1;

CREATE TABLE urls (
    id BIGINT PRIMARY KEY DEFAULT nextval('url_id_seq'),
    original_url TEXT NOT NULL,
    short_code VARCHAR(16) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE click_events (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL,
    clicked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    referrer TEXT,
    user_agent TEXT,
    ip_hash VARCHAR(64),
    CONSTRAINT fk_url_clicks FOREIGN KEY (short_code) REFERENCES urls(short_code) ON DELETE CASCADE
);

CREATE INDEX idx_urls_short_code ON urls(short_code);
CREATE INDEX idx_click_events_analytics ON click_events(short_code, clicked_at DESC);
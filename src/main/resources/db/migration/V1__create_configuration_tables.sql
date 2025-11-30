-- Create configurations table
CREATE TABLE IF NOT EXISTS configurations (
                                              id UUID PRIMARY KEY,
                                              name VARCHAR(100) NOT NULL,
                                              application VARCHAR(100) NOT NULL,
                                              environment VARCHAR(50) NOT NULL,
                                              version INT NOT NULL DEFAULT 1,
                                              content TEXT NOT NULL,
                                              created_at TIMESTAMPTZ DEFAULT NOW(),
                                              updated_at TIMESTAMPTZ DEFAULT NOW()
);
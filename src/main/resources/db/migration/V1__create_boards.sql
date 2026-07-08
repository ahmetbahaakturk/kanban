CREATE TABLE boards (
    public_id VARCHAR(50) PRIMARY KEY ,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);

CREATE TABLE task_lists(
    id BIGSERIAL PRIMARY KEY,
    board_public_id varchar(50) NOT NULL,
    type varchar(20) NOT NULL,
    CONSTRAINT fk_task_lists_board FOREIGN KEY (board_public_id) REFERENCES boards (public_id) ON DELETE CASCADE,
    CONSTRAINT chk_task_lists_type CHECK (type IN ('BACKLOG', 'TO_DO', 'IN_PROGRESS', 'DONE'))
);

CREATE INDEX idx_task_lists_board_public_id ON task_lists (board_public_id);

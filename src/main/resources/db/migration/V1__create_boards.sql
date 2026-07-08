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

CREATE TABLE cards(
    id BIGSERIAL PRIMARY KEY,
    title varchar(150) NOT NULL,
    text text,
    color_code varchar(7) NOT NULL
);

CREATE TABLE task_list_cards(
    id BIGSERIAL PRIMARY KEY,
    task_list_id bigint NOT NULL,
    card_id bigint NOT NULL,
    position integer NOT NULL,
    CONSTRAINT fk_task_list_cards_task_list FOREIGN KEY (task_list_id) REFERENCES task_lists (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_list_cards_card FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE,
    CONSTRAINT uq_task_list_cards_card UNIQUE (card_id),
    CONSTRAINT uq_task_list_cards_task_list_position UNIQUE (task_list_id, position)
);

CREATE INDEX idx_task_list_cards_task_list_id ON task_list_cards (task_list_id);

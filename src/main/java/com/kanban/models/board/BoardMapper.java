package com.kanban.models.board;

import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BoardMapper {

    public Board toBoard(BoardCreateRequest request) {
        return Board.builder()
                .publicId(request.publicId())
                .createdDate(Instant.now())
                .build();
    }

    public BoardResponse toBoardResponse(Board board) {
        return new BoardResponse(
                board.getPublicId(),
                board.getCreatedDate()
        );
    }
}

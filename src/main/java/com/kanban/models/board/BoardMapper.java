package com.kanban.models.board;

import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.tasklist.dto.TaskListResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class BoardMapper {

    public Board toBoard(BoardCreateRequest request) {
        return Board.builder()
                .publicId(request.publicIdValue())
                .createdDate(Instant.now())
                .build();
    }

    public BoardResponse toBoardResponse(Board board) {
        return new BoardResponse(
                board.getPublicId(),
                board.getCreatedDate()
        );
    }

    public BoardDetailResponse toBoardDetailResponse(Board board, List<TaskListResponse> taskLists) {
        return new BoardDetailResponse(
                board.getPublicId(),
                board.getCreatedDate(),
                taskLists
        );
    }
}

package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.tasklist.dto.TaskListResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class BoardMapperTests {
    private final BoardMapper mapper = new BoardMapper();

    @Test
    void toBoardMapsCreateRequestToBoard() {
        Board board = mapper.toBoard(new BoardCreateRequest("board-1"));

        assertThat(board.getPublicId()).isEqualTo("board-1");
        assertThat(board.getCreatedDate()).isNotNull();
    }

    @Test
    void toBoardResponseMapsBoardToResponse() {
        Instant createdDate = Instant.parse("2026-07-09T13:00:00Z");
        Board board = Board.builder()
                .publicId("board-1")
                .createdDate(createdDate)
                .build();

        BoardResponse response = mapper.toBoardResponse(board);

        assertThat(response.publicId()).isEqualTo("board-1");
        assertThat(response.createdAt()).isEqualTo(createdDate);
    }

    @Test
    void toBoardDetailResponseMapsBoardAndTaskListsToResponse() {
        Instant createdDate = Instant.parse("2026-07-09T13:00:00Z");
        Board board = Board.builder()
                .publicId("board-1")
                .createdDate(createdDate)
                .build();
        List<TaskListResponse> taskLists = List.of();

        BoardDetailResponse response = mapper.toBoardDetailResponse(board, taskLists);

        assertThat(response.publicId()).isEqualTo("board-1");
        assertThat(response.createdAt()).isEqualTo(createdDate);
        assertThat(response.taskLists()).isSameAs(taskLists);
    }
}

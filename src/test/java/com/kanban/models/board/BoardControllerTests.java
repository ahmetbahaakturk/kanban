package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class BoardControllerTests {
    private final BoardService boardService = mock(BoardService.class);
    private final BoardController controller = new BoardController(boardService);

    @Test
    void createBoardDelegatesToService() {
        BoardCreateRequest request = new BoardCreateRequest("board-1");
        BoardResponse response = new BoardResponse("board-1", Instant.parse("2026-07-10T12:00:00Z"));

        when(boardService.createBoard(request)).thenReturn(response);

        BoardResponse result = controller.createBoard(request);

        assertThat(result).isEqualTo(response);
        verify(boardService).createBoard(request);
    }

    @Test
    void getBoardDetailDelegatesToService() {
        BoardDetailResponse response = new BoardDetailResponse(
                "board-1",
                Instant.parse("2026-07-10T12:00:00Z"),
                List.of()
        );

        when(boardService.getBoardDetail("board-1")).thenReturn(response);

        BoardDetailResponse result = controller.getBoardDetail("board-1");

        assertThat(result).isEqualTo(response);
        verify(boardService).getBoardDetail("board-1");
    }
}

package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.board.dto.PublicId;
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
    void isAvailableDelegatesToService() {
        PublicId publicId = new PublicId("board-1");

        when(boardService.isAvailable(publicId)).thenReturn(true);

        Boolean result = controller.isAvailable(publicId);

        assertThat(result).isTrue();
        verify(boardService).isAvailable(publicId);
    }

    @Test
    void getBoardDetailDelegatesToService() {
        PublicId publicId = new PublicId("board-1");
        BoardDetailResponse response = new BoardDetailResponse(
                "board-1",
                Instant.parse("2026-07-10T12:00:00Z"),
                List.of()
        );

        when(boardService.getBoardDetail(publicId)).thenReturn(response);

        BoardDetailResponse result = controller.getBoardDetail(publicId);

        assertThat(result).isEqualTo(response);
        verify(boardService).getBoardDetail(publicId);
    }
}

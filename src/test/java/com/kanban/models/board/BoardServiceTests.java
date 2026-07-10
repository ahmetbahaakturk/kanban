package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.exceptions.AlreadyExistsException;
import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.tasklist.TaskListService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

class BoardServiceTests {

    private final BoardRepository repository = mock(BoardRepository.class);
    private final BoardMapper boardMapper = mock(BoardMapper.class);
    private final TaskListService taskListService = mock(TaskListService.class);
    private final BoardService service = new BoardService(repository, boardMapper, taskListService);

    @Test
    void createBoardCreatesDefaultTaskListsAndReturnsResponse() {
        BoardCreateRequest request = new BoardCreateRequest("board-1");
        Board boardToSave = Board.builder()
                .publicId("board-1")
                .createdDate(Instant.parse("2026-07-09T13:00:00Z"))
                .build();
        BoardResponse response = new BoardResponse("board-1", boardToSave.getCreatedDate());

        when(repository.existsById("board-1")).thenReturn(false);
        when(boardMapper.toBoard(request)).thenReturn(boardToSave);
        when(repository.save(boardToSave)).thenReturn(boardToSave);
        when(boardMapper.toBoardResponse(boardToSave)).thenReturn(response);

        BoardResponse result = service.createBoard(request);

        verify(repository).save(boardToSave);
        verify(taskListService).createTaskLists(boardToSave);
        verify(boardMapper).toBoardResponse(boardToSave);
        org.assertj.core.api.Assertions.assertThat(result).isEqualTo(response);
    }

    @Test
    void createBoardThrowsAlreadyExistsWhenPublicIdExists() {
        BoardCreateRequest request = new BoardCreateRequest("board-1");

        when(repository.existsById("board-1")).thenReturn(true);

        assertThatThrownBy(() -> service.createBoard(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Board already exists with publicId: board-1");

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(taskListService, never()).createTaskLists(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createBoardConvertsDataIntegrityViolationToAlreadyExists() {
        BoardCreateRequest request = new BoardCreateRequest("board-1");
        Board boardToSave = Board.builder()
                .publicId("board-1")
                .createdDate(Instant.parse("2026-07-09T13:00:00Z"))
                .build();

        when(repository.existsById("board-1")).thenReturn(false);
        when(boardMapper.toBoard(request)).thenReturn(boardToSave);
        when(repository.save(boardToSave)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.createBoard(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Board already exists with publicId: board-1");

        verify(taskListService, never()).createTaskLists(org.mockito.ArgumentMatchers.any());
    }
}

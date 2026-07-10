package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.exceptions.AlreadyExistsException;
import com.kanban.exceptions.NotFoundException;
import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.board.dto.PublicId;
import com.kanban.models.card.Card;
import com.kanban.models.card.CardRepository;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListMapper;
import com.kanban.models.tasklist.TaskListRepository;
import com.kanban.models.tasklist.TaskListService;
import com.kanban.models.tasklist.TaskListType;
import com.kanban.models.tasklist.dto.TaskListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class BoardServiceTests {

    private final BoardRepository repository = mock(BoardRepository.class);
    private final BoardMapper boardMapper = mock(BoardMapper.class);
    private final TaskListService taskListService = mock(TaskListService.class);
    private final TaskListRepository taskListRepository = mock(TaskListRepository.class);
    private final CardRepository cardRepository = mock(CardRepository.class);
    private final TaskListMapper taskListMapper = mock(TaskListMapper.class);
    private final BoardService service = new BoardService(
            repository,
            boardMapper,
            taskListService,
            taskListRepository,
            cardRepository,
            taskListMapper
    );

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

    @Test
    void getBoardDetailReturnsBoardWithTaskListsAndCards() {
        Board board = Board.builder()
                .publicId("board-1")
                .createdDate(Instant.parse("2026-07-09T13:00:00Z"))
                .build();
        TaskList backlog = TaskList.builder()
                .id(1L)
                .board(board)
                .type(TaskListType.BACKLOG)
                .build();
        TaskList done = TaskList.builder()
                .id(2L)
                .board(board)
                .type(TaskListType.DONE)
                .build();
        Card card = new Card();
        card.setId(10L);
        card.setTaskList(backlog);
        card.setPosition(1);
        TaskListResponse backlogResponse = new TaskListResponse(1L, TaskListType.BACKLOG, List.of());
        TaskListResponse doneResponse = new TaskListResponse(2L, TaskListType.DONE, List.of());
        BoardDetailResponse response = new BoardDetailResponse(
                "board-1",
                board.getCreatedDate(),
                List.of(backlogResponse, doneResponse)
        );

        when(repository.findById("board-1")).thenReturn(Optional.of(board));
        when(taskListRepository.findAllByBoard_PublicIdOrderByIdAsc("board-1")).thenReturn(List.of(backlog, done));
        when(cardRepository.findAllByTaskListInOrderByPositionAsc(List.of(backlog, done))).thenReturn(List.of(card));
        when(taskListMapper.toTaskListResponse(backlog, List.of(card))).thenReturn(backlogResponse);
        when(taskListMapper.toTaskListResponse(done, List.of())).thenReturn(doneResponse);
        when(boardMapper.toBoardDetailResponse(board, List.of(backlogResponse, doneResponse))).thenReturn(response);

        BoardDetailResponse result = service.getBoardDetail(new PublicId("board-1"));

        assertThat(result).isEqualTo(response);
        verify(cardRepository).findAllByTaskListInOrderByPositionAsc(List.of(backlog, done));
        verify(boardMapper).toBoardDetailResponse(board, List.of(backlogResponse, doneResponse));
    }

    @Test
    void getBoardDetailThrowsNotFoundWhenBoardDoesNotExist() {
        when(repository.findById("missing-board")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBoardDetail(new PublicId("missing-board")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Board not found with publicId: missing-board");

        verify(taskListRepository, never()).findAllByBoard_PublicIdOrderByIdAsc(org.mockito.ArgumentMatchers.any());
        verify(cardRepository, never()).findAllByTaskListInOrderByPositionAsc(org.mockito.ArgumentMatchers.any());
    }
}

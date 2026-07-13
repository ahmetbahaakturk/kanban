package com.kanban.models.tasklist;

import com.kanban.exceptions.BadRequestException;
import com.kanban.exceptions.NotFoundException;
import com.kanban.models.board.Board;
import com.kanban.models.card.Card;
import com.kanban.models.card.CardRepository;
import com.kanban.models.tasklist.dto.TaskListOrderRequest;
import com.kanban.models.tasklist.dto.TaskListsOrderRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskListServiceTests {
    private final TaskListRepository taskListRepository = mock(TaskListRepository.class);
    private final CardRepository cardRepository = mock(CardRepository.class);
    private final TaskListService service = new TaskListService(taskListRepository, cardRepository);

    @Test
    void updateCardOrderReordersEveryCardInTaskList() {
        Board board = board("board-1");
        TaskList taskList = taskList(1L, board, TaskListType.TO_DO);
        Card firstCard = card(10L, taskList, 1);
        Card secondCard = card(11L, taskList, 2);
        Card thirdCard = card(12L, taskList, 3);
        TaskListsOrderRequest request = request(
                new TaskListOrderRequest(1L, List.of(12L, 10L, 11L))
        );

        when(taskListRepository.findAllById(List.of(1L))).thenReturn(List.of(taskList));
        when(cardRepository.findAllByTaskListInOrderByPositionAsc(List.of(taskList)))
                .thenReturn(List.of(firstCard, secondCard, thirdCard));

        service.updateCardOrder(request);

        assertThat(thirdCard.getPosition()).isEqualTo(1);
        assertThat(firstCard.getPosition()).isEqualTo(2);
        assertThat(secondCard.getPosition()).isEqualTo(3);
        verify(cardRepository, times(2)).saveAllAndFlush(anyList());
    }

    @Test
    void updateCardOrderMovesCardBetweenTaskLists() {
        Board board = board("board-1");
        TaskList sourceTaskList = taskList(1L, board, TaskListType.TO_DO);
        TaskList targetTaskList = taskList(2L, board, TaskListType.IN_PROGRESS);
        Card movedCard = card(10L, sourceTaskList, 1);
        Card sourceCard = card(11L, sourceTaskList, 2);
        Card targetCard = card(20L, targetTaskList, 1);
        TaskListsOrderRequest request = request(
                new TaskListOrderRequest(1L, List.of(11L)),
                new TaskListOrderRequest(2L, List.of(20L, 10L))
        );

        when(taskListRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(sourceTaskList, targetTaskList));
        when(cardRepository.findAllByTaskListInOrderByPositionAsc(
                List.of(sourceTaskList, targetTaskList)
        )).thenReturn(List.of(movedCard, targetCard, sourceCard));

        service.updateCardOrder(request);

        assertThat(sourceCard.getTaskList()).isSameAs(sourceTaskList);
        assertThat(sourceCard.getPosition()).isEqualTo(1);
        assertThat(targetCard.getTaskList()).isSameAs(targetTaskList);
        assertThat(targetCard.getPosition()).isEqualTo(1);
        assertThat(movedCard.getTaskList()).isSameAs(targetTaskList);
        assertThat(movedCard.getPosition()).isEqualTo(2);
        verify(cardRepository, times(2)).saveAllAndFlush(anyList());
    }

    @Test
    void updateCardOrderRejectsMissingTaskList() {
        TaskListsOrderRequest request = request(
                new TaskListOrderRequest(99L, List.of())
        );

        when(taskListRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.updateCardOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task list not found with id: 99");

        verify(cardRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void updateCardOrderRejectsIncompleteCardList() {
        Board board = board("board-1");
        TaskList taskList = taskList(1L, board, TaskListType.TO_DO);
        Card firstCard = card(10L, taskList, 1);
        Card omittedCard = card(11L, taskList, 2);
        TaskListsOrderRequest request = request(
                new TaskListOrderRequest(1L, List.of(10L))
        );

        when(taskListRepository.findAllById(List.of(1L))).thenReturn(List.of(taskList));
        when(cardRepository.findAllByTaskListInOrderByPositionAsc(List.of(taskList)))
                .thenReturn(List.of(firstCard, omittedCard));

        assertThatThrownBy(() -> service.updateCardOrder(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("cardIds must contain every card from the requested task lists exactly once");

        verify(cardRepository, never()).saveAllAndFlush(anyList());
    }

    private TaskListsOrderRequest request(TaskListOrderRequest... taskLists) {
        return new TaskListsOrderRequest(List.of(taskLists));
    }

    private Board board(String publicId) {
        return Board.builder()
                .publicId(publicId)
                .build();
    }

    private TaskList taskList(Long id, Board board, TaskListType type) {
        return TaskList.builder()
                .id(id)
                .board(board)
                .type(type)
                .build();
    }

    private Card card(Long id, TaskList taskList, Integer position) {
        Card card = new Card();
        card.setId(id);
        card.setTaskList(taskList);
        card.setPosition(position);
        return card;
    }
}

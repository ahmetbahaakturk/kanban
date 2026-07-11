package com.kanban.models.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.exceptions.BadRequestException;
import com.kanban.exceptions.NotFoundException;
import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardMoveRequest;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListRepository;
import com.kanban.models.tasklist.TaskListType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class CardServiceTests {
    private final CardRepository cardRepository = mock(CardRepository.class);
    private final TaskListRepository taskListRepository = mock(TaskListRepository.class);
    private final CardMapper cardMapper = mock(CardMapper.class);
    private final CardColorProvider cardColorProvider = mock(CardColorProvider.class);
    private final CardService service = new CardService(
            cardRepository,
            taskListRepository,
            cardMapper,
            cardColorProvider
    );

    @Test
    void createCardAssignsRandomDefaultColorAndNextPosition() {
        CardCreateRequest request = new CardCreateRequest(1L, "Card title", "Card text");
        TaskList taskList = TaskList.builder()
                .id(1L)
                .type(TaskListType.TO_DO)
                .build();
        Card cardToSave = new Card();
        Card savedCard = new Card();
        CardResponse response = new CardResponse(10L, "Card title", "Card text", "#327edc", 4);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(taskList));
        when(cardRepository.findMaxPositionByTaskListId(1L)).thenReturn(Optional.of(3));
        when(cardColorProvider.randomColorCode()).thenReturn("#327edc");
        when(cardMapper.toCard(request, taskList, "#327edc", 4)).thenReturn(cardToSave);
        when(cardRepository.save(cardToSave)).thenReturn(savedCard);
        when(cardMapper.toCardResponse(savedCard)).thenReturn(response);

        CardResponse result = service.createCard(request);

        assertThat(result).isEqualTo(response);
        verify(cardRepository).save(cardToSave);
        verify(cardMapper).toCard(request, taskList, "#327edc", 4);
    }

    @Test
    void createCardStartsPositionFromOneWhenTaskListHasNoCards() {
        CardCreateRequest request = new CardCreateRequest(1L, "Card title", null);
        TaskList taskList = TaskList.builder()
                .id(1L)
                .type(TaskListType.BACKLOG)
                .build();
        Card cardToSave = new Card();
        Card savedCard = new Card();
        CardResponse response = new CardResponse(10L, "Card title", null, "#0cae96", 1);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(taskList));
        when(cardRepository.findMaxPositionByTaskListId(1L)).thenReturn(Optional.empty());
        when(cardColorProvider.randomColorCode()).thenReturn("#0cae96");
        when(cardMapper.toCard(request, taskList, "#0cae96", 1)).thenReturn(cardToSave);
        when(cardRepository.save(cardToSave)).thenReturn(savedCard);
        when(cardMapper.toCardResponse(savedCard)).thenReturn(response);

        CardResponse result = service.createCard(request);

        assertThat(result).isEqualTo(response);
        verify(cardMapper).toCard(request, taskList, "#0cae96", 1);
    }

    @Test
    void createCardThrowsNotFoundWhenTaskListDoesNotExist() {
        CardCreateRequest request = new CardCreateRequest(99L, "Card title", "Card text");

        when(taskListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCard(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task list not found with id: 99");

        verify(cardRepository, never()).save(any());
        verify(cardColorProvider, never()).randomColorCode();
    }

    @Test
    void moveCardReordersCardsWithinSameTaskList() {
        TaskList taskList = taskList(1L, TaskListType.TO_DO);
        Card firstCard = card(10L, taskList, 1);
        Card secondCard = card(11L, taskList, 2);
        Card thirdCard = card(12L, taskList, 3);
        CardMoveRequest request = new CardMoveRequest(1L, 3);
        CardResponse response = new CardResponse(10L, "Card 10", "Text 10", "#327edc", 3);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(firstCard));
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(taskList));
        when(cardRepository.findAllByTaskList_IdOrderByPositionAsc(1L))
                .thenReturn(List.of(firstCard, secondCard, thirdCard));
        when(cardMapper.toCardResponse(firstCard)).thenReturn(response);

        CardResponse result = service.moveCard(10L, request);

        assertThat(result).isEqualTo(response);
        assertThat(secondCard.getPosition()).isEqualTo(1);
        assertThat(thirdCard.getPosition()).isEqualTo(2);
        assertThat(firstCard.getPosition()).isEqualTo(3);
        assertThat(firstCard.getTaskList()).isSameAs(taskList);
        verify(cardRepository, times(2)).saveAllAndFlush(anyList());
    }

    @Test
    void moveCardMovesCardBetweenTaskLists() {
        TaskList sourceTaskList = taskList(1L, TaskListType.TO_DO);
        TaskList targetTaskList = taskList(2L, TaskListType.IN_PROGRESS);
        Card movedCard = card(10L, sourceTaskList, 1);
        Card sourceCard = card(11L, sourceTaskList, 2);
        Card targetFirstCard = card(20L, targetTaskList, 1);
        Card targetSecondCard = card(21L, targetTaskList, 2);
        CardMoveRequest request = new CardMoveRequest(2L, 2);
        CardResponse response = new CardResponse(10L, "Card 10", "Text 10", "#327edc", 2);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(movedCard));
        when(taskListRepository.findById(2L)).thenReturn(Optional.of(targetTaskList));
        when(cardRepository.findAllByTaskList_IdOrderByPositionAsc(1L))
                .thenReturn(List.of(movedCard, sourceCard));
        when(cardRepository.findAllByTaskList_IdOrderByPositionAsc(2L))
                .thenReturn(List.of(targetFirstCard, targetSecondCard));
        when(cardMapper.toCardResponse(movedCard)).thenReturn(response);

        CardResponse result = service.moveCard(10L, request);

        assertThat(result).isEqualTo(response);
        assertThat(sourceCard.getPosition()).isEqualTo(1);
        assertThat(targetFirstCard.getPosition()).isEqualTo(1);
        assertThat(movedCard.getPosition()).isEqualTo(2);
        assertThat(targetSecondCard.getPosition()).isEqualTo(3);
        assertThat(movedCard.getTaskList()).isSameAs(targetTaskList);
        verify(cardRepository, times(2)).saveAllAndFlush(anyList());
    }

    @Test
    void moveCardThrowsBadRequestWhenTargetPositionIsTooHigh() {
        TaskList taskList = taskList(1L, TaskListType.TO_DO);
        Card card = card(10L, taskList, 1);
        CardMoveRequest request = new CardMoveRequest(1L, 3);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(taskList));
        when(cardRepository.findAllByTaskList_IdOrderByPositionAsc(1L)).thenReturn(List.of(card));

        assertThatThrownBy(() -> service.moveCard(10L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("targetPosition must be between 1 and 1");

        verify(cardRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void moveCardThrowsNotFoundWhenCardDoesNotExist() {
        CardMoveRequest request = new CardMoveRequest(1L, 1);

        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moveCard(99L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card not found with id: 99");

        verify(taskListRepository, never()).findById(any());
        verify(cardRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void moveCardThrowsNotFoundWhenTargetTaskListDoesNotExist() {
        TaskList taskList = taskList(1L, TaskListType.TO_DO);
        Card card = card(10L, taskList, 1);
        CardMoveRequest request = new CardMoveRequest(99L, 1);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moveCard(10L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task list not found with id: 99");

        verify(cardRepository, never()).saveAllAndFlush(anyList());
    }

    private TaskList taskList(Long id, TaskListType type) {
        return TaskList.builder()
                .id(id)
                .type(type)
                .build();
    }

    private Card card(Long id, TaskList taskList, Integer position) {
        Card card = new Card();
        card.setId(id);
        card.setTitle("Card " + id);
        card.setText("Text " + id);
        card.setColorCode("#327edc");
        card.setTaskList(taskList);
        card.setPosition(position);
        return card;
    }
}

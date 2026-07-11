package com.kanban.models.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.exceptions.NotFoundException;
import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListRepository;
import com.kanban.models.tasklist.TaskListType;
import org.junit.jupiter.api.Test;

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
}

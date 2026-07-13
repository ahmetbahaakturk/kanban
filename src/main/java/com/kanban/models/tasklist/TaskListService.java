package com.kanban.models.tasklist;

import com.kanban.exceptions.BadRequestException;
import com.kanban.exceptions.NotFoundException;
import com.kanban.models.board.Board;
import com.kanban.models.card.Card;
import com.kanban.models.card.CardRepository;
import com.kanban.models.tasklist.dto.TaskListOrderRequest;
import com.kanban.models.tasklist.dto.TaskListsOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskListService {
    private final TaskListRepository repository;
    private final CardRepository cardRepository;

    public void createTaskLists(Board board) {
        List<TaskList> taskListsToSave = new ArrayList<>();

        for (TaskListType type : TaskListType.values()) {
            TaskList taskListToSave = TaskList.builder()
                    .type(type)
                    .board(board)
                    .build();

            taskListsToSave.add(taskListToSave);
        }

        repository.saveAll(taskListsToSave);
    }

    @Transactional
    public void updateCardOrder(TaskListsOrderRequest request) {
        List<TaskListOrderRequest> requestedTaskLists = request.taskLists();
        List<Long> taskListIds = requestedTaskLists.stream()
                .map(TaskListOrderRequest::taskListId)
                .toList();

        List<TaskList> taskLists = repository.findAllById(taskListIds);
        Map<Long, TaskList> taskListsById = taskLists.stream()
                .collect(Collectors.toMap(TaskList::getId, taskList -> taskList));

        for (Long taskListId : taskListIds) {
            if (!taskListsById.containsKey(taskListId)) {
                throw new NotFoundException("Task list not found with id: " + taskListId);
            }
        }

        List<Card> affectedCards = cardRepository.findAllByTaskListInOrderByPositionAsc(taskLists);
        Map<Long, Card> cardsById = affectedCards.stream()
                .collect(Collectors.toMap(Card::getId, card -> card));

        Set<Long> orderedCardIds = new HashSet<>();

        for (TaskListOrderRequest taskListRequest : requestedTaskLists) {
            for (Long cardId : taskListRequest.cardIds()) {
                if (!cardsById.containsKey(cardId) || !orderedCardIds.add(cardId)) {
                    throw new BadRequestException(
                            "cardIds must contain every card from the requested task lists exactly once"
                    );
                }
            }
        }

        if (orderedCardIds.size() != affectedCards.size()) {
            throw new BadRequestException(
                    "cardIds must contain every card from the requested task lists exactly once"
            );
        }

        assignTemporaryPositions(affectedCards);
        cardRepository.saveAllAndFlush(affectedCards);

        for (TaskListOrderRequest taskListRequest : requestedTaskLists) {
            TaskList taskList = taskListsById.get(taskListRequest.taskListId());

            for (int index = 0; index < taskListRequest.cardIds().size(); index++) {
                Card card = cardsById.get(taskListRequest.cardIds().get(index));
                card.setTaskList(taskList);
                card.setPosition(index + 1);
            }
        }

        cardRepository.saveAllAndFlush(affectedCards);
    }

    private void assignTemporaryPositions(List<Card> cards) {
        for (int index = 0; index < cards.size(); index++) {
            cards.get(index).setPosition(-(index + 1));
        }
    }
}

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


    //Board ilk create edilirken varsayılan TastListlerini oluşturan fonksiyon
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

        //İçinde Değişim yapılacak tasklistlerin idsini getirir.
        List<TaskListOrderRequest> requestedTaskLists = request.taskLists();

        //TaskListleri veritabanından çeker
        List<TaskList> taskLists = findTaskLists(requestedTaskLists);

        //Her tasklist için etkilenecek tüm kartları veritabanından çeker.
        List<Card> affectedCards = cardRepository.findAllByTaskListInOrderByPositionAsc(taskLists);


        //Döngü içinde sürekli arama yapmamak için id üzerinden hızlı erişilecek map yapıları kuruyoruz.
        Map<Long, TaskList> taskListsById = mapTaskListsById(taskLists);
        Map<Long, Card> cardsById = mapCardsById(affectedCards);

        //Frontendden gelen card idlerinin gerçekten bu tasklistlere ait olup olmadığını kontrol ediyoruz.
        validateRequestedCards(requestedTaskLists, cardsById, affectedCards.size());

        //Frontendden gelen tasklist ve position düzenini doğrudan kartlara uyguluyoruz.
        applyRequestedOrder(requestedTaskLists, taskListsById, cardsById);
        cardRepository.saveAll(affectedCards);
    }

    private List<TaskList> findTaskLists(List<TaskListOrderRequest> requestedTaskLists) {
        List<Long> taskListIds = requestedTaskLists.stream()
                .map(TaskListOrderRequest::taskListId)
                .toList();
        List<TaskList> taskLists = repository.findAllById(taskListIds);
        Map<Long, TaskList> taskListsById = mapTaskListsById(taskLists);

        for (Long taskListId : taskListIds) {
            if (!taskListsById.containsKey(taskListId)) {
                throw new NotFoundException("Task list not found with id: " + taskListId);
            }
        }

        return taskLists;
    }

    private Map<Long, TaskList> mapTaskListsById(List<TaskList> taskLists) {
        return taskLists.stream()
                .collect(Collectors.toMap(TaskList::getId, taskList -> taskList));
    }

    private Map<Long, Card> mapCardsById(List<Card> cards) {
        return cards.stream()
                .collect(Collectors.toMap(Card::getId, card -> card));
    }

    private void validateRequestedCards(
            List<TaskListOrderRequest> requestedTaskLists,
            Map<Long, Card> cardsById,
            int affectedCardCount
    ) {
        //Aynı kart iki kez gönderilmiş mi veya eksik kart var mı diye takip ediyoruz.
        Set<Long> orderedCardIds = new HashSet<>();

        for (TaskListOrderRequest taskListRequest : requestedTaskLists) {
            for (Long cardId : taskListRequest.cardIds()) {
                if (!cardsById.containsKey(cardId) || !orderedCardIds.add(cardId)) {
                    throwInvalidCardOrderException();
                }
            }
        }

        if (orderedCardIds.size() != affectedCardCount) {
            throwInvalidCardOrderException();
        }
    }

    private void throwInvalidCardOrderException() {
        throw new BadRequestException(
                "cardIds must contain every card from the requested task lists exactly once"
        );
    }

    private void applyRequestedOrder(
            List<TaskListOrderRequest> requestedTaskLists,
            Map<Long, TaskList> taskListsById,
            Map<Long, Card> cardsById
    ) {
        //Her tasklist için gelen card id sırasını birebir position değerine çeviriyoruz.
        for (TaskListOrderRequest taskListRequest : requestedTaskLists) {
            TaskList taskList = taskListsById.get(taskListRequest.taskListId());

            for (int index = 0; index < taskListRequest.cardIds().size(); index++) {
                Card card = cardsById.get(taskListRequest.cardIds().get(index));
                card.setTaskList(taskList);
                card.setPosition(index + 1);
            }
        }
    }

}

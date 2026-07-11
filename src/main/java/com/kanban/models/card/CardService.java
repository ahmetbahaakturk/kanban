package com.kanban.models.card;

import com.kanban.exceptions.BadRequestException;
import com.kanban.exceptions.NotFoundException;
import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardMoveRequest;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final TaskListRepository taskListRepository;
    private final CardMapper cardMapper;
    private final CardColorProvider cardColorProvider;

    @Transactional
    public CardResponse createCard(CardCreateRequest request) {

        // Check that the task list exists before creating the card.
        TaskList taskList = taskListRepository.findById(request.taskListId())
                .orElseThrow(() ->
                        new NotFoundException("Task list not found with id: " + request.taskListId())
                );


        // Place the new card after the current last card in the task list.
        Integer position = cardRepository.findMaxPositionByTaskListId(request.taskListId())
                .map(maxPosition -> maxPosition + 1)
                .orElse(1);


        String colorCode = cardColorProvider.randomColorCode();
        Card cardToSave = cardMapper.toCard(request, taskList, colorCode, position);
        Card savedCard = cardRepository.save(cardToSave);

        return cardMapper.toCardResponse(savedCard);
    }

    @Transactional
    public CardResponse moveCard(Long cardId, CardMoveRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found with id: " + cardId));
        TaskList targetTaskList = taskListRepository.findById(request.targetTaskListId())
                .orElseThrow(() ->
                        new NotFoundException("Task list not found with id: " + request.targetTaskListId())
                );
        Long sourceTaskListId = card.getTaskList().getId();
        Long targetTaskListId = targetTaskList.getId();

        if (sourceTaskListId.equals(targetTaskListId)) {
            reorderWithinTaskList(card, sourceTaskListId, request.targetPosition());
        } else {
            moveBetweenTaskLists(card, sourceTaskListId, targetTaskList, request.targetPosition());
        }

        return cardMapper.toCardResponse(card);
    }

    private void reorderWithinTaskList(Card card, Long taskListId, Integer targetPosition) {
        List<Card> reorderedCards = new ArrayList<>(
                cardRepository.findAllByTaskList_IdOrderByPositionAsc(taskListId)
        );

        reorderedCards.removeIf(currentCard -> currentCard.getId().equals(card.getId()));
        validateTargetPosition(targetPosition, reorderedCards.size() + 1);
        reorderedCards.add(targetPosition - 1, card);

        saveWithTemporaryPositions(reorderedCards, List.of(reorderedCards));
    }

    private void moveBetweenTaskLists(
            Card card,
            Long sourceTaskListId,
            TaskList targetTaskList,
            Integer targetPosition
    ) {
        List<Card> sourceCards = new ArrayList<>(
                cardRepository.findAllByTaskList_IdOrderByPositionAsc(sourceTaskListId)
        );
        List<Card> targetCards = new ArrayList<>(
                cardRepository.findAllByTaskList_IdOrderByPositionAsc(targetTaskList.getId())
        );

        sourceCards.removeIf(currentCard -> currentCard.getId().equals(card.getId()));
        validateTargetPosition(targetPosition, targetCards.size() + 1);
        targetCards.add(targetPosition - 1, card);

        List<Card> affectedCards = distinctCards(sourceCards, targetCards);
        assignTemporaryPositions(affectedCards);
        cardRepository.saveAllAndFlush(affectedCards);

        normalizePositions(sourceCards);
        card.setTaskList(targetTaskList);
        normalizePositions(targetCards);
        cardRepository.saveAllAndFlush(distinctCards(sourceCards, targetCards));
    }

    private void saveWithTemporaryPositions(List<Card> affectedCards, List<List<Card>> finalLists) {
        assignTemporaryPositions(affectedCards);
        cardRepository.saveAllAndFlush(affectedCards);

        finalLists.forEach(this::normalizePositions);
        cardRepository.saveAllAndFlush(affectedCards);
    }

    private void validateTargetPosition(Integer targetPosition, int maxPosition) {
        if (targetPosition > maxPosition) {
            throw new BadRequestException("targetPosition must be between 1 and " + maxPosition);
        }
    }

    private void assignTemporaryPositions(List<Card> cards) {
        for (int index = 0; index < cards.size(); index++) {
            cards.get(index).setPosition(-(index + 1));
        }
    }

    private void normalizePositions(List<Card> cards) {
        for (int index = 0; index < cards.size(); index++) {
            cards.get(index).setPosition(index + 1);
        }
    }

    private List<Card> distinctCards(List<Card> cards) {
        Map<Long, Card> cardsById = new LinkedHashMap<>();

        for (Card card : cards) {
            cardsById.put(card.getId(), card);
        }

        return new ArrayList<>(cardsById.values());
    }

    @SafeVarargs
    private List<Card> distinctCards(List<Card>... cardLists) {
        List<Card> cards = new ArrayList<>();

        for (List<Card> cardList : cardLists) {
            cards.addAll(cardList);
        }

        return distinctCards(cards);
    }
}

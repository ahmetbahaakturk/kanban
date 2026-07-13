package com.kanban.models.card;

import com.kanban.exceptions.NotFoundException;
import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.card.dto.CardUpdateRequest;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final TaskListRepository taskListRepository;
    private final CardMapper cardMapper;
    private final CardColorProvider cardColorProvider;

    @Transactional
    public CardResponse createCard(CardCreateRequest request) {

        // İçine card eklenmesi istenilen TaskListin varlığını kontrol ediyoruz.
        TaskList taskList = taskListRepository.findById(request.taskListId())
                .orElseThrow(() ->
                        new NotFoundException("Task list not found with id: " + request.taskListId())
                );


        // En büyük sıra numaraları kartın bir sırasını numara olarak belirliyoruz.
        Integer position = cardRepository.findMaxPositionByTaskListId(request.taskListId())
                .map(maxPosition -> maxPosition + 1)
                .orElse(1);


        String colorCode = cardColorProvider.randomColorCode();
        Card cardToSave = cardMapper.toCard(request, taskList, colorCode, position);
        Card savedCard = cardRepository.save(cardToSave);

        return cardMapper.toCardResponse(savedCard);
    }

    @Transactional
    public CardResponse updateCard(Long cardId, CardUpdateRequest request) {
        Card card = findCard(cardId);

        cardMapper.updateCard(card, request);

        return cardMapper.toCardResponse(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = findCard(cardId);
        Long taskListId = card.getTaskList().getId();
        Integer position = card.getPosition();

        cardRepository.delete(card);
        cardRepository.flush();
        cardRepository.decrementPositionsAfter(taskListId, position);
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new NotFoundException("Card not found with id: " + cardId)
                );
    }
}

package com.kanban.models.card;

import com.kanban.exceptions.NotFoundException;
import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardResponse;
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

        //Tasklistin veritabanındaki kontrolünü gerçkeleştiriyoruz.
        TaskList taskList = taskListRepository.findById(request.taskListId())
                .orElseThrow(() ->
                        new NotFoundException("Task list not found with id: " + request.taskListId())
                );


        //Card nesnesini create ederken listede hali hazırda bulunan en yüksek pozisyonun 1 eklenmiş halini yapıyoruz.
        Integer position = cardRepository.findMaxPositionByTaskListId(request.taskListId())
                .map(maxPosition -> maxPosition + 1)
                .orElse(1);


        String colorCode = cardColorProvider.randomColorCode();
        Card cardToSave = cardMapper.toCard(request, taskList, colorCode, position);
        Card savedCard = cardRepository.save(cardToSave);

        return cardMapper.toCardResponse(savedCard);
    }
}

package com.kanban.models.tasklist;

import com.kanban.models.card.Card;
import com.kanban.models.card.CardMapper;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.tasklist.dto.TaskListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskListMapper {
    private final CardMapper cardMapper;

    public TaskListResponse toTaskListResponse(TaskList taskList, List<Card> cards) {
        List<CardResponse> cardResponses = cards.stream()
                .map(cardMapper::toCardResponse)
                .toList();

        return new TaskListResponse(
                taskList.getId(),
                taskList.getType(),
                cardResponses
        );
    }
}

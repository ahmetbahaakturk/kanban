package com.kanban.models.card;

import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardUpdateRequest;
import com.kanban.models.tasklist.TaskList;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public Card toCard(CardCreateRequest request, TaskList taskList, String colorCode, Integer position) {
        Card card = new Card();
        card.setTitle(request.title());
        card.setText(request.text());
        card.setTaskList(taskList);
        card.setColorCode(colorCode);
        card.setPosition(position);

        return card;
    }

    public void updateCard(Card card, CardUpdateRequest request) {
        card.setTitle(request.title());
        card.setText(request.text());

        if (request.colorCode() != null) {
            card.setColorCode(request.colorCode());
        }
    }

    public CardResponse toCardResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getTitle(),
                card.getText(),
                card.getColorCode(),
                card.getPosition()
        );
    }
}

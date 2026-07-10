package com.kanban.models.card;

import com.kanban.models.card.dto.CardResponse;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

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

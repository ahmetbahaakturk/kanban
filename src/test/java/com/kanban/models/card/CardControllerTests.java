package com.kanban.models.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.card.dto.CardUpdateRequest;
import org.junit.jupiter.api.Test;

class CardControllerTests {
    private final CardService cardService = mock(CardService.class);
    private final CardController controller = new CardController(cardService);

    @Test
    void createCardDelegatesToService() {
        CardCreateRequest request = new CardCreateRequest(1L, "Card title", "Card text");
        CardResponse response = new CardResponse(10L, "Card title", "Card text", "#327edc", 1);

        when(cardService.createCard(request)).thenReturn(response);

        CardResponse result = controller.createCard(request);

        assertThat(result).isEqualTo(response);
        verify(cardService).createCard(request);
    }

    @Test
    void updateCardDelegatesToService() {
        CardUpdateRequest request = new CardUpdateRequest("Updated title", "Updated text", "#0cae96");
        CardResponse response = new CardResponse(10L, "Updated title", "Updated text", "#0cae96", 1);

        when(cardService.updateCard(10L, request)).thenReturn(response);

        CardResponse result = controller.updateCard(10L, request);

        assertThat(result).isEqualTo(response);
        verify(cardService).updateCard(10L, request);
    }

    @Test
    void deleteCardDelegatesToService() {
        controller.deleteCard(10L);

        verify(cardService).deleteCard(10L);
    }
}

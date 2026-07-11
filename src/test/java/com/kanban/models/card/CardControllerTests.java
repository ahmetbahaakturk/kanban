package com.kanban.models.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardResponse;
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
}

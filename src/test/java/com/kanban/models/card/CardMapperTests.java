package com.kanban.models.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.kanban.models.card.dto.CardCreateRequest;
import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.card.dto.CardUpdateRequest;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListType;
import org.junit.jupiter.api.Test;

class CardMapperTests {
    private final CardMapper mapper = new CardMapper();

    @Test
    void toCardMapsCreateRequestToCard() {
        TaskList taskList = TaskList.builder()
                .id(1L)
                .type(TaskListType.TO_DO)
                .build();
        CardCreateRequest request = new CardCreateRequest(1L, "Card title", "Card text");

        Card card = mapper.toCard(request, taskList, "#327edc", 3);

        assertThat(card.getTitle()).isEqualTo("Card title");
        assertThat(card.getText()).isEqualTo("Card text");
        assertThat(card.getTaskList()).isSameAs(taskList);
        assertThat(card.getColorCode()).isEqualTo("#327edc");
        assertThat(card.getPosition()).isEqualTo(3);
    }

    @Test
    void updateCardMapsUpdateRequestToExistingCard() {
        Card card = new Card();
        card.setTitle("Old title");
        card.setText("Old text");
        card.setColorCode("#327edc");
        CardUpdateRequest request = new CardUpdateRequest("Updated title", "Updated text", "#0cae96");

        mapper.updateCard(card, request);

        assertThat(card.getTitle()).isEqualTo("Updated title");
        assertThat(card.getText()).isEqualTo("Updated text");
        assertThat(card.getColorCode()).isEqualTo("#0cae96");
    }

    @Test
    void updateCardKeepsCurrentColorWhenColorCodeIsNull() {
        Card card = new Card();
        card.setColorCode("#327edc");
        CardUpdateRequest request = new CardUpdateRequest("Updated title", null, null);

        mapper.updateCard(card, request);

        assertThat(card.getColorCode()).isEqualTo("#327edc");
    }

    @Test
    void toCardResponseMapsCardToResponse() {
        Card card = new Card();
        card.setId(10L);
        card.setTitle("Card title");
        card.setText("Card text");
        card.setColorCode("#327edc");
        card.setPosition(1);

        CardResponse response = mapper.toCardResponse(card);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Card title");
        assertThat(response.text()).isEqualTo("Card text");
        assertThat(response.colorCode()).isEqualTo("#327edc");
        assertThat(response.position()).isEqualTo(1);
    }
}

package com.kanban.models.card;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CardColorProvider {
    private static final List<String> DEFAULT_COLOR_CODES = List.of(
            "#c742a7",
            "#df3035",
            "#327edc",
            "#6b6ed0",
            "#0cae96"
    );

    public String randomColorCode() {
        int index = ThreadLocalRandom.current().nextInt(DEFAULT_COLOR_CODES.size());

        return DEFAULT_COLOR_CODES.get(index);
    }
}

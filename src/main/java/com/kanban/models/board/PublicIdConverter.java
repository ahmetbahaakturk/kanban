package com.kanban.models.board;

import com.kanban.models.board.dto.PublicId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


//Bu converter sınıfı sayesinde JSON'dan düz string formatında gelen veriyi
//spring boot otomatik olarak PublicId'ye mapliyor.
@Component
public class PublicIdConverter implements Converter<String, PublicId> {

    @Override
    public PublicId convert(String source) {
        return new PublicId(source);
    }
}

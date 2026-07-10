package com.kanban.models.board;

import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.board.dto.PublicId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Validated
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse createBoard(@Valid @RequestBody BoardCreateRequest request) {
        return boardService.createBoard(request);
    }

    @GetMapping
    public Boolean isAvailable(@Valid @RequestParam PublicId publicId) {
        return boardService.isAvailable(publicId);
    }

    @GetMapping("/{publicId}")
    public BoardDetailResponse getBoardDetail(@Valid @PathVariable PublicId publicId) {
        return boardService.getBoardDetail(publicId);
    }
}

package com.kanban.models.board;

import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse createBoard(@Valid @RequestBody BoardCreateRequest request) {
        return boardService.createBoard(request);
    }

    @GetMapping("/{publicId}")
    public BoardDetailResponse getBoardDetail(@PathVariable String publicId) {
        return boardService.getBoardDetail(publicId);
    }
}

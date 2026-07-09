package com.kanban.models.board;

import com.kanban.exceptions.AlreadyExistsException;
import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.tasklist.TaskListService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository repository;
    private final BoardMapper boardMapper;
    private final TaskListService taskListService;

    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request) {
        if (repository.existsById(request.publicId())) {
            throw new AlreadyExistsException("Board already exists with publicId: " + request.publicId());
        }

        Board boardToSave = boardMapper.toBoard(request);
        Board savedBoard;

        try {
            savedBoard = repository.save(boardToSave);
        } catch (DataIntegrityViolationException exception) {
            throw new AlreadyExistsException("Board already exists with publicId: " + request.publicId());
        }

        taskListService.createTaskLists(savedBoard);

        return boardMapper.toBoardResponse(savedBoard);
    }
}

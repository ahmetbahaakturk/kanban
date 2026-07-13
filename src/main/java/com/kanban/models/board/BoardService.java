package com.kanban.models.board;

import com.kanban.exceptions.AlreadyExistsException;
import com.kanban.exceptions.NotFoundException;
import com.kanban.models.board.dto.BoardDetailResponse;
import com.kanban.models.board.dto.BoardCreateRequest;
import com.kanban.models.board.dto.BoardResponse;
import com.kanban.models.board.dto.PublicId;
import com.kanban.models.card.Card;
import com.kanban.models.card.CardRepository;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListMapper;
import com.kanban.models.tasklist.TaskListRepository;
import com.kanban.models.tasklist.TaskListService;
import com.kanban.models.tasklist.dto.TaskListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository repository;
    private final BoardMapper boardMapper;
    private final TaskListService taskListService;
    private final TaskListRepository taskListRepository;
    private final CardRepository cardRepository;
    private final TaskListMapper taskListMapper;

    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request) {
        String publicId = request.publicIdValue();

        //Boardun çakışma durumunu kontrol ediyoruz
        if (repository.existsById(publicId)) {
            throw new AlreadyExistsException("Board already exists with publicId: " + publicId);
        }

        Board boardToSave = boardMapper.toBoard(request);
        Board savedBoard;


        //Aynı anda create edilme riskine karşın database seviyesinde 2. çakışma kontrolünü gerçekleştiriyoruz.
        try {
            savedBoard = repository.save(boardToSave);
        } catch (DataIntegrityViolationException exception) {
            throw new AlreadyExistsException("Board already exists with publicId: " + publicId);
        }

        taskListService.createTaskLists(savedBoard);

        return boardMapper.toBoardResponse(savedBoard);
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(PublicId publicId) {
        return !repository.existsById(publicId.value());
    }

    @Transactional(readOnly = true)
    public BoardDetailResponse getBoardDetail(PublicId requestedPublicId) {
        String publicId = requestedPublicId.value();

        Board board = repository.findById(publicId).orElseThrow(() ->
                new NotFoundException("Board not found with publicId: " + publicId)
        );

        List<TaskList> taskLists = taskListRepository.findAllByBoard_PublicIdOrderByIdAsc(publicId);

        //Eğer boarda bağlı bir taskList yok ise o boardu sistemden siliyoruz ve frontende hata yolluyoruz.
        if (taskLists.isEmpty()) {
            repository.delete(board);
            throw new IllegalStateException("Board has no task lists: " + publicId);
        }

        List<Card> cards = cardRepository.findAllByTaskListInOrderByPositionAsc(taskLists);

        //İlk önce her card listesini kendi içinde idsine göre sıralayıp sonra onları TastListlere
        //göre map ediyoruz. Böylece arayüze sıralı gelmiş bir yapıya ön ayak oluyor.
        Map<Long, List<Card>> cardsByTaskListId = cards.stream()
                .collect(Collectors.groupingBy(
                        card -> card.getTaskList().getId())
                );

        //TaskList alt dtosunu hazırlıyoruz.
        List<TaskListResponse> taskListResponses = taskLists.stream()
                .map(taskList -> taskListMapper.toTaskListResponse(
                        taskList,
                        cardsByTaskListId.getOrDefault(taskList.getId(), List.of())
                ))
                .toList();

        return boardMapper.toBoardDetailResponse(board, taskListResponses);
    }
}

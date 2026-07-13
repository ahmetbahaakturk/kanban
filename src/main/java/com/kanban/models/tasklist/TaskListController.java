package com.kanban.models.tasklist;

import com.kanban.models.tasklist.dto.TaskListsOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task-lists")
@RequiredArgsConstructor
public class TaskListController {
    private final TaskListService taskListService;

    @PutMapping("/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCardOrder(@Valid @RequestBody TaskListsOrderRequest request) {
        taskListService.updateCardOrder(request);
    }
}

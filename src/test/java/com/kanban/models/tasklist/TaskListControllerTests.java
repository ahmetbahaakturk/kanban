package com.kanban.models.tasklist;

import com.kanban.models.tasklist.dto.TaskListOrderRequest;
import com.kanban.models.tasklist.dto.TaskListsOrderRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TaskListControllerTests {
    private final TaskListService taskListService = mock(TaskListService.class);
    private final TaskListController controller = new TaskListController(taskListService);

    @Test
    void updateCardOrderDelegatesToService() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(List.of(
                new TaskListOrderRequest(1L, List.of(10L, 11L))
        ));

        controller.updateCardOrder(request);

        verify(taskListService).updateCardOrder(request);
    }
}

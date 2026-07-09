package com.kanban.models.tasklist;

import com.kanban.models.board.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskListService {
    private final TaskListRepository repository;

    public List<TaskList> createTaskLists(Board board) {
        List<TaskList> taskListsToSave = new ArrayList<>();

        for (TaskListType type : TaskListType.values()) {
            TaskList taskListToSave = TaskList.builder()
                    .type(type)
                    .board(board)
                    .build();

            taskListsToSave.add(taskListToSave);
        }

        return repository.saveAll(taskListsToSave);
    }
}

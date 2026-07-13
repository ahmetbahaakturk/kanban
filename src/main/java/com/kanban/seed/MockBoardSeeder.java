package com.kanban.seed;

import com.kanban.models.board.Board;
import com.kanban.models.board.BoardRepository;
import com.kanban.models.card.Card;
import com.kanban.models.card.CardRepository;
import com.kanban.models.tasklist.TaskList;
import com.kanban.models.tasklist.TaskListRepository;
import com.kanban.models.tasklist.TaskListType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MockBoardSeeder implements ApplicationRunner {
    private static final String MOCK_BOARD_PUBLIC_ID = "mock";

    private final BoardRepository boardRepository;
    private final TaskListRepository taskListRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (boardRepository.existsById(MOCK_BOARD_PUBLIC_ID)) {
            return;
        }

        Board board = Board.builder()
                .publicId(MOCK_BOARD_PUBLIC_ID)
                .createdDate(Instant.now())
                .build();
        Board savedBoard = boardRepository.save(board);
        List<TaskList> savedTaskLists = taskListRepository.saveAll(createTaskLists(savedBoard));
        Map<TaskListType, TaskList> taskListsByType = mapTaskListsByType(savedTaskLists);

        cardRepository.saveAll(createCards(taskListsByType));
    }

    private List<TaskList> createTaskLists(Board board) {
        List<TaskList> taskLists = new ArrayList<>();

        for (TaskListType type : TaskListType.values()) {
            TaskList taskList = TaskList.builder()
                    .board(board)
                    .type(type)
                    .build();

            taskLists.add(taskList);
        }

        return taskLists;
    }

    private Map<TaskListType, TaskList> mapTaskListsByType(List<TaskList> taskLists) {
        Map<TaskListType, TaskList> taskListsByType = new EnumMap<>(TaskListType.class);

        for (TaskList taskList : taskLists) {
            taskListsByType.put(taskList.getType(), taskList);
        }

        return taskListsByType;
    }

    private List<Card> createCards(Map<TaskListType, TaskList> taskListsByType) {
        return List.of(
                card(taskListsByType.get(TaskListType.BACKLOG), 1, "Twilio integration",
                        "Create new note via SMS. Support text, audio, links, and media.", "#c742a7"),
                card(taskListsByType.get(TaskListType.BACKLOG), 2, "Markdown support",
                        "Markdown shorthand converts to formatting.", "#6b6ed0"),
                card(taskListsByType.get(TaskListType.TO_DO), 1, "Tablet view",
                        "Layout pass for medium screens.", "#df3035"),
                card(taskListsByType.get(TaskListType.TO_DO), 2, "Mobile view",
                        "Functions for both web responsive and native apps.", "#df3035"),
                card(taskListsByType.get(TaskListType.IN_PROGRESS), 1, "Desktop view",
                        "PWA for website and native apps. Windows and Mac need unique share icons.", "#327edc"),
                card(taskListsByType.get(TaskListType.DONE), 1, "Audio recording",
                        "Interface for when recording a new audio note.", "#0cae96")
        );
    }

    private Card card(TaskList taskList, Integer position, String title, String text, String colorCode) {
        Card card = new Card();
        card.setTaskList(taskList);
        card.setPosition(position);
        card.setTitle(title);
        card.setText(text);
        card.setColorCode(colorCode);

        return card;
    }
}

package com.kanban.models.card;

import com.kanban.models.tasklist.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByTaskListInOrderByPositionAsc(List<TaskList> taskLists);

    @Query("select max(card.position) from Card card where card.taskList.id = :taskListId")
    Optional<Integer> findMaxPositionByTaskListId(Long taskListId);
}

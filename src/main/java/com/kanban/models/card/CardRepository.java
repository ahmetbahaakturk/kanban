package com.kanban.models.card;

import com.kanban.models.tasklist.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByTaskListInOrderByPositionAsc(List<TaskList> taskLists);


    //O an taskliste bağlı cardların en büyük position değelerini çekiyoruz.
    @Query("select max(card.position) from Card card where card.taskList.id = :taskListId")
    Optional<Integer> findMaxPositionByTaskListId(Long taskListId);

    //Silinen karttan sonraki kartların sıra numaralarını 1 düşürürek sıralama uyumlu hale gelir
    @Modifying
    @Query("""
            update Card card
            set card.position = card.position - 1
            where card.taskList.id = :taskListId and card.position > :position
            """)
    void decrementPositionsAfter(Long taskListId, Integer position);
}

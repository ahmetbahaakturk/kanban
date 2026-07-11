import { useDroppable } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
import CardCreateForm from './CardCreateForm'
import SortableBoardCard from './SortableBoardCard'

function BoardColumn({
  cardMessage,
  isCreatingCard,
  loading,
  taskList,
  onAddCard,
  onCancelCard,
  onCreateCard,
}) {
  const { isOver, setNodeRef } = useDroppable({
    id: `taskList:${taskList.id}`,
    data: {
      taskListId: taskList.id,
      type: 'taskList',
    },
  })

  return (
    <article className={`column ${isOver ? 'column-over' : ''}`}>
      <div className="column-header">
        <h2>{taskList.title}</h2>
        <button
          className="column-add"
          type="button"
          onClick={() => onAddCard(taskList)}
          aria-label={`Add card to ${taskList.title}`}
        >
          +
        </button>
      </div>
      <div className="cards" ref={setNodeRef}>
        {isCreatingCard ? (
          <CardCreateForm
            loading={loading}
            message={cardMessage}
            onCancel={onCancelCard}
            onSubmit={(request) => onCreateCard(taskList, request)}
          />
        ) : null}
        <SortableContext
          items={taskList.cards.map((card) => `card:${card.id}`)}
          strategy={verticalListSortingStrategy}
        >
          {taskList.cards.length ? (
            taskList.cards.map((card) => (
              <SortableBoardCard card={card} taskListId={taskList.id} key={card.id} />
            ))
          ) : !isCreatingCard ? (
            <div className="empty-card">No cards</div>
          ) : null}
        </SortableContext>
      </div>
    </article>
  )
}

export default BoardColumn

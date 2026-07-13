import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import BoardCard from './BoardCard'

function SortableBoardCard({ card, onEditCard, taskListId }) {
  const {
    attributes,
    isDragging,
    listeners,
    setNodeRef,
    transform,
    transition,
  } = useSortable({
    id: `card:${card.id}`,
    data: {
      card,
      taskListId,
      type: 'card',
    },
  })

  const style = {
    opacity: isDragging ? 0.35 : undefined,
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <BoardCard
      card={card}
      className="sortable-card"
      dragAttributes={attributes}
      dragListeners={listeners}
      isDragging={isDragging}
      onDoubleClick={() => onEditCard(card)}
      setNodeRef={setNodeRef}
      style={style}
    />
  )
}

export default SortableBoardCard

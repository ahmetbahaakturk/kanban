import { useDroppable } from '@dnd-kit/core'

function CardDeleteDropZone({ active }) {
  const { isOver, setNodeRef } = useDroppable({
    id: 'delete-card',
    data: {
      type: 'delete-card',
    },
  })

  return (
    <div
      ref={setNodeRef}
      className={`delete-drop-zone ${active ? 'visible' : ''} ${isOver ? 'over' : ''}`.trim()}
    >
      Drop here to delete
    </div>
  )
}

export default CardDeleteDropZone

import {
  closestCorners,
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import { arrayMove } from '@dnd-kit/sortable'
import { useEffect, useRef, useState } from 'react'
import BoardCard from './BoardCard'
import BoardColumn from './BoardColumn'

function parseCardId(dndId) {
  return String(dndId).replace('card:', '')
}

function findCardLocation(taskLists, cardId) {
  const normalizedCardId = String(cardId)

  for (const [taskListIndex, taskList] of taskLists.entries()) {
    const cardIndex = taskList.cards.findIndex((card) => String(card.id) === normalizedCardId)

    if (cardIndex !== -1) {
      return {
        card: taskList.cards[cardIndex],
        cardIndex,
        taskList,
        taskListIndex,
      }
    }
  }

  return null
}

function withCardPositions(taskLists) {
  return taskLists.map((taskList) => ({
    ...taskList,
    cards: taskList.cards.map((card, index) => ({
      ...card,
      position: index + 1,
    })),
  }))
}

function moveCardInTaskLists(taskLists, activeDndId, overDndId) {
  const activeCardId = parseCardId(activeDndId)
  const activeLocation = findCardLocation(taskLists, activeCardId)

  if (!activeLocation || !overDndId || activeDndId === overDndId) {
    return taskLists
  }

  const nextTaskLists = taskLists.map((taskList) => ({
    ...taskList,
    cards: [...taskList.cards],
  }))

  if (String(overDndId).startsWith('card:')) {
    const overCardId = parseCardId(overDndId)
    const overLocation = findCardLocation(nextTaskLists, overCardId)

    if (!overLocation) {
      return taskLists
    }

    if (activeLocation.taskList.id === overLocation.taskList.id) {
      nextTaskLists[activeLocation.taskListIndex].cards = arrayMove(
        nextTaskLists[activeLocation.taskListIndex].cards,
        activeLocation.cardIndex,
        overLocation.cardIndex,
      )

      return withCardPositions(nextTaskLists)
    }

    nextTaskLists[activeLocation.taskListIndex].cards.splice(activeLocation.cardIndex, 1)
    nextTaskLists[overLocation.taskListIndex].cards.splice(
      overLocation.cardIndex,
      0,
      activeLocation.card,
    )

    return withCardPositions(nextTaskLists)
  }

  const targetTaskListIndex = nextTaskLists.findIndex((taskList) => {
    return `taskList:${taskList.id}` === overDndId
  })

  if (targetTaskListIndex === -1) {
    return taskLists
  }

  nextTaskLists[activeLocation.taskListIndex].cards.splice(activeLocation.cardIndex, 1)
  nextTaskLists[targetTaskListIndex].cards.push(activeLocation.card)

  return withCardPositions(nextTaskLists)
}

function BoardPage({
  board,
  message,
  taskLists,
  onAddCard,
  onChangeBoard,
  onMoveCard,
}) {
  const [activeCard, setActiveCard] = useState(null)
  const [draftTaskLists, setDraftTaskLists] = useState(null)
  const latestDraftTaskLists = useRef(null)
  const lastOverId = useRef(null)
  const hasDraggedOver = useRef(false)
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 6,
      },
    }),
  )
  const renderedTaskLists = draftTaskLists ?? taskLists

  useEffect(() => {
    if (!activeCard) {
      setDraftTaskLists(null)
      latestDraftTaskLists.current = null
    }
  }, [activeCard, taskLists])

  function handleDragStart(event) {
    const cardId = parseCardId(event.active.id)
    const location = findCardLocation(taskLists, cardId)

    if (!location) {
      return
    }

    setActiveCard(location.card)
    setDraftTaskLists(taskLists)
    latestDraftTaskLists.current = taskLists
    lastOverId.current = null
    hasDraggedOver.current = false
  }

  function handleDragOver(event) {
    if (!event.over || !activeCard || event.active.id === event.over.id) {
      return
    }

    if (lastOverId.current === event.over.id) {
      return
    }

    const nextTaskLists = moveCardInTaskLists(
      latestDraftTaskLists.current ?? taskLists,
      event.active.id,
      event.over.id,
    )

    latestDraftTaskLists.current = nextTaskLists
    lastOverId.current = event.over.id
    hasDraggedOver.current = true
    setDraftTaskLists(nextTaskLists)
  }

  async function handleDragEnd(event) {
    if (!event.over) {
      handleDragCancel()
      return
    }

    const activeDndId = event.active.id
    const finalTaskLists = hasDraggedOver.current
      ? latestDraftTaskLists.current
      : moveCardInTaskLists(taskLists, activeDndId, event.over?.id)
    const finalLocation = findCardLocation(finalTaskLists ?? taskLists, parseCardId(activeDndId))
    const originalLocation = findCardLocation(taskLists, parseCardId(activeDndId))
    const shouldMove = finalLocation
      && originalLocation
      && (
        finalLocation.taskList.id !== originalLocation.taskList.id
        || finalLocation.cardIndex !== originalLocation.cardIndex
      )

    setActiveCard(null)
    setDraftTaskLists(null)
    latestDraftTaskLists.current = null
    lastOverId.current = null
    hasDraggedOver.current = false

    if (shouldMove) {
      await onMoveCard(
        finalLocation.card,
        finalLocation.taskList.id,
        finalLocation.cardIndex + 1,
        finalTaskLists,
      )
    }
  }

  function handleDragCancel() {
    setActiveCard(null)
    setDraftTaskLists(null)
    latestDraftTaskLists.current = null
    lastOverId.current = null
    hasDraggedOver.current = false
  }

  return (
    <main className="board-page">
      <header className="board-header">
        <div>
          <button className="text-button" type="button" onClick={onChangeBoard}>
            Change board
          </button>
          <h1>{board.publicId}</h1>
        </div>
      </header>

      {message ? <p className="board-message">{message}</p> : null}

      <DndContext
        collisionDetection={closestCorners}
        sensors={sensors}
        onDragCancel={handleDragCancel}
        onDragEnd={handleDragEnd}
        onDragOver={handleDragOver}
        onDragStart={handleDragStart}
      >
        <section className="board" aria-label="Kanban board">
          {renderedTaskLists.map((taskList) => (
            <BoardColumn taskList={taskList} onAddCard={onAddCard} key={taskList.id} />
          ))}
        </section>
        <DragOverlay>
          {activeCard ? (
            <BoardCard card={activeCard} className="card-overlay" style={{ cursor: 'grabbing' }} />
          ) : null}
        </DragOverlay>
      </DndContext>
    </main>
  )
}

export default BoardPage

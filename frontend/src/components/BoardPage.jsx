import {
  closestCorners,
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import { useEffect, useRef, useState } from 'react'
import BoardCard from './BoardCard'
import BoardColumn from './BoardColumn'
import CardDeleteDropZone from './CardDeleteDropZone'

const DELETE_ANIMATION_DURATION = 180

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

function withoutCard(taskLists, cardId) {
  const normalizedCardId = String(cardId)

  return withCardPositions(taskLists.map((taskList) => ({
    ...taskList,
    cards: taskList.cards.filter((card) => String(card.id) !== normalizedCardId),
  })))
}

function wait(ms) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms)
  })
}

function shouldInsertAfterOverCard(event) {
  if (!String(event.over?.id).startsWith('card:')) {
    return false
  }

  const activeRect = event.active.rect.current.translated
  const overRect = event.over.rect

  if (!activeRect || !overRect) {
    return false
  }

  const activeMiddleY = activeRect.top + activeRect.height / 2
  const overMiddleY = overRect.top + overRect.height / 2

  return activeMiddleY > overMiddleY
}

function moveCardInTaskLists(taskLists, activeDndId, overDndId, insertAfterOverCard = false) {
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

    let targetCardIndex = overLocation.cardIndex + (insertAfterOverCard ? 1 : 0)

    nextTaskLists[activeLocation.taskListIndex].cards.splice(activeLocation.cardIndex, 1)

    if (
      activeLocation.taskList.id === overLocation.taskList.id
      && activeLocation.cardIndex < targetCardIndex
    ) {
      targetCardIndex -= 1
    }

    targetCardIndex = Math.min(
      targetCardIndex,
      nextTaskLists[overLocation.taskListIndex].cards.length,
    )
    nextTaskLists[overLocation.taskListIndex].cards.splice(
      targetCardIndex,
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
  onDeleteCard,
  onEditCard,
  onUpdateCardOrder,
}) {
  const [activeCard, setActiveCard] = useState(null)
  const [deletingCard, setDeletingCard] = useState(false)
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
    if (!activeCard && !deletingCard) {
      setDraftTaskLists(null)
      latestDraftTaskLists.current = null
    }
  }, [activeCard, deletingCard, taskLists])

  function handleDragStart(event) {
    const cardId = parseCardId(event.active.id)
    const location = findCardLocation(taskLists, cardId)

    if (!location) {
      return
    }

    setActiveCard(location.card)
    setDraftTaskLists(taskLists)
    setDeletingCard(false)
    latestDraftTaskLists.current = taskLists
    lastOverId.current = null
    hasDraggedOver.current = false
  }

  function handleDragOver(event) {
    if (!event.over || !activeCard || event.active.id === event.over.id) {
      return
    }

    if (event.over.id === 'delete-card') {
      return
    }

    const insertAfterOverCard = shouldInsertAfterOverCard(event)
    const overPlacementKey = `${event.over.id}:${insertAfterOverCard ? 'after' : 'before'}`

    if (lastOverId.current === overPlacementKey) {
      return
    }

    const nextTaskLists = moveCardInTaskLists(
      latestDraftTaskLists.current ?? taskLists,
      event.active.id,
      event.over.id,
      insertAfterOverCard,
    )

    latestDraftTaskLists.current = nextTaskLists
    lastOverId.current = overPlacementKey
    hasDraggedOver.current = true
    setDraftTaskLists(nextTaskLists)
  }

  async function handleDragEnd(event) {
    if (!event.over) {
      handleDragCancel()
      return
    }

    const activeDndId = event.active.id

    if (event.over.id === 'delete-card') {
      const cardId = parseCardId(activeDndId)
      const nextTaskLists = withoutCard(latestDraftTaskLists.current ?? taskLists, cardId)

      setDeletingCard(true)
      setDraftTaskLists(nextTaskLists)
      latestDraftTaskLists.current = nextTaskLists

      await wait(DELETE_ANIMATION_DURATION)

      setActiveCard(null)
      lastOverId.current = null
      hasDraggedOver.current = false

      await onDeleteCard(cardId)

      setDeletingCard(false)
      setDraftTaskLists(null)
      latestDraftTaskLists.current = null
      return
    }

    const insertAfterOverCard = shouldInsertAfterOverCard(event)
    const currentTaskLists = hasDraggedOver.current
      ? latestDraftTaskLists.current ?? taskLists
      : taskLists
    const finalTaskLists = event.over.id === activeDndId
      ? currentTaskLists
      : moveCardInTaskLists(currentTaskLists, activeDndId, event.over.id, insertAfterOverCard)
    const finalLocation = findCardLocation(finalTaskLists ?? taskLists, parseCardId(activeDndId))
    const originalLocation = findCardLocation(taskLists, parseCardId(activeDndId))
    const shouldMove = finalLocation
      && originalLocation
      && (
        finalLocation.taskList.id !== originalLocation.taskList.id
        || finalLocation.cardIndex !== originalLocation.cardIndex
      )

    setActiveCard(null)
    setDeletingCard(false)
    setDraftTaskLists(null)
    latestDraftTaskLists.current = null
    lastOverId.current = null
    hasDraggedOver.current = false

    if (shouldMove) {
      const affectedTaskListIds = [
        ...new Set([originalLocation.taskList.id, finalLocation.taskList.id]),
      ]

      await onUpdateCardOrder(finalTaskLists, affectedTaskListIds)
    }
  }

  function handleDragCancel() {
    setActiveCard(null)
    setDeletingCard(false)
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
        <p className="board-help">Double-click a card to edit it.</p>
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
        <CardDeleteDropZone active={Boolean(activeCard)} />
        <section className="board" aria-label="Kanban board">
          {renderedTaskLists.map((taskList) => (
            <BoardColumn
              taskList={taskList}
              onAddCard={onAddCard}
              onEditCard={onEditCard}
              key={taskList.id}
            />
          ))}
        </section>
        <DragOverlay dropAnimation={deletingCard ? null : undefined}>
          {activeCard ? (
            <BoardCard
              card={activeCard}
              className={`card-overlay ${deletingCard ? 'deleting-card-overlay' : ''}`}
              style={{ cursor: 'grabbing' }}
            />
          ) : null}
        </DragOverlay>
      </DndContext>
    </main>
  )
}

export default BoardPage

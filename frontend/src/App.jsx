import { useEffect, useMemo, useState } from 'react'
import BoardActionModal from './components/BoardActionModal'
import BoardPage from './components/BoardPage'
import CardCreateModal from './components/CardCreateModal'
import LandingPage from './components/LandingPage'
import { taskListLabels, taskListOrder } from './constants/kanban'
import './App.css'

function readPublicIdFromUrl() {
  const pathMatch = window.location.pathname.match(/^\/boards\/([^/]+)$/)

  if (pathMatch) {
    return decodeURIComponent(pathMatch[1])
  }

  return new URLSearchParams(window.location.search).get('publicId') ?? ''
}

async function parseResponse(response) {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch {
    return { message: text }
  }
}

function App() {
  const [publicId, setPublicId] = useState(readPublicIdFromUrl)
  const [board, setBoard] = useState(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [modalMode, setModalMode] = useState(null)
  const [modalPublicId, setModalPublicId] = useState('')
  const [cardModalTaskList, setCardModalTaskList] = useState(null)
  const [cardMessage, setCardMessage] = useState('')

  const visibleTaskLists = useMemo(() => {
    const taskLists = board?.taskLists ?? []

    return taskListOrder.map((type) => {
      const taskList = taskLists.find((item) => item.type === type)
      const cards = taskList?.cards ?? []

      return {
        id: taskList?.id ?? type,
        type,
        title: taskListLabels[type],
        cards: [...cards].sort((left, right) => {
          return (left.position ?? 0) - (right.position ?? 0)
        }),
      }
    })
  }, [board])

  useEffect(() => {
    const initialPublicId = readPublicIdFromUrl()

    if (initialPublicId) {
      loadBoard(initialPublicId, { replaceUrl: false })
    }
  }, [])

  async function loadBoard(nextPublicId, options = {}) {
    const trimmedPublicId = nextPublicId.trim()

    if (!trimmedPublicId) {
      return false
    }

    setLoading(true)
    setMessage('')

    try {
      const response = await fetch(`/api/boards/${encodeURIComponent(trimmedPublicId)}`)
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Board could not be found.')
      }

      setBoard(data)
      setPublicId(data.publicId)

      if (options.replaceUrl !== false) {
        window.history.pushState(null, '', `/boards/${encodeURIComponent(data.publicId)}`)
      }

      return true
    } catch (error) {
      setBoard(null)
      setMessage(error.message)
      return false
    } finally {
      setLoading(false)
    }
  }

  async function createBoard(nextPublicId) {
    const trimmedPublicId = nextPublicId.trim()

    if (!trimmedPublicId) {
      return false
    }

    setLoading(true)
    setMessage('')

    try {
      const response = await fetch('/api/boards', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ publicId: trimmedPublicId }),
      })
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Board could not be created.')
      }

      const loaded = await loadBoard(data.publicId)

      if (loaded) {
        setMessage('Board created.')
      }

      return loaded
    } catch (error) {
      setBoard(null)
      setMessage(error.message)
      setLoading(false)
      return false
    }
  }

  async function createCard(taskList, request) {
    setLoading(true)
    setCardMessage('')

    try {
      const response = await fetch('/api/cards', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          taskListId: taskList.id,
          title: request.title,
          text: request.text,
        }),
      })
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Card could not be created.')
      }

      const refreshed = await loadBoard(board.publicId, { replaceUrl: false })

      if (!refreshed) {
        throw new Error('Card was created, but board could not be refreshed.')
      }

      return true
    } catch (error) {
      setCardMessage(error.message)
      return false
    } finally {
      setLoading(false)
    }
  }

  function updateBoardTaskLists(taskLists) {
    setBoard((currentBoard) => {
      if (!currentBoard) {
        return currentBoard
      }

      return {
        ...currentBoard,
        taskLists: taskLists.map((taskList) => ({
          id: taskList.id,
          type: taskList.type,
          cards: taskList.cards.map((card, index) => ({
            ...card,
            position: index + 1,
          })),
        })),
      }
    })
  }

  async function moveCard(card, targetTaskListId, targetPosition, nextTaskLists) {
    const previousBoard = board

    setMessage('')
    updateBoardTaskLists(nextTaskLists)

    try {
      const response = await fetch(`/api/cards/${card.id}/move`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          targetTaskListId,
          targetPosition,
        }),
      })
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Card could not be moved.')
      }

      const refreshed = await loadBoard(previousBoard.publicId, { replaceUrl: false })

      if (!refreshed) {
        setBoard(previousBoard)
      }
    } catch (error) {
      setBoard(previousBoard)
      setMessage(error.message)
    }
  }

  function openModal(mode) {
    setModalMode(mode)
    setModalPublicId(publicId)
    setMessage('')
  }

  function closeModal() {
    if (!loading) {
      setModalMode(null)
    }
  }

  function openCardModal(taskList) {
    setCardModalTaskList(taskList)
    setCardMessage('')
  }

  function closeCardModal() {
    if (!loading) {
      setCardModalTaskList(null)
    }
  }

  function showLanding() {
    setBoard(null)
    setMessage('')
    window.history.pushState(null, '', '/')
  }

  async function handleModalSubmit(event) {
    event.preventDefault()

    const success = modalMode === 'create'
      ? await createBoard(modalPublicId)
      : await loadBoard(modalPublicId)

    if (success) {
      setModalMode(null)
    }
  }

  async function handleCardSubmit(request) {
    const success = await createCard(cardModalTaskList, request)

    if (success) {
      setCardModalTaskList(null)
    }
  }

  if (!board) {
    return (
      <main className="landing">
        <LandingPage loading={loading} message={message} onOpenModal={openModal} />
        {modalMode ? (
          <BoardActionModal
            loading={loading}
            message={message}
            mode={modalMode}
            publicId={modalPublicId}
            onClose={closeModal}
            onPublicIdChange={setModalPublicId}
            onSubmit={handleModalSubmit}
          />
        ) : null}
      </main>
    )
  }

  return (
    <>
      <BoardPage
        board={board}
        message={message}
        taskLists={visibleTaskLists}
        onAddCard={openCardModal}
        onChangeBoard={showLanding}
        onMoveCard={moveCard}
      />
      {cardModalTaskList ? (
        <CardCreateModal
          loading={loading}
          message={cardMessage}
          taskList={cardModalTaskList}
          onClose={closeCardModal}
          onSubmit={handleCardSubmit}
        />
      ) : null}
    </>
  )
}

export default App

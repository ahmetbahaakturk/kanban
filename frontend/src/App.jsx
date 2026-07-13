import { useEffect, useMemo, useState } from 'react'
import BoardActionModal from './components/BoardActionModal'
import BoardPage from './components/BoardPage'
import CardCreateModal from './components/CardCreateModal'
import CardEditModal from './components/CardEditModal'
import LandingPage from './components/LandingPage'
import RecentBoards from './components/RecentBoards'
import { taskListLabels, taskListOrder } from './constants/kanban'
import './App.css'

const RECENT_BOARDS_KEY = 'kanban.recentBoards'
const RECENT_BOARDS_LIMIT = 10
const BOARD_NAME_MIN_LENGTH = 4
const BOARD_NAME_MAX_LENGTH = 60
const BOARD_NAME_PATTERN = /^[A-Za-z0-9._~-]+$/

function readPublicIdFromUrl() {
  const pathMatch = window.location.pathname.match(/^\/boards\/([^/]+)$/)

  if (pathMatch) {
    return decodeURIComponent(pathMatch[1])
  }

  return new URLSearchParams(window.location.search).get('publicId') ?? ''
}

function readRecentBoards() {
  try {
    const parsedItems = JSON.parse(localStorage.getItem(RECENT_BOARDS_KEY) ?? '[]')

    if (!Array.isArray(parsedItems)) {
      return []
    }

    return parsedItems.filter((item) => typeof item === 'string')
  } catch {
    return []
  }
}

function rememberRecentBoard(items, publicId) {
  const nextItems = [
    publicId,
    ...items.filter((item) => item !== publicId),
  ].slice(0, RECENT_BOARDS_LIMIT)

  localStorage.setItem(RECENT_BOARDS_KEY, JSON.stringify(nextItems))

  return nextItems
}

function validateBoardName(publicId) {
  const trimmedPublicId = publicId.trim()

  if (!trimmedPublicId) {
    return 'Board name is required.'
  }

  if (
    trimmedPublicId.length < BOARD_NAME_MIN_LENGTH
    || trimmedPublicId.length > BOARD_NAME_MAX_LENGTH
  ) {
    return `Board name must be between ${BOARD_NAME_MIN_LENGTH} and ${BOARD_NAME_MAX_LENGTH} characters.`
  }

  if (!BOARD_NAME_PATTERN.test(trimmedPublicId)) {
    return 'Use only letters, numbers, dots, dashes, underscores, and tildes.'
  }

  return ''
}

function getBoardLoadErrorMessage(message) {
  if (message?.startsWith('Board has no task lists:')) {
    return 'This board could not be opened because its lists are missing. Please create a new board or try another board name.'
  }

  return message
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
  const [modalMessage, setModalMessage] = useState('')
  const [modalPublicId, setModalPublicId] = useState('')
  const [cardModalTaskList, setCardModalTaskList] = useState(null)
  const [cardMessage, setCardMessage] = useState('')
  const [editingCard, setEditingCard] = useState(null)
  const [editCardMessage, setEditCardMessage] = useState('')
  const [recentBoards, setRecentBoards] = useState(readRecentBoards)

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
    const setErrorMessage = options.setErrorMessage ?? setMessage

    if (!trimmedPublicId) {
      return false
    }

    setLoading(true)
    setErrorMessage('')

    try {
      const response = await fetch(`/api/boards/${encodeURIComponent(trimmedPublicId)}`)
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(getBoardLoadErrorMessage(data?.message) ?? 'Board could not be found.')
      }

      setBoard(data)
      setPublicId(data.publicId)
      setRecentBoards((items) => rememberRecentBoard(items, data.publicId))

      if (options.replaceUrl !== false) {
        window.history.pushState(null, '', `/boards/${encodeURIComponent(data.publicId)}`)
      }

      return true
    } catch (error) {
      setBoard(null)
      setErrorMessage(error.message)
      return false
    } finally {
      setLoading(false)
    }
  }

  async function createBoard(nextPublicId, options = {}) {
    const trimmedPublicId = nextPublicId.trim()
    const setErrorMessage = options.setErrorMessage ?? setMessage

    if (!trimmedPublicId) {
      return false
    }

    setLoading(true)
    setErrorMessage('')

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

      const loaded = await loadBoard(data.publicId, {
        setErrorMessage,
      })

      if (loaded) {
        setMessage('Board created.')
      }

      return loaded
    } catch (error) {
      setBoard(null)
      setErrorMessage(error.message)
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

  async function updateCard(card, request) {
    setLoading(true)
    setEditCardMessage('')

    try {
      const response = await fetch(`/api/cards/${card.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: request.title,
          text: request.text,
          colorCode: request.colorCode,
        }),
      })
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Card could not be updated.')
      }

      const refreshed = await loadBoard(board.publicId, { replaceUrl: false })

      if (!refreshed) {
        throw new Error('Card was updated, but board could not be refreshed.')
      }

      return true
    } catch (error) {
      setEditCardMessage(error.message)
      return false
    } finally {
      setLoading(false)
    }
  }

  async function deleteCard(cardId) {
    setLoading(true)
    setMessage('')

    try {
      const response = await fetch(`/api/cards/${cardId}`, {
        method: 'DELETE',
      })
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Card could not be deleted.')
      }

      const refreshed = await loadBoard(board.publicId, { replaceUrl: false })

      if (!refreshed) {
        throw new Error('Card was deleted, but board could not be refreshed.')
      }
    } catch (error) {
      setMessage(error.message)
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

  async function updateCardOrder(nextTaskLists, affectedTaskListIds) {
    const previousBoard = board

    setMessage('')
    updateBoardTaskLists(nextTaskLists)

    try {
      const taskLists = nextTaskLists
        .filter((taskList) => affectedTaskListIds.includes(taskList.id))
        .map((taskList) => ({
          taskListId: taskList.id,
          cardIds: taskList.cards.map((card) => card.id),
        }))
      const response = await fetch('/api/task-lists/order', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ taskLists }),
      })
      const data = await parseResponse(response)

      if (!response.ok) {
        throw new Error(data?.message ?? 'Card order could not be updated.')
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
    setModalMessage('')
  }

  function changeModalPublicId(nextPublicId) {
    setModalPublicId(nextPublicId)

    if (modalMessage) {
      setModalMessage('')
    }
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

  function openEditCardModal(card) {
    setEditingCard(card)
    setEditCardMessage('')
  }

  function closeEditCardModal() {
    if (!loading) {
      setEditingCard(null)
    }
  }

  function showLanding() {
    setBoard(null)
    setMessage('')
    window.history.pushState(null, '', '/')
  }

  async function handleModalSubmit(event) {
    event.preventDefault()

    const validationMessage = validateBoardName(modalPublicId)

    if (validationMessage) {
      setModalMessage(validationMessage)
      return
    }

    const success = modalMode === 'create'
      ? await createBoard(modalPublicId, { setErrorMessage: setModalMessage })
      : await loadBoard(modalPublicId, { setErrorMessage: setModalMessage })

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

  async function handleCardEditSubmit(request) {
    const success = await updateCard(editingCard, request)

    if (success) {
      setEditingCard(null)
    }
  }

  if (!board) {
    return (
      <main className="landing">
        <LandingPage loading={loading} message={message} onOpenModal={openModal} />
        <RecentBoards
          currentPublicId={publicId}
          items={recentBoards}
          onSelect={(selectedPublicId) => loadBoard(selectedPublicId)}
        />
        {modalMode ? (
          <BoardActionModal
            loading={loading}
            message={modalMessage}
            mode={modalMode}
            publicId={modalPublicId}
            onClose={closeModal}
            onPublicIdChange={changeModalPublicId}
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
        onDeleteCard={deleteCard}
        onEditCard={openEditCardModal}
        onUpdateCardOrder={updateCardOrder}
      />
      <RecentBoards
        currentPublicId={board.publicId}
        items={recentBoards}
        onSelect={(selectedPublicId) => loadBoard(selectedPublicId)}
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
      {editingCard ? (
        <CardEditModal
          card={editingCard}
          loading={loading}
          message={editCardMessage}
          onClose={closeEditCardModal}
          onSubmit={handleCardEditSubmit}
        />
      ) : null}
    </>
  )
}

export default App

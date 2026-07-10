import { useEffect, useMemo, useState } from 'react'
import './App.css'

const taskListLabels = {
  BACKLOG: 'Backlog',
  TO_DO: 'To do',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
}

const taskListOrder = ['BACKLOG', 'TO_DO', 'IN_PROGRESS', 'DONE']

const fallbackCards = {
  BACKLOG: [
    {
      id: 'mock-backlog-1',
      title: 'Twilio integration',
      text: 'Create new note via SMS. Support text, audio, links, and media.',
      colorCode: '#c742a7',
    },
    {
      id: 'mock-backlog-2',
      title: 'Markdown support',
      text: 'Markdown shorthand converts to formatting.',
      colorCode: '#6b6ed0',
    },
  ],
  TO_DO: [
    {
      id: 'mock-todo-1',
      title: 'Tablet view',
      text: 'Layout pass for medium screens.',
      colorCode: '#df3035',
    },
    {
      id: 'mock-todo-2',
      title: 'Mobile view',
      text: 'Functions for both web responsive and native apps.',
      colorCode: '#df3035',
    },
    {
      id: 'mock-todo-3',
      title: 'Audio recording in note',
      text: 'Show audio in a note and playback UI.',
      colorCode: '#6b6ed0',
    },
  ],
  IN_PROGRESS: [
    {
      id: 'mock-progress-1',
      title: 'Desktop view',
      text: 'PWA for website and native apps. Windows and Mac need unique share icons.',
      colorCode: '#df3035',
    },
    {
      id: 'mock-progress-2',
      title: 'Mobile home screen',
      text: 'Folders, tags, and notes lists are sorted by recent.',
      colorCode: '#327edc',
    },
  ],
  DONE: [
    {
      id: 'mock-done-1',
      title: 'Audio recording',
      text: 'Interface for when recording a new audio note.',
      colorCode: '#0cae96',
    },
    {
      id: 'mock-done-2',
      title: 'Bookmarking',
      text: 'Interface for when creating a new link note.',
      colorCode: '#0cae96',
    },
  ],
}

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
      return
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
    } catch (error) {
      setBoard(null)
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function createBoard(nextPublicId) {
    const trimmedPublicId = nextPublicId.trim()

    if (!trimmedPublicId) {
      return
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

      await loadBoard(data.publicId)
      setMessage('Board created.')
    } catch (error) {
      setBoard(null)
      setMessage(error.message)
      setLoading(false)
    }
  }

  function showLanding() {
    setBoard(null)
    setMessage('')
    window.history.pushState(null, '', '/')
  }

  function handleSubmit(event, action) {
    event.preventDefault()

    if (action === 'create') {
      createBoard(publicId)
      return
    }

    loadBoard(publicId)
  }

  if (!board) {
    return (
      <main className="landing">
        <section className="landing-grid">
          <div className="intro">
            <h1>Kanbab</h1>
            <p>Open a board with a public id, or create a fresh board using the same key.</p>
            <PreviewBoard />
          </div>

          <form className="panel" onSubmit={(event) => handleSubmit(event, 'find')}>
            <h2>Board key</h2>
            <label htmlFor="publicId">Public id</label>
            <input
              id="publicId"
              name="publicId"
              value={publicId}
              onChange={(event) => setPublicId(event.target.value)}
              autoComplete="off"
              placeholder="roadmap-2026"
              minLength="5"
              maxLength="60"
              required
            />
            <div className="actions">
              <button type="button" onClick={(event) => handleSubmit(event, 'create')} disabled={loading}>
                Create
              </button>
              <button type="submit" disabled={loading}>
                Find
              </button>
            </div>
            {message ? <p className="message">{message}</p> : <p className="hint">Spring API is proxied through /api.</p>}
          </form>
        </section>
      </main>
    )
  }

  return (
    <main className="board-page">
      <header className="board-header">
        <div>
          <button className="text-button" type="button" onClick={showLanding}>
            Change board
          </button>
          <h1>{board.publicId}</h1>
        </div>
      </header>

      <section className="board" aria-label="Kanban board">
        {visibleTaskLists.map((taskList) => (
          <article className="column" key={taskList.id}>
            <h2>{taskList.title}</h2>
            <div className="cards">
              {taskList.cards.length ? (
                taskList.cards.map((card) => (
                  <div className="card" style={{ backgroundColor: card.colorCode }} key={card.id}>
                    <h3>{card.title}</h3>
                    <p>{card.text}</p>
                  </div>
                ))
              ) : (
                <div className="empty-card">No cards</div>
              )}
            </div>
          </article>
        ))}
      </section>
    </main>
  )
}

function PreviewBoard() {
  return (
    <div className="preview" aria-hidden="true">
      {taskListOrder.map((type) => (
        <div className="mini-column" key={type}>
          <div className="mini-title" />
          {fallbackCards[type].slice(0, type === 'TO_DO' ? 3 : 2).map((card) => (
            <div className="mini-card" style={{ backgroundColor: card.colorCode }} key={card.id} />
          ))}
        </div>
      ))}
    </div>
  )
}

export default App

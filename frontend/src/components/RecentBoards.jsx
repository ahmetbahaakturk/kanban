import { useState } from 'react'

function RecentBoards({ currentPublicId, items, onSelect }) {
  const [open, setOpen] = useState(false)

  if (!items.length) {
    return null
  }

  return (
    <aside className="recent-boards" aria-label="Recent boards">
      {open ? (
        <div className="recent-board-panel">
          <div className="recent-board-header">
            <h2>Recent</h2>
            <button
              className="recent-board-close"
              type="button"
              aria-label="Close recent boards"
              onClick={() => setOpen(false)}
            >
              x
            </button>
          </div>
          <div className="recent-board-list">
            {items.map((publicId) => (
              <button
                className={publicId === currentPublicId ? 'recent-board active' : 'recent-board'}
                type="button"
                onClick={() => onSelect(publicId)}
                key={publicId}
              >
                {publicId}
              </button>
            ))}
          </div>
        </div>
      ) : null}
      <button
        className="recent-board-toggle"
        type="button"
        aria-expanded={open}
        onClick={() => setOpen((currentOpen) => !currentOpen)}
      >
        Recent
      </button>
    </aside>
  )
}

export default RecentBoards

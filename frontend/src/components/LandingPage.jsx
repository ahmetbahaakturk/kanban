import PreviewBoard from './PreviewBoard'

function LandingPage({ loading, message, onOpenModal }) {
  return (
    <section className="landing-grid">
      <div className="intro">
        <h1>Kanban</h1>
        <p>Create a shared board in seconds, or open one you already use with its board name.</p>
        <PreviewBoard />
      </div>

      <div className="panel">
        <h2>Start with a board</h2>
        <div className="actions">
          <button type="button" onClick={() => onOpenModal('create')} disabled={loading}>
            New board
          </button>
          <button type="button" onClick={() => onOpenModal('find')} disabled={loading}>
            Open board
          </button>
        </div>
        {message ? <p className="message">{message}</p> : <p className="hint">Use a short name you can share and remember.</p>}
      </div>
    </section>
  )
}

export default LandingPage

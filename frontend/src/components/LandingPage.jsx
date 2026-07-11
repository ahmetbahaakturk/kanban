import PreviewBoard from './PreviewBoard'

function LandingPage({ loading, message, onOpenModal }) {
  return (
    <section className="landing-grid">
      <div className="intro">
        <h1>Kanbab</h1>
        <p>Open a board with a public id, or create a fresh board using the same key.</p>
        <PreviewBoard />
      </div>

      <div className="panel">
        <h2>Board key</h2>
        <div className="actions">
          <button type="button" onClick={() => onOpenModal('create')} disabled={loading}>
            Create
          </button>
          <button type="button" onClick={() => onOpenModal('find')} disabled={loading}>
            Find
          </button>
        </div>
        {message ? <p className="message">{message}</p> : <p className="hint">Spring API is proxied through /api.</p>}
      </div>
    </section>
  )
}

export default LandingPage

function CardCreateModal({
  loading,
  message,
  taskList,
  onClose,
  onSubmit,
}) {
  function handleSubmit(event) {
    event.preventDefault()

    const formData = new FormData(event.currentTarget)

    onSubmit({
      title: formData.get('title').trim(),
      text: formData.get('text').trim(),
    })
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <form
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="cardModalTitle"
        onSubmit={handleSubmit}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="modal-close" type="button" onClick={onClose} aria-label="Close">
          x
        </button>
        <h2 id="cardModalTitle">Create card</h2>
        <p className="modal-context">{taskList.title}</p>

        <label htmlFor="cardTitle">Title</label>
        <input
          id="cardTitle"
          name="title"
          autoComplete="off"
          maxLength="150"
          required
          autoFocus
        />

        <label htmlFor="cardText">Text</label>
        <textarea
          id="cardText"
          name="text"
          rows="4"
        />

        <button className="modal-submit" type="submit" disabled={loading}>
          Create
        </button>
        {message ? <p className="message">{message}</p> : null}
      </form>
    </div>
  )
}

export default CardCreateModal

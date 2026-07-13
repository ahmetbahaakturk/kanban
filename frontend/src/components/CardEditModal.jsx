function CardEditModal({
  card,
  loading,
  message,
  onClose,
  onSubmit,
}) {
  function handleSubmit(event) {
    event.preventDefault()

    const formData = new FormData(event.currentTarget)

    onSubmit({
      title: formData.get('title').trim(),
      text: formData.get('text').trim(),
      colorCode: formData.get('colorCode'),
    })
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <form
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="cardEditModalTitle"
        onSubmit={handleSubmit}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="modal-close" type="button" onClick={onClose} aria-label="Close">
          x
        </button>
        <h2 id="cardEditModalTitle">Edit card</h2>

        <label htmlFor="editCardTitle">Title</label>
        <input
          id="editCardTitle"
          name="title"
          autoComplete="off"
          defaultValue={card.title}
          maxLength="150"
          required
          autoFocus
        />

        <label htmlFor="editCardText">Text</label>
        <textarea
          id="editCardText"
          name="text"
          defaultValue={card.text ?? ''}
          rows="4"
        />

        <label htmlFor="editCardColor">Color</label>
        <input
          id="editCardColor"
          className="color-input"
          name="colorCode"
          type="color"
          defaultValue={card.colorCode}
        />

        <button className="modal-submit" type="submit" disabled={loading}>
          Save
        </button>
        {message ? <p className="message">{message}</p> : null}
      </form>
    </div>
  )
}

export default CardEditModal

import { useEffect, useRef } from 'react'

function CardCreateForm({ loading, message, onCancel, onSubmit }) {
  const titleInputRef = useRef(null)

  useEffect(() => {
    titleInputRef.current?.focus()
  }, [])

  function handleSubmit(event) {
    event.preventDefault()

    const formData = new FormData(event.currentTarget)

    onSubmit({
      title: formData.get('title').trim(),
      text: formData.get('text').trim(),
    })
  }

  return (
    <form className="card card-composer" onSubmit={handleSubmit}>
      <input
        ref={titleInputRef}
        name="title"
        autoComplete="off"
        maxLength="150"
        placeholder="Card title"
        required
      />
      <textarea
        name="text"
        placeholder="Card text"
        rows="3"
      />
      <div className="card-composer-actions">
        <button type="submit" disabled={loading}>
          Create
        </button>
        <button type="button" onClick={onCancel} disabled={loading}>
          Cancel
        </button>
      </div>
      {message ? <p className="card-composer-message">{message}</p> : null}
    </form>
  )
}

export default CardCreateForm

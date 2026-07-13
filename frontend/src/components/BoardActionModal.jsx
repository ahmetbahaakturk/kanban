function BoardActionModal({
  loading,
  message,
  mode,
  publicId,
  onClose,
  onPublicIdChange,
  onSubmit,
}) {
  const actionLabel = mode === 'create' ? 'New board' : 'Open board'

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <form
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modalTitle"
        noValidate
        onSubmit={onSubmit}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="modal-close" type="button" onClick={onClose} aria-label="Close">
          x
        </button>
        <h2 id="modalTitle">{actionLabel}</h2>
        <label htmlFor="modalPublicId">Board name</label>
        <input
          id="modalPublicId"
          name="publicId"
          value={publicId}
          onChange={(event) => onPublicIdChange(event.target.value)}
          autoComplete="off"
          placeholder="roadmap-2026"
          minLength="4"
          maxLength="60"
          pattern="[A-Za-z0-9._~-]+"
          title="Use only letters, numbers, dots, dashes, underscores, and tildes."
          required
          autoFocus
        />
        <button className="modal-submit" type="submit" disabled={loading}>
          {mode === 'create' ? 'Create board' : 'Open board'}
        </button>
        {message ? <p className="message">{message}</p> : null}
      </form>
    </div>
  )
}

export default BoardActionModal

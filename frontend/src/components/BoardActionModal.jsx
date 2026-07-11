function BoardActionModal({
  loading,
  message,
  mode,
  publicId,
  onClose,
  onPublicIdChange,
  onSubmit,
}) {
  const actionLabel = mode === 'create' ? 'Create' : 'Find'

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <form
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modalTitle"
        onSubmit={onSubmit}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="modal-close" type="button" onClick={onClose} aria-label="Close">
          x
        </button>
        <h2 id="modalTitle">{actionLabel} board</h2>
        <label htmlFor="modalPublicId">Public id</label>
        <input
          id="modalPublicId"
          name="publicId"
          value={publicId}
          onChange={(event) => onPublicIdChange(event.target.value)}
          autoComplete="off"
          placeholder="roadmap-2026"
          minLength="5"
          maxLength="60"
          required
          autoFocus
        />
        <button className="modal-submit" type="submit" disabled={loading}>
          {actionLabel}
        </button>
        {message ? <p className="message">{message}</p> : null}
      </form>
    </div>
  )
}

export default BoardActionModal

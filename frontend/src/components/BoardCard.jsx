function BoardCard({
  card,
  className = '',
  dragAttributes = {},
  dragListeners = {},
  isDragging = false,
  onDoubleClick,
  setNodeRef,
  style,
}) {
  return (
    <div
      ref={setNodeRef}
      className={`card ${className}`.trim()}
      style={{ backgroundColor: card.colorCode, ...style }}
      {...dragAttributes}
      {...dragListeners}
      aria-pressed={isDragging}
      onDoubleClick={onDoubleClick}
    >
      <h3>{card.title}</h3>
      <p>{card.text}</p>
    </div>
  )
}

export default BoardCard

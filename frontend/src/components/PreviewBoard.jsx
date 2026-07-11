import { fallbackCards, taskListOrder } from '../constants/kanban'

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

export default PreviewBoard

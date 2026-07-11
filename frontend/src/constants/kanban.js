export const taskListLabels = {
  BACKLOG: 'Backlog',
  TO_DO: 'To do',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
}

export const taskListOrder = ['BACKLOG', 'TO_DO', 'IN_PROGRESS', 'DONE']

export const fallbackCards = {
  BACKLOG: [
    {
      id: 'mock-backlog-1',
      title: 'Twilio integration',
      text: 'Create new note via SMS. Support text, audio, links, and media.',
      colorCode: '#c742a7',
    },
    {
      id: 'mock-backlog-2',
      title: 'Markdown support',
      text: 'Markdown shorthand converts to formatting.',
      colorCode: '#6b6ed0',
    },
  ],
  TO_DO: [
    {
      id: 'mock-todo-1',
      title: 'Tablet view',
      text: 'Layout pass for medium screens.',
      colorCode: '#df3035',
    },
    {
      id: 'mock-todo-2',
      title: 'Mobile view',
      text: 'Functions for both web responsive and native apps.',
      colorCode: '#df3035',
    },
    {
      id: 'mock-todo-3',
      title: 'Audio recording in note',
      text: 'Show audio in a note and playback UI.',
      colorCode: '#6b6ed0',
    },
  ],
  IN_PROGRESS: [
    {
      id: 'mock-progress-1',
      title: 'Desktop view',
      text: 'PWA for website and native apps. Windows and Mac need unique share icons.',
      colorCode: '#df3035',
    },
    {
      id: 'mock-progress-2',
      title: 'Mobile home screen',
      text: 'Folders, tags, and notes lists are sorted by recent.',
      colorCode: '#327edc',
    },
  ],
  DONE: [
    {
      id: 'mock-done-1',
      title: 'Audio recording',
      text: 'Interface for when recording a new audio note.',
      colorCode: '#0cae96',
    },
    {
      id: 'mock-done-2',
      title: 'Bookmarking',
      text: 'Interface for when creating a new link note.',
      colorCode: '#0cae96',
    },
  ],
}

const extensionToLanguage: Record<string, string> = {
  '.md': 'markdown',
  '.markdown': 'markdown',
  '.json': 'json',
  '.js': 'javascript',
  '.jsx': 'javascript',
  '.ts': 'typescript',
  '.tsx': 'typescript',
  '.py': 'python',
  '.yaml': 'yaml',
  '.yml': 'yaml',
  '.xml': 'xml',
  '.html': 'html',
  '.css': 'css',
  '.sh': 'shell',
  '.bash': 'shell',
  '.sql': 'sql',
  '.java': 'java',
  '.go': 'go',
  '.rs': 'rust',
  '.toml': 'ini',
  '.txt': 'plaintext',
}

export function getLanguageFromFilename(filename: string): string {
  if (!filename) return 'plaintext'
  const lower = filename.toLowerCase()
  for (const [ext, lang] of Object.entries(extensionToLanguage)) {
    if (lower.endsWith(ext)) return lang
  }
  return 'plaintext'
}

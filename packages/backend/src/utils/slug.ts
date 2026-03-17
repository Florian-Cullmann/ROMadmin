export function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

export function fileNameToDisplayName(fileName: string): string {
  // Remove file extension
  const name = fileName.replace(/\.[^.]+$/, '');
  // Replace underscores, dots, and dashes with spaces
  return name
    .replace(/[_.\-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    // Title case
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

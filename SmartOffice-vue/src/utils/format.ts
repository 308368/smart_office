// 日期格式化工具

/**
 * 格式化日期为 yyyy-MM-dd HH:mm 格式
 * @param dateStr 后端返回的日期字符串，如 "2026-03-14T08:17:40"
 */
export const formatDateTime = (dateStr: string | undefined | null): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化日期为 yyyy-MM-dd 格式
 * @param dateStr 后端返回的日期字符串，如 "2026-03-14T08:17:40"
 */
export const formatDate = (dateStr: string | undefined | null): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
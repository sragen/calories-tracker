"use client"

export function saveTokenToCookie(token: string) {
  document.cookie = `access_token=${token}; path=/; max-age=${60 * 60 * 24}`
  localStorage.setItem("access_token", token)
}

export function clearAuthCookie() {
  document.cookie = "access_token=; path=/; max-age=0"
  localStorage.removeItem("access_token")
}

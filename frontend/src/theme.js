export const THEME_STORAGE_KEY = "theme";

export function getSystemTheme() {
  if (typeof window === "undefined" || !window.matchMedia) {
    return "light";
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

export function getStoredTheme() {
  try {
    const value = localStorage.getItem(THEME_STORAGE_KEY);
    return value === "dark" || value === "light" ? value : null;
  } catch {
    return null;
  }
}

export function resolveInitialTheme() {
  return getStoredTheme() ?? getSystemTheme();
}

export function applyTheme(theme) {
  const root = document.documentElement;
  const isDark = theme === "dark";

  root.classList.toggle("dark", isDark);
  root.dataset.theme = theme;
  root.style.colorScheme = theme;

  try {
    localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    // Ignore storage failures (private mode, etc.)
  }

  const metaTheme = document.querySelector('meta[name="theme-color"]');
  if (metaTheme) {
    metaTheme.setAttribute("content", isDark ? "#000000" : "#f4f4f5");
  }

  window.dispatchEvent(
    new CustomEvent("themechange", {
      detail: { theme, isDark },
    })
  );
}

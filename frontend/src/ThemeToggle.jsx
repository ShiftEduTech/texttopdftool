import { useCallback, useEffect, useId, useState } from "react";
import { Moon, Sun } from "lucide-react";
import { applyTheme, resolveInitialTheme } from "./theme";

/**
 * Premium SaaS-style theme toggle (Sun / Moon).
 * Persists preference, respects system theme on first visit,
 * and toggles the `dark` class on the document root.
 */
export default function ThemeToggle() {
  const labelId = useId();
  const [theme, setTheme] = useState(() => resolveInitialTheme());
  const [pressed, setPressed] = useState(false);
  const isDark = theme === "dark";

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setPressed(true);
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
    window.setTimeout(() => setPressed(false), 220);
  }, []);

  const onKeyDown = useCallback(
    (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        toggleTheme();
      }
    },
    [toggleTheme]
  );

  const nextLabel = isDark ? "Switch to light mode" : "Switch to dark mode";

  return (
    <button
      type="button"
      onClick={toggleTheme}
      onKeyDown={onKeyDown}
      aria-labelledby={labelId}
      aria-label={nextLabel}
      aria-pressed={isDark}
      title={nextLabel}
      className={[
        "group relative inline-flex h-10 w-10 items-center justify-center",
        "rounded-full border border-slate-200 bg-white text-slate-700 shadow-sm",
        "backdrop-blur-md transition-all duration-280 ease-out",
        "hover:-translate-y-0.5 hover:border-blue-300 hover:bg-white hover:shadow-md",
        "active:translate-y-0 active:scale-95",
        "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/60 focus-visible:ring-offset-2 focus-visible:ring-offset-white",
        "dark:border-white/10 dark:bg-slate-900/80 dark:text-slate-100 dark:shadow-black/30",
        "dark:hover:border-blue-400/40 dark:hover:bg-slate-800/90",
        "dark:focus-visible:ring-blue-400/70 dark:focus-visible:ring-offset-[#0F172A]",
        pressed ? "animate-theme-pop" : "",
      ].join(" ")}
    >
      <span id={labelId} className="sr-only">
        {nextLabel}
      </span>

      <span className="relative h-5 w-5" aria-hidden="true">
        <Sun
          strokeWidth={2}
          className={[
            "absolute inset-0 h-5 w-5 transition-all duration-280 ease-out",
            isDark
              ? "rotate-90 scale-50 opacity-0"
              : "rotate-0 scale-100 opacity-100",
          ].join(" ")}
        />
        <Moon
          strokeWidth={2}
          className={[
            "absolute inset-0 h-5 w-5 transition-all duration-280 ease-out",
            isDark
              ? "rotate-0 scale-100 opacity-100"
              : "-rotate-90 scale-50 opacity-0",
          ].join(" ")}
        />
      </span>
    </button>
  );
}

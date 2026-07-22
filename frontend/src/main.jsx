import { createRoot } from "react-dom/client";
import ThemeToggle from "./ThemeToggle";
import { applyTheme, resolveInitialTheme } from "./theme";
import "./index.css";

// Ensure theme is applied even if the FOUC script was skipped.
applyTheme(resolveInitialTheme());

const mountNode = document.getElementById("theme-root");
if (mountNode) {
  createRoot(mountNode).render(<ThemeToggle />);
}

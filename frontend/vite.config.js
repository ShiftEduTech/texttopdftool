import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig({
  plugins: [react()],
  define: {
    "process.env.NODE_ENV": JSON.stringify("production"),
  },
  build: {
    outDir: path.resolve(__dirname, "../src/main/resources/static/theme"),
    emptyOutDir: true,
    cssCodeSplit: false,
    lib: {
      entry: path.resolve(__dirname, "src/main.jsx"),
      name: "ShiftThemeToggle",
      formats: ["iife"],
      fileName: () => "theme-toggle.js",
    },
    rollupOptions: {
      output: {
        assetFileNames: "theme-toggle[extname]",
      },
    },
  },
});

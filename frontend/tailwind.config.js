/** @type {import('tailwindcss').Config} */
export default {
  content: ["./src/**/*.{js,jsx}"],
  darkMode: "class",
  theme: {
    extend: {
      transitionDuration: {
        280: "280ms",
      },
      keyframes: {
        "theme-pop": {
          "0%": { transform: "scale(1)" },
          "50%": { transform: "scale(0.92)" },
          "100%": { transform: "scale(1)" },
        },
      },
      animation: {
        "theme-pop": "theme-pop 220ms ease-out",
      },
    },
  },
  plugins: [],
};

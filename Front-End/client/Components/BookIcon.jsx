import * as React from "react";

// By: lucide
// See: https://v0.app/icon/lucide/book-open-text
// Example: <IconLucideBookOpenText width="24px" height="24px" style={{color: "#000000"}} />
// Cited: https://www.v0.app/icon/lucide/book-open-text

export const IconLucideBookOpenText = ({
  height = "1em",
  strokeWidth = "2",
  fill = "none",
  focusable = "false",
  ...props
}) => (
  <svg
    role="img"
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    height={height}
    focusable={focusable}
    {...props}
  >
    <path
      fill={fill}
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={strokeWidth}
      d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2zm20 0h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7zM6 8h2m-2 4h2m8-4h2m-2 4h2"
    />
  </svg>
);

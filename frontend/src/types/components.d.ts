/* eslint-disable */
/* prettier-ignore */
// Wot UI easycom component shims. Use `any` to avoid vue-tsc checking third-party .vue internals.
export {}

declare module 'vue' {
  export interface GlobalComponents {
    WdBadge: any
    WdButton: any
    WdIcon: any
    WdPopup: any
  }
}

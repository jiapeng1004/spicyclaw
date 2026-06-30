import { createRequire } from 'node:module'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const require = createRequire(import.meta.url)
const __dirname = path.dirname(fileURLToPath(import.meta.url))

/** 通过 workspace 依赖 @spicyclaw/web 解析 Web 产物路径（非相对路径拷贝）。 */
export function resolveWebPackageRoot() {
  const pkgJson = require.resolve('@spicyclaw/web/package.json')
  return path.dirname(pkgJson)
}

export function resolveWebDistIndex() {
  return path.join(resolveWebPackageRoot(), 'dist/index.html')
}

/** electron-builder 打包后 dist 位于 extraResources/web/dist */
export function resolvePackagedWebDistIndex(resourcesPath) {
  return path.join(resourcesPath, 'web/dist/index.html')
}

export function webDevServerUrl() {
  return process.env.SPICYCLAW_WEB_DEV_URL ?? 'http://localhost:5173'
}

export function resolvePreloadPath() {
  return path.join(__dirname, 'preload.mjs')
}

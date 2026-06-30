import { app, BrowserWindow, shell } from 'electron'
import fs from 'node:fs'
import {
  resolvePackagedWebDistIndex,
  resolvePreloadPath,
  resolveWebDistIndex,
  webDevServerUrl,
} from './resolve-web.mjs'

const isDev = process.env.SPICYCLAW_DEV === '1'

function resolveProductionIndex() {
  if (app.isPackaged) {
    const packaged = resolvePackagedWebDistIndex(process.resourcesPath)
    if (fs.existsSync(packaged)) return packaged
  }
  return resolveWebDistIndex()
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 860,
    minWidth: 960,
    minHeight: 640,
    title: 'SpicyClaw',
    webPreferences: {
      preload: resolvePreloadPath(),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  })

  win.webContents.setWindowOpenHandler(({ url }) => {
    void shell.openExternal(url)
    return { action: 'deny' }
  })

  if (isDev) {
    void win.loadURL(webDevServerUrl())
    win.webContents.openDevTools({ mode: 'detach' })
  } else {
    void win.loadFile(resolveProductionIndex())
  }
}

app.whenReady().then(() => {
  createWindow()
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

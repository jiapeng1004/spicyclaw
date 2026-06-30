import { contextBridge } from 'electron'

contextBridge.exposeInMainWorld('spicyclawDesktop', {
  platform: process.platform,
  isDesktop: true,
})

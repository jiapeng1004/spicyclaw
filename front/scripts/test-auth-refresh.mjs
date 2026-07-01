import { chromium } from 'playwright'

const base = process.env.BASE_URL ?? 'http://localhost:5173'

async function main() {
  const browser = await chromium.launch()
  const page = await browser.newPage()

  page.on('console', (msg) => console.log('[console]', msg.text()))
  page.on('pageerror', (err) => console.log('[pageerror]', err.message))

  await page.goto(base, { waitUntil: 'networkidle' })

  const hasLogin = await page.locator('button.submit').isVisible().catch(() => false)
  console.log('initial login visible:', hasLogin)

  page.on('response', async (res) => {
    if (res.url().includes('/auth/login') && res.status() === 200) {
      const token = await page.evaluate(() => localStorage.getItem('spicyclaw-access-token'))
      console.log('token right after login response:', token ? `${token.slice(0, 24)}...` : null)
    }
    if (res.url().includes('/api/') || res.url().includes('/auth/')) {
      let body = ''
      try {
        body = await res.text()
      } catch {
        body = '<unreadable>'
      }
      console.log('[response]', res.status(), res.url(), body.slice(0, 120))
    }
  })

  if (hasLogin) {
    await page.fill('input[autocomplete="username"]', 'admin')
    await page.fill('input[type="password"]', 'spicyclaw')
    await page.click('button.submit')
    await page.waitForTimeout(500)
    const tokenImmediate = await page.evaluate(() => localStorage.getItem('spicyclaw-access-token'))
    console.log('token 500ms after login click:', tokenImmediate ? `${tokenImmediate.slice(0, 20)}...` : null)
    try {
      await page.waitForSelector('.shell', { timeout: 15000 })
      console.log('login succeeded, shell visible')
    } catch (e) {
      console.log('login failed, shell not visible:', e.message)
    }
  }

  const tokenBefore = await page.evaluate(() => localStorage.getItem('spicyclaw-access-token'))
  console.log('token before reload:', tokenBefore ? `${tokenBefore.slice(0, 20)}...` : null)

  await page.reload({ waitUntil: 'networkidle' })

  const tokenAfter = await page.evaluate(() => localStorage.getItem('spicyclaw-access-token'))
  const loginAfter = await page.locator('button.submit').isVisible().catch(() => false)
  const shellAfter = await page.locator('.shell').isVisible().catch(() => false)

  console.log('token after reload:', tokenAfter ? `${tokenAfter.slice(0, 20)}...` : null)
  console.log('login visible after reload:', loginAfter)
  console.log('shell visible after reload:', shellAfter)

  const meRequests = []
  page.on('request', (req) => {
    if (req.url().includes('/auth/me')) {
      meRequests.push({
        url: req.url(),
        auth: req.headers()['authorization'] ?? req.headers()['Authorization'] ?? null,
      })
    }
  })

  await browser.close()

  if (loginAfter) {
    process.exitCode = 1
    console.error('FAIL: still on login page after refresh')
  } else if (shellAfter) {
    console.log('PASS: stayed logged in after refresh')
  }
}

main().catch((e) => {
  console.error(e)
  process.exitCode = 1
})

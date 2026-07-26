import { test, expect } from '@playwright/test'

test.describe('Auth flows (mocked API)', () => {
  test('login page is visible', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByText('Sign in to FlagForge')).toBeVisible()
    await expect(page.getByLabel('Email')).toBeVisible()
    await expect(page.getByLabel('Password')).toBeVisible()
  })

  test('successful login navigates to flags', async ({ page }) => {
    await page.route('**/api/v1/**', async (route) => {
      const url = route.request().url()
      const method = route.request().method()

      if (url.includes('/auth/login') && method === 'POST') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            userId: '11111111-1111-1111-1111-111111111111',
            username: 'admin',
            email: 'admin@flagforge.local',
            role: 'ADMIN',
            accessToken: 'e2e-test-token',
            tokenType: 'Bearer',
            expiresInMs: 86400000,
          }),
        })
        return
      }

      if (url.includes('/auth/me') && method === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: '11111111-1111-1111-1111-111111111111',
            username: 'admin',
            email: 'admin@flagforge.local',
            role: 'ADMIN',
          }),
        })
        return
      }

      if (url.includes('/flags') && method === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ items: [], nextCursor: null, hasMore: false }),
        })
        return
      }

      await route.fulfill({ status: 404, body: 'not mocked' })
    })

    await page.goto('/login')
    await page.getByLabel('Email').fill('admin@flagforge.local')
    await page.getByLabel('Password').fill('password123')
    await page.getByRole('button', { name: /sign in/i }).click()

    await expect(page).toHaveURL(/\/flags/)
    await expect(page.getByText('Feature Flags')).toBeVisible()
    await expect(page.getByText('admin', { exact: true })).toBeVisible()
    await expect(page.getByText('ADMIN', { exact: true })).toBeVisible()
  })

  test('shows API error on failed login', async ({ page }) => {
    await page.route('**/api/v1/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 401,
          message: 'Invalid email or password',
        }),
      })
    })
    // Bootstrap /me may not be called without token
    await page.route('**/api/v1/auth/me', async (route) => {
      await route.fulfill({ status: 401, body: '{}' })
    })

    await page.goto('/login')
    await page.getByLabel('Email').fill('bad@flagforge.local')
    await page.getByLabel('Password').fill('wrongpass1')
    await page.getByRole('button', { name: /sign in/i }).click()

    await expect(page.getByText('Invalid email or password')).toBeVisible()
    await expect(page).toHaveURL(/\/login/)
  })

  test('register page link works', async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('link', { name: /create one/i }).click()
    await expect(page).toHaveURL(/\/register/)
    await expect(page.getByText('Create your account')).toBeVisible()
  })
})

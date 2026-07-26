import { test, expect } from '@playwright/test'

async function mockAuthenticatedSession(page: import('@playwright/test').Page) {
  await page.addInitScript(() => {
    localStorage.setItem('flagforge_token', 'e2e-test-token')
  })

  await page.route('**/api/v1/**', async (route) => {
    const url = route.request().url()
    const method = route.request().method()

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

    if (url.includes('/flags') && method === 'GET' && !url.match(/\/flags\/[^/]+/)) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          items: [
            {
              id: '22222222-2222-2222-2222-222222222222',
              key: 'dark-mode',
              name: 'Dark Mode',
              description: null,
              enabled: true,
              status: 'ACTIVE',
              flagType: 'BOOLEAN',
              environment: 'DEVELOPMENT',
              defaultValue: 'false',
              percentage: null,
              rulesJson: null,
              createdBy: '11111111-1111-1111-1111-111111111111',
              createdAt: '2026-01-01T00:00:00Z',
              updatedAt: '2026-01-01T00:00:00Z',
              version: 1,
            },
          ],
          nextCursor: null,
          hasMore: false,
        }),
      })
      return
    }

    await route.fulfill({ status: 404, body: 'not mocked' })
  })
}

test.describe('Flags page (mocked API)', () => {
  test('lists flags for authenticated admin', async ({ page }) => {
    await mockAuthenticatedSession(page)
    await page.goto('/flags')
    await expect(page.getByText('Feature Flags')).toBeVisible()
    await expect(page.getByText('dark-mode')).toBeVisible()
    await expect(page.getByText('Dark Mode')).toBeVisible()
    await expect(page.getByRole('button', { name: /new flag/i })).toBeVisible()
  })
})

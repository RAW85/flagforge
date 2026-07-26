/** Email/password login → JWT. Redirects to /flags when already authenticated. */
import { useEffect } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { authActions, selectIsAuthenticated } from '@/features/auth/authSlice'
import { Card } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(1),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const dispatch = useAppDispatch()
  const isAuthenticated = useAppSelector(selectIsAuthenticated)
  const { loading, error } = useAppSelector((s) => s.auth)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
  })

  useEffect(() => {
    dispatch(authActions.clearAuthError())
  }, [dispatch])

  if (isAuthenticated) {
    return <Navigate to="/flags" replace />
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <Card className="w-full max-w-md" title="Sign in to FlagForge">
        <p className="mb-5 text-left text-sm text-slate-400">
          Manage feature flags, evaluate rollouts, and control progressive releases.
        </p>
        <form
          className="space-y-4"
          onSubmit={handleSubmit((values) => dispatch(authActions.loginRequest(values)))}
        >
          <Input
            label="Email"
            type="email"
            autoComplete="email"
            error={errors.email?.message}
            {...register('email')}
          />
          <Input
            label="Password"
            type="password"
            autoComplete="current-password"
            error={errors.password?.message}
            {...register('password')}
          />
          {error && (
            <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
              {error}
            </div>
          )}
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>
        <p className="mt-4 text-center text-sm text-slate-400">
          No account?{' '}
          <Link className="text-indigo-300 hover:text-indigo-200" to="/register">
            Create one
          </Link>
        </p>
      </Card>
    </div>
  )
}

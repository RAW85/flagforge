/** New account signup. First registered user becomes ADMIN on the backend. */
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
  username: z
    .string()
    .min(3)
    .max(100)
    .regex(/^[a-zA-Z0-9._-]+$/, 'Letters, digits, ., _, - only'),
  email: z.string().email(),
  password: z.string().min(8, 'At least 8 characters'),
})

type FormValues = z.infer<typeof schema>

export function RegisterPage() {
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
      <Card className="w-full max-w-md" title="Create your account">
        <p className="mb-5 text-left text-sm text-slate-400">
          The first registered user becomes <span className="text-indigo-300">ADMIN</span>.
        </p>
        <form
          className="space-y-4"
          onSubmit={handleSubmit((values) => dispatch(authActions.registerRequest(values)))}
        >
          <Input
            label="Username"
            error={errors.username?.message}
            {...register('username')}
          />
          <Input
            label="Email"
            type="email"
            error={errors.email?.message}
            {...register('email')}
          />
          <Input
            label="Password"
            type="password"
            error={errors.password?.message}
            {...register('password')}
          />
          {error && (
            <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
              {error}
            </div>
          )}
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creating…' : 'Create account'}
          </Button>
        </form>
        <p className="mt-4 text-center text-sm text-slate-400">
          Already have an account?{' '}
          <Link className="text-indigo-300 hover:text-indigo-200" to="/login">
            Sign in
          </Link>
        </p>
      </Card>
    </div>
  )
}

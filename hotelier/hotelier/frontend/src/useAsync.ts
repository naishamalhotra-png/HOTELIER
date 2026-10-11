import { useCallback, useEffect, useRef, useState } from 'react'

/** Loads data from the backend and exposes loading / error / reload. */
export function useAsync<T>(loader: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const counter = useRef(0)

  const load = useCallback(() => {
    const id = ++counter.current
    setLoading(true)
    setError(null)
    loader()
      .then((result) => { if (id === counter.current) setData(result) })
      .catch((e: Error) => { if (id === counter.current) setError(e.message) })
      .finally(() => { if (id === counter.current) setLoading(false) })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => { load() }, [load])
  return { data, loading, error, reload: load, setData }
}

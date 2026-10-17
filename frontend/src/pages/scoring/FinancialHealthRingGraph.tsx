import { useId } from 'react'

type FinancialHealthRing = {
  label: string
  value: number
  color: string
  radius: number
}

type FinancialHealthRingGraphProps = {
  score: number
  band: string
  rings: readonly FinancialHealthRing[]
}

const RING_LEGEND = [
  'Gold · Foundation',
  'Silver · Controls',
  'Bronze · Future',
] as const

export default function FinancialHealthRingGraph({ score, band, rings }: FinancialHealthRingGraphProps) {
  const graphId = useId().replace(/:/g, '')
  const gradients = [`${graphId}-gold`, `${graphId}-silver`, `${graphId}-bronze`]

  return (
    <figure
      className="financial-health-ring-figure"
      aria-label={`Financial Health score ${score} out of 1000, rated ${band}`}
    >
      <div className="financial-health-ring-visual">
        <svg viewBox="0 0 184 184" aria-hidden="true">
          <defs>
            <linearGradient id={gradients[0]} x1="22" y1="20" x2="162" y2="164" gradientUnits="userSpaceOnUse">
              <stop offset="0" stopColor="#7b5008" />
              <stop offset="0.24" stopColor="#f4c54f" />
              <stop offset="0.43" stopColor="#ffe99a" />
              <stop offset="0.68" stopColor="#d69a1e" />
              <stop offset="1" stopColor="#845508" />
            </linearGradient>
            <linearGradient id={gradients[1]} x1="22" y1="20" x2="162" y2="164" gradientUnits="userSpaceOnUse">
              <stop offset="0" stopColor="#656b73" />
              <stop offset="0.24" stopColor="#d5d9de" />
              <stop offset="0.43" stopColor="#f7f8f9" />
              <stop offset="0.68" stopColor="#adb3ba" />
              <stop offset="1" stopColor="#686e76" />
            </linearGradient>
            <linearGradient id={gradients[2]} x1="22" y1="20" x2="162" y2="164" gradientUnits="userSpaceOnUse">
              <stop offset="0" stopColor="#663213" />
              <stop offset="0.24" stopColor="#c67a3b" />
              <stop offset="0.43" stopColor="#f0ad70" />
              <stop offset="0.68" stopColor="#a95726" />
              <stop offset="1" stopColor="#653012" />
            </linearGradient>
          </defs>
          {rings.map((ring, index) => (
            <g key={ring.label} transform="rotate(-90 92 92)">
              <circle className="financial-health-ring-track" cx="92" cy="92" r={ring.radius} pathLength="100" />
              <circle
                className="financial-health-ring-progress"
                cx="92"
                cy="92"
                r={ring.radius}
                pathLength="100"
                stroke={`url(#${gradients[index]})`}
                strokeDasharray={`${ring.value} ${100 - ring.value}`}
              />
            </g>
          ))}
        </svg>
        <div className="financial-health-ring-score">
          <strong>{score}</strong>
          <span>/ 1000</span>
        </div>
      </div>
      <figcaption className="financial-health-ring-legend">
        {rings.map((ring, index) => (
          <span key={ring.label}>
            <i style={{ background: ring.color, boxShadow: `0 0 8px ${ring.color}` }} aria-hidden="true" />
            {RING_LEGEND[index]}
          </span>
        ))}
      </figcaption>
    </figure>
  )
}
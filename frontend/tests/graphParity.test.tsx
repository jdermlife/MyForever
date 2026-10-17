import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import BudgetUtilizationGraph from '../src/pages/scoring/BudgetUtilizationGraph'
import CreditHealthScoreGraph from '../src/pages/scoring/CreditHealthScoreGraph'
import FinancialHealthRingGraph from '../src/pages/scoring/FinancialHealthRingGraph'
import WealthBalanceRadar from '../src/pages/scoring/WealthBalanceRadar'

describe('In-a-Glance source graph parity', () => {
  it('renders the Financial Summary three-ring model', () => {
    const { container } = render(
      <FinancialHealthRingGraph
        score={742}
        band="Good"
        rings={[
          { label: 'Foundation', value: 74, color: '#e0ad2f', radius: 78 },
          { label: 'Control', value: 68, color: '#d2d6db', radius: 61 },
          { label: 'Future', value: 81, color: '#c57b3c', radius: 44 },
        ]}
      />,
    )

    expect(container.querySelectorAll('.financial-health-ring-progress')).toHaveLength(3)
    expect(screen.getByText('742')).toBeTruthy()
  })

  it('renders all five Lending Scorecard credit rings', () => {
    const { container } = render(
      <CreditHealthScoreGraph
        scores={{ credit: 720, creditBureau: 700, nonStarter: 680, social: 740, psychometric: 760 }}
      />,
    )

    expect(container.querySelectorAll('[data-score-ring]')).toHaveLength(5)
    expect(screen.getByText('Credit Bureau Score')).toBeTruthy()
  })

  it('renders the four Net Worth Building radar dimensions', () => {
    const { container } = render(
      <WealthBalanceRadar
        netWorthPositioning={680}
        wealthBehaviour={72}
        wealthFoundation={760}
        wealthAuthenticity={84}
      />,
    )

    expect(container.querySelectorAll('[data-radar-axis]')).toHaveLength(4)
    expect(screen.getByText('Wealth Authenticity')).toBeTruthy()
  })

  it('renders Budget Tracker utilization and overflow values', () => {
    const { container } = render(
      <BudgetUtilizationGraph
        value={{
          budgetAmount: 1000,
          utilizedAmount: 1250,
          utilizationPercent: 125,
          progressPercent: 100,
          overflowPercent: 25,
          remainingAmount: 0,
          sourceLabel: 'from entered actuals',
        }}
        formatAmount={(amount) => `PHP ${amount}`}
      />,
    )

    expect(screen.getByText('125.0%')).toBeTruthy()
    expect(container.querySelector('.budget-utilization-overflow')).toBeTruthy()
    expect(screen.getByText(/PHP 250 over budget/)).toBeTruthy()
  })
})
export type BudgetUtilizationGraphValue = {
  budgetAmount: number
  utilizedAmount: number
  utilizationPercent: number
  progressPercent: number
  overflowPercent: number
  remainingAmount: number
  sourceLabel: string
}

type BudgetUtilizationGraphProps = {
  value: BudgetUtilizationGraphValue
  formatAmount: (amount: number) => string
}

export default function BudgetUtilizationGraph({ value, formatAmount }: BudgetUtilizationGraphProps) {
  return (
    <div className="budget-utilization-graph" aria-labelledby="budget-utilization-title">
      <div className="budget-utilization-heading">
        <div>
          <span>Budget Performance</span>
          <h2 id="budget-utilization-title">Budget Utilization</h2>
        </div>
        <strong>{value.utilizationPercent.toFixed(1)}%</strong>
      </div>
      <div
        className="budget-utilization-track"
        role="progressbar"
        aria-label="Budget utilization"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(value.progressPercent)}
        aria-valuetext={`${value.utilizationPercent.toFixed(1)}% utilized, ${formatAmount(value.utilizedAmount)} of ${formatAmount(value.budgetAmount)} budget`}
      >
        <div className="budget-utilization-budget-shadow" />
        <div className="budget-utilization-progress" style={{ width: `${value.progressPercent}%` }} />
      </div>
      {value.overflowPercent > 0 ? (
        <div className="budget-utilization-overflow" aria-label={`${value.overflowPercent.toFixed(1)}% over budget`}>
          <span style={{ width: `${value.overflowPercent}%` }} />
        </div>
      ) : null}
      <div className="budget-utilization-legend">
        <span><i className="budget-utilization-swatch budget-utilization-swatch-used" />Utilized {formatAmount(value.utilizedAmount)}</span>
        <span><i className="budget-utilization-swatch budget-utilization-swatch-budget" />Budget {formatAmount(value.budgetAmount)}</span>
      </div>
      <small>
        {value.utilizationPercent > 100
          ? `${formatAmount(value.utilizedAmount - value.budgetAmount)} over budget`
          : `${formatAmount(value.remainingAmount)} remaining`}
        {` ${value.sourceLabel}`}
      </small>
    </div>
  )
}
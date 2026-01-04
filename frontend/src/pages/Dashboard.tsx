import { useQuery } from '@tanstack/react-query'
import { TrendingUp, DollarSign, Users, AlertTriangle, CheckCircle, Clock } from 'lucide-react'
import { api } from '@/lib/api'

export function Dashboard() {
  const { data: cycles } = useQuery({
    queryKey: ['cycles'],
    queryFn: api.getCycles,
  })

  const activeCycles = cycles?.filter(c => c.status === 'ACTIVE') || []

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-ops-text mb-2">Operations Command Center</h1>
        <p className="text-ops-muted">Real-time allocation status and system overview</p>
      </div>

      {/* Status Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatusCard
          title="Active Cycles"
          value={activeCycles.length}
          icon={Clock}
          color="accent"
        />
        <StatusCard
          title="Pending Requests"
          value={142}
          icon={AlertTriangle}
          color="warning"
        />
        <StatusCard
          title="Approved This Month"
          value={89}
          icon={CheckCircle}
          color="success"
        />
        <StatusCard
          title="Budget Allocated"
          value="$1.2M"
          icon={DollarSign}
          color="accent"
        />
      </div>

      {/* Active Cycles */}
      <div className="bg-ops-surface rounded-lg border border-ops-border p-6">
        <h2 className="text-xl font-bold text-ops-text mb-4">Active Allocation Cycles</h2>
        
        {activeCycles.length === 0 ? (
          <div className="text-center py-12 text-ops-muted">
            <Calendar className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>No active cycles. Create one to get started.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {activeCycles.map(cycle => (
              <div key={cycle.id} className="p-4 bg-ops-bg rounded-lg border border-ops-border">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="font-semibold text-ops-text">{cycle.name}</h3>
                  <span className="px-3 py-1 bg-ops-accent/20 text-ops-accent text-sm rounded-full">
                    Active
                  </span>
                </div>
                <p className="text-sm text-ops-muted mb-3">{cycle.description}</p>
                <div className="flex items-center space-x-6 text-sm">
                  <div className="flex items-center space-x-2 text-ops-muted">
                    <Calendar className="h-4 w-4" />
                    <span>{new Date(cycle.startDate).toLocaleDateString()} - {new Date(cycle.endDate).toLocaleDateString()}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Readiness Indicators */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
        <ReadinessCard
          title="Budget Pools"
          status="ready"
          message="All pools configured"
        />
        <ReadinessCard
          title="Resource Pools"
          status="ready"
          message="5 pools available"
        />
        <ReadinessCard
          title="Allocation Engine"
          status="ready"
          message="Online and responsive"
        />
      </div>
    </div>
  )
}

function StatusCard({ title, value, icon: Icon, color }: {
  title: string
  value: string | number
  icon: any
  color: 'accent' | 'success' | 'warning' | 'error'
}) {
  const colorClasses = {
    accent: 'text-ops-accent bg-ops-accent/10',
    success: 'text-ops-success bg-ops-success/10',
    warning: 'text-ops-warning bg-ops-warning/10',
    error: 'text-ops-error bg-ops-error/10',
  }

  return (
    <div className="bg-ops-surface rounded-lg border border-ops-border p-6">
      <div className="flex items-center justify-between mb-4">
        <div className={`p-3 rounded-lg ${colorClasses[color]}`}>
          <Icon className="h-6 w-6" />
        </div>
      </div>
      <h3 className="text-3xl font-bold text-ops-text mb-1">{value}</h3>
      <p className="text-sm text-ops-muted">{title}</p>
    </div>
  )
}

function ReadinessCard({ title, status, message }: {
  title: string
  status: 'ready' | 'warning' | 'error'
  message: string
}) {
  const statusConfig = {
    ready: { color: 'text-ops-success', bg: 'bg-ops-success', icon: CheckCircle },
    warning: { color: 'text-ops-warning', bg: 'bg-ops-warning', icon: AlertTriangle },
    error: { color: 'text-ops-error', bg: 'bg-ops-error', icon: AlertTriangle },
  }

  const config = statusConfig[status]
  const Icon = config.icon

  return (
    <div className="bg-ops-surface rounded-lg border border-ops-border p-6">
      <div className="flex items-center space-x-3 mb-3">
        <div className={`h-2 w-2 ${config.bg} rounded-full animate-pulse`} />
        <h3 className="font-semibold text-ops-text">{title}</h3>
      </div>
      <div className="flex items-start space-x-2">
        <Icon className={`h-5 w-5 ${config.color} mt-0.5`} />
        <p className="text-sm text-ops-muted">{message}</p>
      </div>
    </div>
  )
}

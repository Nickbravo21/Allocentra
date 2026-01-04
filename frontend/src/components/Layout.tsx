import { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { 
  LayoutDashboard, 
  ListChecks, 
  Calendar, 
  Play, 
  BarChart3, 
  FlaskConical, 
  FileText,
  Activity 
} from 'lucide-react'

interface LayoutProps {
  children: ReactNode
}

export function Layout({ children }: LayoutProps) {
  const location = useLocation()

  const navigation = [
    { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
    { name: 'Cycles', href: '/cycles', icon: Calendar },
    { name: 'Requests', href: '/requests', icon: ListChecks },
    { name: 'Run Allocation', href: '/run', icon: Play },
    { name: 'Results', href: '/results', icon: BarChart3 },
    { name: 'Scenario Lab', href: '/scenarios', icon: FlaskConical },
    { name: 'Audit Log', href: '/audit', icon: FileText },
  ]

  const isActive = (path: string) => location.pathname === path

  return (
    <div className="min-h-screen flex bg-ops-bg">
      {/* Sidebar */}
      <aside className="w-64 bg-ops-surface border-r border-ops-border flex flex-col">
        {/* Logo */}
        <div className="p-6 border-b border-ops-border">
          <div className="flex items-center space-x-3">
            <Activity className="h-8 w-8 text-ops-accent" />
            <div>
              <h1 className="text-xl font-bold text-ops-text">Allocentra</h1>
              <p className="text-xs text-ops-muted">Command Center</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1">
          {navigation.map((item) => {
            const Icon = item.icon
            const active = isActive(item.href)
            return (
              <Link
                key={item.name}
                to={item.href}
                className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors ${
                  active
                    ? 'bg-ops-accent text-white'
                    : 'text-ops-muted hover:bg-ops-border hover:text-ops-text'
                }`}
              >
                <Icon className="h-5 w-5" />
                <span className="font-medium">{item.name}</span>
              </Link>
            )
          })}
        </nav>

        {/* System Status */}
        <div className="p-4 border-t border-ops-border">
          <div className="flex items-center space-x-2 text-ops-success">
            <div className="h-2 w-2 bg-ops-success rounded-full animate-pulse" />
            <span className="text-sm">System Online</span>
          </div>
          <p className="text-xs text-ops-muted mt-1">Engine v1.0.0</p>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        {children}
      </main>
    </div>
  )
}

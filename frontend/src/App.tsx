import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { Requests } from './pages/Requests'
import { Cycles } from './pages/Cycles'
import { RunAllocation } from './pages/RunAllocation'
import { Results } from './pages/Results'
import { ScenarioLab } from './pages/ScenarioLab'
import { AuditLog } from './pages/AuditLog'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/cycles" element={<Cycles />} />
        <Route path="/requests" element={<Requests />} />
        <Route path="/run" element={<RunAllocation />} />
        <Route path="/results/:runId" element={<Results />} />
        <Route path="/scenarios" element={<ScenarioLab />} />
        <Route path="/audit" element={<AuditLog />} />
      </Routes>
    </Layout>
  )
}

export default App

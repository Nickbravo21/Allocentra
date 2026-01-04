-- Allocentra Database Schema V1

-- Allocation Cycles
CREATE TABLE allocation_cycles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    allow_partial_allocations BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT chk_cycle_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED')),
    CONSTRAINT chk_cycle_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_cycles_status ON allocation_cycles(status);
CREATE INDEX idx_cycles_dates ON allocation_cycles(start_date, end_date);

-- Budget Pools
CREATE TABLE budget_pools (
    id VARCHAR(36) PRIMARY KEY,
    cycle_id VARCHAR(36) NOT NULL REFERENCES allocation_cycles(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    allocated_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    CONSTRAINT chk_budget_category CHECK (category IN ('MONEY', 'PERSONNEL', 'VEHICLES', 'EQUIPMENT', 'HOURS', 'TRAINING', 'TRAVEL')),
    CONSTRAINT chk_budget_amounts CHECK (allocated_amount >= 0 AND allocated_amount <= total_amount)
);

CREATE INDEX idx_budget_cycle ON budget_pools(cycle_id);
CREATE INDEX idx_budget_category ON budget_pools(category);

-- Resource Pools
CREATE TABLE resource_pools (
    id VARCHAR(36) PRIMARY KEY,
    cycle_id VARCHAR(36) NOT NULL REFERENCES allocation_cycles(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    total_quantity DECIMAL(19,2) NOT NULL,
    allocated_quantity DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(50) NOT NULL DEFAULT 'COUNT',
    available_hours DECIMAL(19,2),
    exclusive BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_resource_category CHECK (category IN ('MONEY', 'PERSONNEL', 'VEHICLES', 'EQUIPMENT', 'HOURS', 'TRAINING', 'TRAVEL')),
    CONSTRAINT chk_resource_quantities CHECK (allocated_quantity >= 0 AND allocated_quantity <= total_quantity)
);

CREATE INDEX idx_resource_cycle ON resource_pools(cycle_id);
CREATE INDEX idx_resource_category_type ON resource_pools(category, resource_type);

-- Requests
CREATE TABLE requests (
    id VARCHAR(36) PRIMARY KEY,
    cycle_id VARCHAR(36) NOT NULL REFERENCES allocation_cycles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    justification TEXT,
    category VARCHAR(50) NOT NULL,
    amount_requested DECIMAL(19,2),
    minimum_viable_allocation DECIMAL(19,2),
    resource_type VARCHAR(100),
    quantity_requested DECIMAL(19,2),
    minimum_viable_quantity DECIMAL(19,2),
    priority INTEGER NOT NULL DEFAULT 3,
    urgency_deadline DATE NOT NULL,
    impact VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    risk VARCHAR(20) NOT NULL DEFAULT 'LOW',
    strategic INTEGER NOT NULL DEFAULT 3,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    score DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    start_date DATE,
    end_date DATE,
    CONSTRAINT chk_request_category CHECK (category IN ('MONEY', 'PERSONNEL', 'VEHICLES', 'EQUIPMENT', 'HOURS', 'TRAINING', 'TRAVEL')),
    CONSTRAINT chk_request_priority CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT chk_request_strategic CHECK (strategic BETWEEN 1 AND 5),
    CONSTRAINT chk_request_impact CHECK (impact IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_request_risk CHECK (risk IN ('LOW', 'OPERATIONAL', 'SAFETY', 'LEGAL')),
    CONSTRAINT chk_request_status CHECK (status IN ('PENDING', 'APPROVED', 'PARTIAL', 'DEFERRED', 'DENIED'))
);

CREATE INDEX idx_requests_cycle ON requests(cycle_id);
CREATE INDEX idx_requests_cycle_status ON requests(cycle_id, status);
CREATE INDEX idx_requests_score ON requests(score DESC);

-- Request Dependencies
CREATE TABLE request_dependencies (
    request_id VARCHAR(36) NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    dependency_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (request_id, dependency_id)
);

CREATE INDEX idx_dependencies_request ON request_dependencies(request_id);

-- Allocation Runs
CREATE TABLE allocation_runs (
    id VARCHAR(36) PRIMARY KEY,
    cycle_id VARCHAR(36) NOT NULL REFERENCES allocation_cycles(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    engine_version VARCHAR(20) NOT NULL,
    allow_partial_allocations BOOLEAN NOT NULL DEFAULT TRUE,
    category_caps_json TEXT,
    notes TEXT,
    total_requests INTEGER,
    approved_count INTEGER,
    partial_count INTEGER,
    deferred_count INTEGER,
    denied_count INTEGER,
    total_allocated DECIMAL(19,2),
    budget_utilization DECIMAL(10,4),
    execution_time_ms BIGINT,
    progress DECIMAL(10,2),
    current_phase VARCHAR(100),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT chk_run_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_runs_cycle ON allocation_runs(cycle_id);
CREATE INDEX idx_runs_status ON allocation_runs(status);
CREATE INDEX idx_runs_created ON allocation_runs(created_at DESC);

-- Allocation Results
CREATE TABLE allocation_results (
    id VARCHAR(36) PRIMARY KEY,
    run_id VARCHAR(36) NOT NULL REFERENCES allocation_runs(id) ON DELETE CASCADE,
    request_id VARCHAR(36) NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    amount_requested DECIMAL(19,2),
    amount_allocated DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    quantity_requested DECIMAL(19,2),
    quantity_allocated DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    score DECIMAL(10,2) NOT NULL,
    rank INTEGER NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_result_status CHECK (status IN ('PENDING', 'APPROVED', 'PARTIAL', 'DEFERRED', 'DENIED'))
);

CREATE INDEX idx_results_run ON allocation_results(run_id);
CREATE INDEX idx_results_request ON allocation_results(request_id);
CREATE INDEX idx_results_rank ON allocation_results(rank);

-- Allocation Result Constraints
CREATE TABLE allocation_result_constraints (
    result_id VARCHAR(36) NOT NULL REFERENCES allocation_results(id) ON DELETE CASCADE,
    constraint_type VARCHAR(100) NOT NULL,
    PRIMARY KEY (result_id, constraint_type)
);

-- Decision Explanations
CREATE TABLE decision_explanations (
    id VARCHAR(36) PRIMARY KEY,
    result_id VARCHAR(36) NOT NULL REFERENCES allocation_results(id) ON DELETE CASCADE,
    score_breakdown_json TEXT,
    reason_approved TEXT,
    reason_denied TEXT,
    reason_partial TEXT,
    reason_deferred TEXT,
    compared_to_request_id VARCHAR(36),
    compared_to_request_title VARCHAR(255),
    compared_to_score DECIMAL(10,2),
    score_difference DECIMAL(10,2),
    why_this_won TEXT,
    why_this_lost TEXT,
    UNIQUE(result_id)
);

CREATE INDEX idx_explanations_result ON decision_explanations(result_id);

-- Explanation Remediation Suggestions
CREATE TABLE explanation_remediation (
    explanation_id VARCHAR(36) NOT NULL REFERENCES decision_explanations(id) ON DELETE CASCADE,
    suggestion TEXT NOT NULL
);

CREATE INDEX idx_remediation_explanation ON explanation_remediation(explanation_id);

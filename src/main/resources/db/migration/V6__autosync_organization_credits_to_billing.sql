-- Auto-sync existing organization credits to billing_transactions
-- This ensures all organizations with credits get billing transaction records

-- For each organization with credits > 0, create an initial billing transaction if none exists
INSERT INTO billing_transactions (
    id,
    organization_id,
    type,
    amount,
    resulting_balance,
    date,
    description,
    processed_by,
    created_at,
    status
)
SELECT 
    CONCAT('txn_autosync_', SUBSTRING(o.id, 1, 12)) AS id,
    o.id AS organization_id,
    'CREDIT_ADDED' AS type,
    o.remaining_credits AS amount,
    o.remaining_credits AS resulting_balance,
    NOW() AS date,
    CONCAT(
        'Auto-sync: Initial credits from organization (Gifted: ',
        COALESCE(o.gifted_credits, 0),
        ', Purchased: ',
        COALESCE(o.purchased_credits, 0),
        ')'
    ) AS description,
    'system' AS processed_by,
    NOW() AS created_at,
    'completed' AS status
FROM organizations o
WHERE o.remaining_credits > 0
  AND NOT EXISTS (
      SELECT 1 
      FROM billing_transactions bt 
      WHERE bt.organization_id = o.id
  );

-- Log the number of synced organizations
SELECT 
    COUNT(*) AS organizations_synced,
    SUM(remaining_credits) AS total_credits_synced
FROM organizations
WHERE remaining_credits > 0
  AND EXISTS (
      SELECT 1 
      FROM billing_transactions bt 
      WHERE bt.organization_id = organizations.id
      AND bt.description LIKE 'Auto-sync: Initial credits%'
  );

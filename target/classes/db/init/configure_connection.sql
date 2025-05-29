-- Configuration des paramètres de connexion

-- Configurer les paramètres de session
SET session_replication_role = 'replica';
SET client_min_messages = 'error';

-- Configurer les timeouts de session
SET statement_timeout = '30s';
SET idle_in_transaction_session_timeout = '60s';

-- Configurer les paramètres TCP
SET tcp_keepalives_idle = '60';
SET tcp_keepalives_interval = '10';
SET tcp_keepalives_count = '6';

-- Note: Les paramètres suivants doivent être configurés manuellement dans postgresql.conf:
-- max_connections = 100
-- max_prepared_transactions = 50

-- Afficher les paramètres actuels
SELECT name, setting, unit, context 
FROM pg_settings 
WHERE name IN (
    'session_replication_role',
    'client_min_messages',
    'statement_timeout',
    'idle_in_transaction_session_timeout',
    'tcp_keepalives_idle',
    'tcp_keepalives_interval',
    'tcp_keepalives_count'
); 
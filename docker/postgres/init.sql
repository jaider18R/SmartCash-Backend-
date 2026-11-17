\set ON_ERROR_STOP on

\getenv usuarios_db USUARIOS_DB_NAME
\getenv usuarios_user USUARIOS_DB_USER
\getenv usuarios_pass USUARIOS_DB_PASSWORD

\getenv transacciones_db TRANSACCIONES_DB_NAME
\getenv transacciones_user TRANSACCIONES_DB_USER
\getenv transacciones_pass TRANSACCIONES_DB_PASSWORD

SELECT format('CREATE ROLE %I WITH LOGIN PASSWORD %L', :'usuarios_user', :'usuarios_pass')
WHERE NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = :'usuarios_user') \gexec

SELECT format('ALTER ROLE %I WITH LOGIN PASSWORD %L', :'usuarios_user', :'usuarios_pass') \gexec

SELECT format('CREATE ROLE %I WITH LOGIN PASSWORD %L', :'transacciones_user', :'transacciones_pass')
WHERE NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = :'transacciones_user') \gexec

SELECT format('ALTER ROLE %I WITH LOGIN PASSWORD %L', :'transacciones_user', :'transacciones_pass') \gexec

SELECT format('CREATE DATABASE %I OWNER %I', :'usuarios_db', :'usuarios_user')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'usuarios_db') \gexec

SELECT format('CREATE DATABASE %I OWNER %I', :'transacciones_db', :'transacciones_user')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'transacciones_db') \gexec

SELECT format('REVOKE ALL ON DATABASE %I FROM PUBLIC', :'usuarios_db') \gexec
SELECT format('GRANT ALL PRIVILEGES ON DATABASE %I TO %I', :'usuarios_db', :'usuarios_user') \gexec

SELECT format('REVOKE ALL ON DATABASE %I FROM PUBLIC', :'transacciones_db') \gexec
SELECT format('GRANT ALL PRIVILEGES ON DATABASE %I TO %I', :'transacciones_db', :'transacciones_user') \gexec

DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'usuarios_user') THEN
      CREATE ROLE usuarios_user WITH LOGIN PASSWORD 'smartcash_db_pass_2026';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'transacciones_user') THEN
      CREATE ROLE transacciones_user WITH LOGIN PASSWORD 'smartcash_db_pass_2026';
   END IF;
END
$do$;

CREATE DATABASE usuarios_db WITH OWNER usuarios_user;
CREATE DATABASE transacciones_db WITH OWNER transacciones_user;

GRANT ALL PRIVILEGES ON DATABASE usuarios_db TO usuarios_user;
GRANT ALL PRIVILEGES ON DATABASE transacciones_db TO transacciones_user;

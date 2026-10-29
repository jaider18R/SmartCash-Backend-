"""Conexion a la base de datos. Toda la configuracion viene de variables de entorno."""
import os

from sqlalchemy import create_engine, text
from sqlalchemy.engine import make_url
from sqlalchemy.orm import sessionmaker

from app.infrastructure.persistence.models_sql import Base

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./logs_db.sqlite")


def asegurar_base_de_datos(url: str) -> None:
    """Si es PostgreSQL y la base 'logs_db' no existe todavia, la crea (se conecta a la base 'postgres').

    Asi el servicio funciona aunque el volumen de PostgreSQL ya existiera antes de agregar logs_db.
    """
    parsed = make_url(url)
    if not parsed.drivername.startswith("postgresql"):
        return
    admin = create_engine(parsed.set(database="postgres"), isolation_level="AUTOCOMMIT")
    try:
        with admin.connect() as conn:
            existe = conn.execute(text("SELECT 1 FROM pg_database WHERE datname = :n"),
                                  {"n": parsed.database}).scalar()
            if not existe:
                conn.execute(text(f'CREATE DATABASE "{parsed.database}"'))
    finally:
        admin.dispose()


def crear_motor(url: str = DATABASE_URL):
    asegurar_base_de_datos(url)
    kwargs = {"connect_args": {"check_same_thread": False}} if url.startswith("sqlite") else {"pool_pre_ping": True}
    return create_engine(url, **kwargs)


def crear_sesion_factory(engine):
    Base.metadata.create_all(bind=engine)
    return sessionmaker(bind=engine, autoflush=False, autocommit=False)

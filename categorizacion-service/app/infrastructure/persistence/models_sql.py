from sqlalchemy import Column, Integer, String, Float, DateTime
from sqlalchemy.orm import declarative_base
from datetime import datetime

Base = declarative_base()

class TransaccionClasificadaDB(Base):
    __tablename__ = "transacciones_clasificadas"

    transaccion_id = Column(Integer, primary_key=True)
    comercio = Column(String(150), nullable=False)
    monto = Column(Float, nullable=False)
    descripcion = Column(String(255), nullable=True)
    categoria = Column(String(100), nullable=False)
    nivel_confianza = Column(Float, nullable=False)
    usuario_id = Column(Integer, nullable=True)
    fecha = Column(DateTime, default=datetime.utcnow)

class CorreccionDB(Base):
    __tablename__ = "correcciones_categorias"

    id = Column(Integer, primary_key=True, autoincrement=True)
    transaccion_id = Column(Integer, nullable=False)
    categoria_anterior = Column(String(100), nullable=False)
    categoria_nueva = Column(String(100), nullable=False)
    comercio = Column(String(150), nullable=False)
    monto = Column(Float, nullable=False)
    fecha = Column(DateTime, default=datetime.utcnow)

from datetime import datetime, timezone

from sqlalchemy import BigInteger, DateTime, Index, Integer, String, Text
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class LogSistemaModel(Base):
    __tablename__ = "logs_sistema"

    id: Mapped[int] = mapped_column(BigInteger().with_variant(Integer, "sqlite"), primary_key=True, autoincrement=True)
    fecha: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False,
                                            default=lambda: datetime.now(timezone.utc), index=True)
    servicio: Mapped[str] = mapped_column(String(60), nullable=False)
    nivel: Mapped[str] = mapped_column(String(10), nullable=False)
    mensaje: Mapped[str] = mapped_column(Text, nullable=False)
    detalle: Mapped[str] = mapped_column(Text, nullable=True)
    ruta: Mapped[str] = mapped_column(String(300), nullable=True)
    metodo: Mapped[str] = mapped_column(String(10), nullable=True)
    estado_http: Mapped[int] = mapped_column(Integer, nullable=True)
    usuario: Mapped[str] = mapped_column(String(120), nullable=True)

    __table_args__ = (Index("ix_logs_servicio_nivel", "servicio", "nivel"),)

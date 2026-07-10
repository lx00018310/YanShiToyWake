"""数据库引擎与初始化。

engine 为模块级全局，便于测试时替换为内存引擎。
init_db 通过模块属性在调用时解析 engine，因此替换 database.engine 即生效。
"""

from sqlmodel import SQLModel, create_engine

from app.config import settings

engine = create_engine(
    settings.database_url,
    connect_args={"check_same_thread": False},
    echo=False,
)


def init_db() -> None:
    """创建所有表。可重复调用。"""
    SQLModel.metadata.create_all(engine)

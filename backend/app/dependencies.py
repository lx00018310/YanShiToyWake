"""依赖注入：数据库会话等。

get_session 通过模块属性解析 engine，测试时替换 database.engine 即对会话生效。
"""

from sqlmodel import Session

from app import database


def get_session():
    with Session(database.engine) as session:
        yield session

"""应用配置：通过 pydantic-settings 从环境变量 / .env 加载。"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # AI
    ai_mode: str = "mock"  # mock | ai
    ai_base_url: str = ""
    ai_api_key: str = ""
    ai_model: str = ""
    ai_timeout_seconds: float = 10.0

    # 数据库
    database_url: str = "sqlite:///./toywake.db"

    # 业务
    max_sparks_per_session: int = 5


settings = Settings()

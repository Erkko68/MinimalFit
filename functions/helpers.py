from datetime import datetime, timezone, timedelta

from firebase_admin import messaging


def send_fcm(token: str, title: str, body: str) -> None:
    message = messaging.Message(
        notification=messaging.Notification(title=title, body=body),
        token=token,
    )
    messaging.send(message)


def today_utc_range():
    """Returns (start_of_day, end_of_day) as UTC datetime objects."""
    now = datetime.now(timezone.utc)
    start = now.replace(hour=0, minute=0, second=0, microsecond=0)
    end = start + timedelta(days=1)
    return start, end


def today_utc_str() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%d")

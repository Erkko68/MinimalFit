from firebase_admin import firestore
from firebase_functions import scheduler_fn

from helpers import send_fcm


@scheduler_fn.on_schedule(schedule="every day 08:00", timezone=scheduler_fn.Timezone("UTC"))
def daily_run_reminder(_event: scheduler_fn.ScheduledEvent) -> None:
    """
    Runs every day at 08:00 UTC.
    Sends a push notification to every user who has enabled the daily run reminder.
    Firestore path: users/{uid}  →  fcmToken: str, notifications.dailyRun: bool
    """
    db = firestore.client()
    users = db.collection("users").stream()

    for user_doc in users:
        data = user_doc.to_dict() or {}
        notifications = data.get("notifications", {})
        if not notifications.get("dailyRun", False):
            continue
        token = data.get("fcmToken")
        if not token:
            continue
        try:
            send_fcm(
                token=token,
                title="Time for your daily run! 🏃",
                body="Lace up and get moving — your streak is waiting.",
            )
        except Exception:
            pass

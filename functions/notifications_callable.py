from firebase_admin import firestore
from firebase_functions import https_fn

from helpers import send_fcm, today_utc_str


@https_fn.on_call()
def check_weight_milestone(req: https_fn.CallableRequest):
    """
    Called by the Android app after a set is completed.
    The app passes the current daily total (kg) and the function:
      1. Checks if the user has enabled the milestone notification.
      2. Checks that the milestone hasn't been notified today already.
      3. Sends FCM and records the notification date.

    Expected request body: { "totalKg": <float> }
    Firestore paths:
      users/{uid}  →  fcmToken, notifications.weightMilestone, milestones.weightMilestoneDate
    """
    if req.auth is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.UNAUTHENTICATED,
            message="Authentication required.",
        )

    uid = req.auth.uid
    total_kg = req.data.get("totalKg", 0.0)

    if total_kg < 1000:
        return {"sent": False, "reason": "threshold not reached"}

    db = firestore.client()
    user_ref = db.collection("users").document(uid)
    user_doc = user_ref.get()
    if not user_doc.exists:
        return {"sent": False, "reason": "user not found"}

    data = user_doc.to_dict() or {}
    notifications = data.get("notifications", {})
    if not notifications.get("weightMilestone", False):
        return {"sent": False, "reason": "notification disabled"}

    token = data.get("fcmToken")
    if not token:
        return {"sent": False, "reason": "no fcm token"}

    today_str = today_utc_str()
    milestones = data.get("milestones", {})
    if milestones.get("weightMilestoneDate") == today_str:
        return {"sent": False, "reason": "already notified today"}

    try:
        send_fcm(
            token=token,
            title="1 000 kg milestone! 💪",
            body="You've lifted over 1 000 kg today. Incredible work!",
        )
        user_ref.set(
            {"milestones": {"weightMilestoneDate": today_str}},
            merge=True,
        )
        return {"sent": True}
    except Exception as e:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=str(e),
        )

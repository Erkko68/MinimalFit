from firebase_admin import initialize_app

initialize_app()

# Firebase Functions discovers decorated functions by importing this module.
# Each function lives in its own file; re-exporting here keeps the entry point clean.
from notifications_scheduled import daily_run_reminder
from notifications_callable import check_weight_milestone

__all__ = ["daily_run_reminder", "check_weight_milestone"]

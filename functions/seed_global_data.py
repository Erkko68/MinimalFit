"""
One-time script to seed global exercises and ingredients into Firestore.
Run from the project root:
    python functions/seed_global_data.py

Requires Application Default Credentials:
    firebase login  (already done if you use Firebase CLI)
    gcloud auth application-default login  -- OR --
    set GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccountKey.json
"""

import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timezone

PROJECT_ID = "minimalfit-e4f0d"

# ── Data ──────────────────────────────────────────────────────────────────────

EXERCISES = [
    # Chest
    {"name": "Bench Press",           "isBodyweight": False, "muscleGroup": "Chest",     "restSeconds": 120},
    {"name": "Incline Bench Press",   "isBodyweight": False, "muscleGroup": "Chest",     "restSeconds": 120},
    {"name": "Decline Bench Press",   "isBodyweight": False, "muscleGroup": "Chest",     "restSeconds": 120},
    {"name": "Chest Fly",             "isBodyweight": False, "muscleGroup": "Chest",     "restSeconds": 90},
    {"name": "Cable Crossover",       "isBodyweight": False, "muscleGroup": "Chest",     "restSeconds": 90},
    {"name": "Push-Up",               "isBodyweight": True,  "muscleGroup": "Chest",     "restSeconds": 60},
    {"name": "Dumbbell Pullover",     "isBodyweight": False, "muscleGroup": "Chest",     "restSeconds": 90},
    # Back
    {"name": "Deadlift",              "isBodyweight": False, "muscleGroup": "Back",      "restSeconds": 180},
    {"name": "Pull-Up",               "isBodyweight": True,  "muscleGroup": "Back",      "restSeconds": 120},
    {"name": "Chin-Up",               "isBodyweight": True,  "muscleGroup": "Back",      "restSeconds": 120},
    {"name": "Barbell Row",           "isBodyweight": False, "muscleGroup": "Back",      "restSeconds": 120},
    {"name": "Dumbbell Row",          "isBodyweight": False, "muscleGroup": "Back",      "restSeconds": 90},
    {"name": "Lat Pulldown",          "isBodyweight": False, "muscleGroup": "Back",      "restSeconds": 90},
    {"name": "Seated Cable Row",      "isBodyweight": False, "muscleGroup": "Back",      "restSeconds": 90},
    {"name": "T-Bar Row",             "isBodyweight": False, "muscleGroup": "Back",      "restSeconds": 120},
    # Shoulders
    {"name": "Overhead Press",        "isBodyweight": False, "muscleGroup": "Shoulders", "restSeconds": 120},
    {"name": "Dumbbell Shoulder Press","isBodyweight": False,"muscleGroup": "Shoulders", "restSeconds": 90},
    {"name": "Lateral Raise",         "isBodyweight": False, "muscleGroup": "Shoulders", "restSeconds": 60},
    {"name": "Front Raise",           "isBodyweight": False, "muscleGroup": "Shoulders", "restSeconds": 60},
    {"name": "Rear Delt Fly",         "isBodyweight": False, "muscleGroup": "Shoulders", "restSeconds": 60},
    {"name": "Arnold Press",          "isBodyweight": False, "muscleGroup": "Shoulders", "restSeconds": 90},
    {"name": "Face Pull",             "isBodyweight": False, "muscleGroup": "Shoulders", "restSeconds": 60},
    # Biceps
    {"name": "Barbell Curl",          "isBodyweight": False, "muscleGroup": "Biceps",    "restSeconds": 90},
    {"name": "Dumbbell Curl",         "isBodyweight": False, "muscleGroup": "Biceps",    "restSeconds": 90},
    {"name": "Hammer Curl",           "isBodyweight": False, "muscleGroup": "Biceps",    "restSeconds": 60},
    {"name": "Preacher Curl",         "isBodyweight": False, "muscleGroup": "Biceps",    "restSeconds": 90},
    {"name": "Incline Dumbbell Curl", "isBodyweight": False, "muscleGroup": "Biceps",    "restSeconds": 60},
    {"name": "Cable Curl",            "isBodyweight": False, "muscleGroup": "Biceps",    "restSeconds": 60},
    # Triceps
    {"name": "Tricep Dip",            "isBodyweight": True,  "muscleGroup": "Triceps",   "restSeconds": 90},
    {"name": "Skull Crusher",         "isBodyweight": False, "muscleGroup": "Triceps",   "restSeconds": 90},
    {"name": "Tricep Pushdown",       "isBodyweight": False, "muscleGroup": "Triceps",   "restSeconds": 60},
    {"name": "Overhead Tricep Extension","isBodyweight": False,"muscleGroup": "Triceps", "restSeconds": 60},
    {"name": "Close-Grip Bench Press","isBodyweight": False, "muscleGroup": "Triceps",   "restSeconds": 120},
    {"name": "Diamond Push-Up",       "isBodyweight": True,  "muscleGroup": "Triceps",   "restSeconds": 60},
    # Legs
    {"name": "Squat",                 "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 180},
    {"name": "Front Squat",           "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 180},
    {"name": "Leg Press",             "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 120},
    {"name": "Romanian Deadlift",     "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 120},
    {"name": "Lunges",                "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 90},
    {"name": "Bulgarian Split Squat", "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 90},
    {"name": "Leg Curl",              "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 90},
    {"name": "Leg Extension",         "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 90},
    {"name": "Calf Raise",            "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 60},
    {"name": "Goblet Squat",          "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 90},
    {"name": "Hip Thrust",            "isBodyweight": False, "muscleGroup": "Legs",      "restSeconds": 90},
    # Core
    {"name": "Plank",                 "isBodyweight": True,  "muscleGroup": "Core",      "restSeconds": 60},
    {"name": "Crunch",                "isBodyweight": True,  "muscleGroup": "Core",      "restSeconds": 60},
    {"name": "Hanging Leg Raise",     "isBodyweight": True,  "muscleGroup": "Core",      "restSeconds": 60},
    {"name": "Ab Wheel Rollout",      "isBodyweight": True,  "muscleGroup": "Core",      "restSeconds": 60},
    {"name": "Russian Twist",         "isBodyweight": True,  "muscleGroup": "Core",      "restSeconds": 60},
    {"name": "Cable Crunch",          "isBodyweight": False, "muscleGroup": "Core",      "restSeconds": 60},
    # Cardio / Full body
    {"name": "Burpee",                "isBodyweight": True,  "muscleGroup": "Full Body", "restSeconds": 60},
    {"name": "Jumping Jack",          "isBodyweight": True,  "muscleGroup": "Full Body", "restSeconds": 30},
    {"name": "Mountain Climber",      "isBodyweight": True,  "muscleGroup": "Full Body", "restSeconds": 45},
    {"name": "Kettlebell Swing",      "isBodyweight": False, "muscleGroup": "Full Body", "restSeconds": 60},
    {"name": "Clean and Press",       "isBodyweight": False, "muscleGroup": "Full Body", "restSeconds": 120},
]

# Calories are per 100 g (or per 100 ml for liquids). measurementUnit: GRAMS or MILLILITERS.
INGREDIENTS = [
    # Proteins
    {"name": "Chicken Breast",        "baseCalories": 165, "measurementUnit": "GRAMS"},
    {"name": "Chicken Thigh",         "baseCalories": 209, "measurementUnit": "GRAMS"},
    {"name": "Turkey Breast",         "baseCalories": 135, "measurementUnit": "GRAMS"},
    {"name": "Salmon",                "baseCalories": 208, "measurementUnit": "GRAMS"},
    {"name": "Tuna (canned)",         "baseCalories": 116, "measurementUnit": "GRAMS"},
    {"name": "Cod",                   "baseCalories": 82,  "measurementUnit": "GRAMS"},
    {"name": "Shrimp",                "baseCalories": 85,  "measurementUnit": "GRAMS"},
    {"name": "Egg",                   "baseCalories": 155, "measurementUnit": "GRAMS"},
    {"name": "Egg White",             "baseCalories": 52,  "measurementUnit": "GRAMS"},
    {"name": "Ground Beef (lean)",    "baseCalories": 215, "measurementUnit": "GRAMS"},
    {"name": "Ground Beef (regular)", "baseCalories": 254, "measurementUnit": "GRAMS"},
    {"name": "Steak (sirloin)",       "baseCalories": 207, "measurementUnit": "GRAMS"},
    {"name": "Pork Tenderloin",       "baseCalories": 143, "measurementUnit": "GRAMS"},
    {"name": "Ham",                   "baseCalories": 145, "measurementUnit": "GRAMS"},
    {"name": "Tofu",                  "baseCalories": 76,  "measurementUnit": "GRAMS"},
    {"name": "Tempeh",                "baseCalories": 193, "measurementUnit": "GRAMS"},
    {"name": "Whey Protein Powder",   "baseCalories": 370, "measurementUnit": "GRAMS"},
    # Dairy
    {"name": "Whole Milk",            "baseCalories": 61,  "measurementUnit": "MILLILITERS"},
    {"name": "Skimmed Milk",          "baseCalories": 35,  "measurementUnit": "MILLILITERS"},
    {"name": "Greek Yogurt (0%)",     "baseCalories": 59,  "measurementUnit": "GRAMS"},
    {"name": "Greek Yogurt (full)",   "baseCalories": 97,  "measurementUnit": "GRAMS"},
    {"name": "Cottage Cheese",        "baseCalories": 98,  "measurementUnit": "GRAMS"},
    {"name": "Cheddar Cheese",        "baseCalories": 403, "measurementUnit": "GRAMS"},
    {"name": "Mozzarella",            "baseCalories": 280, "measurementUnit": "GRAMS"},
    {"name": "Butter",                "baseCalories": 717, "measurementUnit": "GRAMS"},
    # Carbs / Grains
    {"name": "White Rice",            "baseCalories": 130, "measurementUnit": "GRAMS"},
    {"name": "Brown Rice",            "baseCalories": 112, "measurementUnit": "GRAMS"},
    {"name": "Oats",                  "baseCalories": 389, "measurementUnit": "GRAMS"},
    {"name": "White Bread",           "baseCalories": 265, "measurementUnit": "GRAMS"},
    {"name": "Whole Wheat Bread",     "baseCalories": 247, "measurementUnit": "GRAMS"},
    {"name": "Pasta (dry)",           "baseCalories": 371, "measurementUnit": "GRAMS"},
    {"name": "Pasta (cooked)",        "baseCalories": 131, "measurementUnit": "GRAMS"},
    {"name": "Potato",                "baseCalories": 77,  "measurementUnit": "GRAMS"},
    {"name": "Sweet Potato",          "baseCalories": 86,  "measurementUnit": "GRAMS"},
    {"name": "Quinoa (cooked)",       "baseCalories": 120, "measurementUnit": "GRAMS"},
    {"name": "Lentils (cooked)",      "baseCalories": 116, "measurementUnit": "GRAMS"},
    {"name": "Chickpeas (cooked)",    "baseCalories": 164, "measurementUnit": "GRAMS"},
    {"name": "Black Beans (cooked)",  "baseCalories": 132, "measurementUnit": "GRAMS"},
    {"name": "Corn",                  "baseCalories": 86,  "measurementUnit": "GRAMS"},
    # Fats / Nuts
    {"name": "Olive Oil",             "baseCalories": 884, "measurementUnit": "MILLILITERS"},
    {"name": "Coconut Oil",           "baseCalories": 862, "measurementUnit": "GRAMS"},
    {"name": "Avocado",               "baseCalories": 160, "measurementUnit": "GRAMS"},
    {"name": "Almond",                "baseCalories": 579, "measurementUnit": "GRAMS"},
    {"name": "Walnut",                "baseCalories": 654, "measurementUnit": "GRAMS"},
    {"name": "Peanut Butter",         "baseCalories": 588, "measurementUnit": "GRAMS"},
    {"name": "Cashew",                "baseCalories": 553, "measurementUnit": "GRAMS"},
    {"name": "Chia Seeds",            "baseCalories": 486, "measurementUnit": "GRAMS"},
    {"name": "Flaxseed",              "baseCalories": 534, "measurementUnit": "GRAMS"},
    # Vegetables
    {"name": "Broccoli",              "baseCalories": 34,  "measurementUnit": "GRAMS"},
    {"name": "Spinach",               "baseCalories": 23,  "measurementUnit": "GRAMS"},
    {"name": "Kale",                  "baseCalories": 49,  "measurementUnit": "GRAMS"},
    {"name": "Carrot",                "baseCalories": 41,  "measurementUnit": "GRAMS"},
    {"name": "Bell Pepper",           "baseCalories": 31,  "measurementUnit": "GRAMS"},
    {"name": "Tomato",                "baseCalories": 18,  "measurementUnit": "GRAMS"},
    {"name": "Cucumber",              "baseCalories": 16,  "measurementUnit": "GRAMS"},
    {"name": "Lettuce",               "baseCalories": 15,  "measurementUnit": "GRAMS"},
    {"name": "Onion",                 "baseCalories": 40,  "measurementUnit": "GRAMS"},
    {"name": "Garlic",                "baseCalories": 149, "measurementUnit": "GRAMS"},
    {"name": "Mushroom",              "baseCalories": 22,  "measurementUnit": "GRAMS"},
    {"name": "Zucchini",              "baseCalories": 17,  "measurementUnit": "GRAMS"},
    {"name": "Cauliflower",           "baseCalories": 25,  "measurementUnit": "GRAMS"},
    {"name": "Green Beans",           "baseCalories": 31,  "measurementUnit": "GRAMS"},
    {"name": "Asparagus",             "baseCalories": 20,  "measurementUnit": "GRAMS"},
    # Fruits
    {"name": "Banana",                "baseCalories": 89,  "measurementUnit": "GRAMS"},
    {"name": "Apple",                 "baseCalories": 52,  "measurementUnit": "GRAMS"},
    {"name": "Orange",                "baseCalories": 47,  "measurementUnit": "GRAMS"},
    {"name": "Strawberry",            "baseCalories": 32,  "measurementUnit": "GRAMS"},
    {"name": "Blueberry",             "baseCalories": 57,  "measurementUnit": "GRAMS"},
    {"name": "Mango",                 "baseCalories": 60,  "measurementUnit": "GRAMS"},
    {"name": "Pineapple",             "baseCalories": 50,  "measurementUnit": "GRAMS"},
    {"name": "Grapes",                "baseCalories": 69,  "measurementUnit": "GRAMS"},
    {"name": "Watermelon",            "baseCalories": 30,  "measurementUnit": "GRAMS"},
    {"name": "Peach",                 "baseCalories": 39,  "measurementUnit": "GRAMS"},
    # Beverages
    {"name": "Orange Juice",          "baseCalories": 45,  "measurementUnit": "MILLILITERS"},
    {"name": "Whole Milk",            "baseCalories": 61,  "measurementUnit": "MILLILITERS"},
    {"name": "Coffee (black)",        "baseCalories": 2,   "measurementUnit": "MILLILITERS"},
]

# ── Seeding ───────────────────────────────────────────────────────────────────

def seed():
    app = firebase_admin.initialize_app(options={"projectId": PROJECT_ID})
    db = firestore.client()
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

    print("Seeding exercises...")
    ex_col = db.collection("global").document("exercises").collection("items")
    batch = db.batch()
    for i, ex in enumerate(EXERCISES):
        doc_ref = ex_col.document()
        batch.set(doc_ref, {
            "name": ex["name"],
            "isBodyweight": ex["isBodyweight"],
            "muscleGroup": ex.get("muscleGroup"),
            "restSeconds": ex["restSeconds"],
            "isGlobal": True,
            "creatorId": None,
            "updatedAt": now,
        })
        if (i + 1) % 400 == 0:
            batch.commit()
            batch = db.batch()
    batch.commit()
    print(f"  {len(EXERCISES)} exercises written.")

    print("Seeding ingredients...")
    ing_col = db.collection("global").document("ingredients").collection("items")
    batch = db.batch()
    for i, ing in enumerate(INGREDIENTS):
        doc_ref = ing_col.document()
        batch.set(doc_ref, {
            "name": ing["name"],
            "baseCalories": ing["baseCalories"],
            "measurementUnit": ing["measurementUnit"],
            "imageUrl": None,
            "isGlobal": True,
            "creatorId": None,
            "updatedAt": now,
        })
        if (i + 1) % 400 == 0:
            batch.commit()
            batch = db.batch()
    batch.commit()
    print(f"  {len(INGREDIENTS)} ingredients written.")

    print("Done.")

if __name__ == "__main__":
    seed()

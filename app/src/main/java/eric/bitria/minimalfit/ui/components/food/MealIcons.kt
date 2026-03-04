package eric.bitria.minimalfit.ui.components.food

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NamedIcon(val icon: ImageVector, val label: String)

val mealIcons = listOf(
    NamedIcon(Icons.Default.BreakfastDining, "Breakfast"),
    NamedIcon(Icons.Default.LunchDining, "Lunch"),
    NamedIcon(Icons.Default.DinnerDining, "Dinner"),
    NamedIcon(Icons.Default.Restaurant, "Restaurant"),
    NamedIcon(Icons.Default.Egg, "Egg"),
    NamedIcon(Icons.Default.LocalPizza, "Pizza"),
    NamedIcon(Icons.Default.LocalCafe, "Café"),
    NamedIcon(Icons.Default.LocalBar, "Bar"),
    NamedIcon(Icons.Default.EmojiFoodBeverage, "Beverage"),
    NamedIcon(Icons.Default.LocalDrink, "Drink"),
    NamedIcon(Icons.Default.Fastfood, "Fast food"),
    NamedIcon(Icons.Default.BakeryDining, "Bakery"),
    NamedIcon(Icons.Default.RamenDining, "Ramen"),
    NamedIcon(Icons.Default.Icecream, "Ice cream"),
    NamedIcon(Icons.Default.SoupKitchen, "Soup"),
    NamedIcon(Icons.Default.SetMeal, "Set meal"),
    NamedIcon(Icons.Default.Blender, "Smoothie"),
    NamedIcon(Icons.Default.FitnessCenter, "Fitness"),
)


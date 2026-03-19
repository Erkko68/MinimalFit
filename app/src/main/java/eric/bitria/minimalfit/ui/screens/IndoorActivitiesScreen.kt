package eric.bitria.minimalfit.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndoorActivitiesScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(Spacing.m)
    ){
        Text(
            text = "Indoor Activities Screen"
        )
    }
}
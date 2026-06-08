package app.echo.android

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EchoMobileApp(viewModel: EchoAndroidViewModel = viewModel()) {
    EchoAppRoot(viewModel = viewModel)
}

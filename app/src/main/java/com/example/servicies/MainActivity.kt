package com.example.servicies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.servicies.ui.theme.ServiciesTheme

class MainActivity : ComponentActivity() {
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    var hasPermissionState: MutableState<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Notifications.createNotificationChannel(this)

        setContent {
            ServiciesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotificationStatusView(
                        modifier = Modifier.padding(innerPadding),
                        onPermissionRequest = { requestNotificationPermission() },
                        onWorkStart = { startWorkChain() }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            val isGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            hasPermissionState?.value = isGranted

            if (!isGranted) {
                Toast.makeText(this, "Уведомления отключены", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startWorkChain() {
        val workA = OneTimeWorkRequestBuilder<GeneratorWorker>().build()
        val workB = OneTimeWorkRequestBuilder<FilterWorker>().build()
        val workC = OneTimeWorkRequestBuilder<AnalyzerWorker>().build()

        WorkManager.getInstance(this)
            .beginWith(workA)
            .then(workB)
            .then(workC)
            .enqueue()
    }
}

@Composable
fun NotificationStatusView(
    modifier: Modifier = Modifier,
    onPermissionRequest: () -> Unit,
    onWorkStart: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                true
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    // Сохраняем ссылку на состояние в активности
    DisposableEffect(Unit) {
        (context as? MainActivity)?.hasPermissionState = hasPermission
        onDispose {
            (context as? MainActivity)?.hasPermissionState = null
        }
    }

    val statusText = if (hasPermission.value) {
        "Уведомления разрешены — готово к работе"
    } else {
        "Уведомления запрещены — не работает"
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = statusText)
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission.value) {
                    onPermissionRequest()
                } else {
                    onWorkStart()
                }
            }
        ) {
            Text("Запуск")
        }
    }
}
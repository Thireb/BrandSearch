package com.closetfinder.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = viewModel(),
) {
    val status by viewModel.status.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::import) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Export everything to a zip file you can move to a new phone, or import a zip you saved before.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Button(
                onClick = {
                    val name = "closet_finder_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.zip"
                    exportLauncher.launch(name)
                },
                enabled = status != BackupStatus.WORKING,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Export backup") }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                enabled = status != BackupStatus.WORKING,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Import backup") }

            if (status == BackupStatus.WORKING) {
                CircularProgressIndicator()
            }
            if (status == BackupStatus.FAILED) {
                Text("Something went wrong. Please try again.", color = MaterialTheme.colorScheme.error)
            }
            if (status == BackupStatus.EXPORT_DONE) {
                Text("Backup saved.", color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }

    if (status == BackupStatus.IMPORT_DONE) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Import complete") },
            text = { Text("Please close and reopen Closet Finder to see the restored items.") },
            confirmButton = { TextButton(onClick = onBack) { Text("OK") } },
        )
    }
}

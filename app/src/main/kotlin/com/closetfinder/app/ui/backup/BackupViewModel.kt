package com.closetfinder.app.ui.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.closetfinder.app.data.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BackupStatus { IDLE, WORKING, EXPORT_DONE, IMPORT_DONE, FAILED }

class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val _status = MutableStateFlow(BackupStatus.IDLE)
    val status: StateFlow<BackupStatus> = _status.asStateFlow()

    fun export(destinationUri: Uri) {
        _status.value = BackupStatus.WORKING
        viewModelScope.launch {
            _status.value = runCatching { BackupManager.export(getApplication(), destinationUri) }
                .fold({ BackupStatus.EXPORT_DONE }, { BackupStatus.FAILED })
        }
    }

    fun import(sourceUri: Uri) {
        _status.value = BackupStatus.WORKING
        viewModelScope.launch {
            _status.value = runCatching { BackupManager.import(getApplication(), sourceUri) }
                .fold({ BackupStatus.IMPORT_DONE }, { BackupStatus.FAILED })
        }
    }
}

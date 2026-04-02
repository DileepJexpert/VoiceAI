package com.voiceai.app.presentation.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.data.local.dao.FolderDao
import com.voiceai.app.data.local.entity.FolderEntity
import com.voiceai.app.domain.model.Folder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FolderViewMode {
    GRID, LIST
}

data class FoldersUiState(
    val folders: List<Folder> = emptyList(),
    val viewMode: FolderViewMode = FolderViewMode.GRID,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingFolder: Folder? = null
)

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderDao: FolderDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoldersUiState())
    val uiState: StateFlow<FoldersUiState> = _uiState.asStateFlow()

    private val smartFolders = listOf(
        Folder(id = -1, name = "Meetings", icon = "\uD83D\uDCC5", isSmartFolder = true, sortRule = "meetings"),
        Folder(id = -2, name = "Ideas", icon = "\uD83D\uDCA1", isSmartFolder = true, sortRule = "ideas"),
        Folder(id = -3, name = "Lectures", icon = "\uD83C\uDF93", isSmartFolder = true, sortRule = "lectures"),
        Folder(id = -4, name = "Receipts & Invoices", icon = "\uD83E\uDDFE", isSmartFolder = true, sortRule = "receipts"),
        Folder(id = -5, name = "Business Cards", icon = "\uD83D\uDCBC", isSmartFolder = true, sortRule = "business_cards"),
        Folder(id = -6, name = "Personal Notes", icon = "\uD83D\uDCDD", isSmartFolder = true, sortRule = "personal")
    )

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            folderDao.getAll().collect { entities ->
                val userFolders = entities.map { entity ->
                    Folder(
                        id = entity.id,
                        name = entity.name,
                        icon = entity.icon,
                        isSmartFolder = entity.isSmartFolder,
                        sortRule = entity.sortRule
                    )
                }
                _uiState.update { it.copy(folders = smartFolders + userFolders) }
            }
        }
    }

    fun createFolder(name: String, icon: String) {
        viewModelScope.launch {
            folderDao.insert(
                FolderEntity(
                    name = name,
                    icon = icon,
                    isSmartFolder = false
                )
            )
            _uiState.update { it.copy(showCreateDialog = false) }
        }
    }

    fun deleteFolder(folder: Folder) {
        if (folder.isSmartFolder) return
        viewModelScope.launch {
            folderDao.delete(
                FolderEntity(
                    id = folder.id,
                    name = folder.name,
                    icon = folder.icon,
                    isSmartFolder = folder.isSmartFolder,
                    sortRule = folder.sortRule
                )
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(
                viewMode = if (it.viewMode == FolderViewMode.GRID) FolderViewMode.LIST
                else FolderViewMode.GRID
            )
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun showEditDialog(folder: Folder) {
        _uiState.update { it.copy(showEditDialog = true, editingFolder = folder) }
    }

    fun dismissEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingFolder = null) }
    }

    fun updateFolder(folder: Folder, newName: String, newIcon: String) {
        viewModelScope.launch {
            folderDao.update(
                FolderEntity(
                    id = folder.id,
                    name = newName,
                    icon = newIcon,
                    isSmartFolder = folder.isSmartFolder,
                    sortRule = folder.sortRule
                )
            )
            _uiState.update { it.copy(showEditDialog = false, editingFolder = null) }
        }
    }
}

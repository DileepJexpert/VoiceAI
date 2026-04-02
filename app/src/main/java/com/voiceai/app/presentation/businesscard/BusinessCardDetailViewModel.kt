package com.voiceai.app.presentation.businesscard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.ScannedContact
import com.voiceai.app.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BusinessCardDetailUiState(
    val contact: ScannedContact? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSavedToContacts: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class BusinessCardDetailViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Long = savedStateHandle["contactId"] ?: 0L

    private val _uiState = MutableStateFlow(BusinessCardDetailUiState())
    val uiState: StateFlow<BusinessCardDetailUiState> = _uiState.asStateFlow()

    init {
        loadContact()
    }

    private fun loadContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val contact = contactRepository.getById(contactId)
            _uiState.update {
                it.copy(
                    contact = contact,
                    isLoading = false,
                    isSavedToContacts = contact?.isSavedToContacts ?: false
                )
            }
        }
    }

    fun updateField(field: String, value: String) {
        val current = _uiState.value.contact ?: return
        val updated = when (field) {
            "name" -> current.copy(name = value)
            "phone" -> current.copy(phone = value)
            "email" -> current.copy(email = value)
            "company" -> current.copy(company = value)
            "designation" -> current.copy(designation = value)
            "address" -> current.copy(address = value)
            "website" -> current.copy(website = value)
            else -> current
        }
        _uiState.update { it.copy(contact = updated) }
        viewModelScope.launch {
            contactRepository.update(updated)
        }
    }

    fun saveToContacts() {
        val contact = _uiState.value.contact ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val success = contactRepository.saveToDeviceContacts(contact)
            if (success) {
                val updated = contact.copy(isSavedToContacts = true)
                contactRepository.update(updated)
                _uiState.update {
                    it.copy(
                        contact = updated,
                        isSaving = false,
                        isSavedToContacts = true
                    )
                }
            } else {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun shareVCard() {
        // vCard generation and share intent would be triggered from the Screen
        // This method prepares the vCard string content
    }

    fun buildVCardString(): String {
        val contact = _uiState.value.contact ?: return ""
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:${contact.name}")
            contact.phone?.let { appendLine("TEL:$it") }
            contact.email?.let { appendLine("EMAIL:$it") }
            contact.company?.let { appendLine("ORG:$it") }
            contact.designation?.let { appendLine("TITLE:$it") }
            contact.address?.let { appendLine("ADR:;;$it") }
            contact.website?.let { appendLine("URL:$it") }
            appendLine("END:VCARD")
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteContact() {
        viewModelScope.launch {
            contactRepository.delete(contactId)
            _uiState.update { it.copy(showDeleteDialog = false, isDeleted = true) }
        }
    }
}

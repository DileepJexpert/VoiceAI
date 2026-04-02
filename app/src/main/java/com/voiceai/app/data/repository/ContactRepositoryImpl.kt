package com.voiceai.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract
import com.voiceai.app.data.local.dao.ScannedContactDao
import com.voiceai.app.data.local.entity.ScannedContactEntity
import com.voiceai.app.domain.model.ScannedContact
import com.voiceai.app.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val scannedContactDao: ScannedContactDao,
    @ApplicationContext private val context: Context
) : ContactRepository {

    override fun getAll(): Flow<List<ScannedContact>> {
        return scannedContactDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): ScannedContact? {
        return scannedContactDao.getById(id)?.toDomain()
    }

    override fun search(query: String): Flow<List<ScannedContact>> {
        return scannedContactDao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(contact: ScannedContact): Long {
        return scannedContactDao.insert(contact.toEntity())
    }

    override suspend fun update(contact: ScannedContact) {
        scannedContactDao.update(contact.toEntity())
    }

    override suspend fun delete(id: Long) {
        scannedContactDao.deleteById(id)
    }

    override suspend fun saveToDeviceContacts(contact: ScannedContact): Boolean {
        return try {
            val contentResolver = context.contentResolver

            val rawContactValues = ContentValues()
            val rawContactUri = contentResolver.insert(
                ContactsContract.RawContacts.CONTENT_URI,
                rawContactValues
            ) ?: return false

            val rawContactId = rawContactUri.lastPathSegment?.toLongOrNull() ?: return false

            // Insert display name
            val nameValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
            }
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

            // Insert phone number
            contact.phone?.let { phone ->
                val phoneValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                }
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
            }

            // Insert email
            contact.email?.let { email ->
                val emailValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Email.DATA, email)
                    put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                }
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, emailValues)
            }

            // Insert company and designation
            if (contact.company != null || contact.designation != null) {
                val orgValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    contact.company?.let { put(ContactsContract.CommonDataKinds.Organization.COMPANY, it) }
                    contact.designation?.let { put(ContactsContract.CommonDataKinds.Organization.TITLE, it) }
                }
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, orgValues)
            }

            // Insert address
            contact.address?.let { address ->
                val addressValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
                }
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, addressValues)
            }

            // Insert website
            contact.website?.let { website ->
                val websiteValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Website.URL, website)
                    put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_WORK)
                }
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, websiteValues)
            }

            // Mark as saved in local database
            scannedContactDao.update(contact.copy(isSavedToContacts = true).toEntity())

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun ScannedContactEntity.toDomain(): ScannedContact {
        return ScannedContact(
            id = id,
            name = name,
            phone = phone,
            email = email,
            company = company,
            designation = designation,
            address = address,
            website = website,
            scanId = scanId,
            imagePath = imagePath,
            isSavedToContacts = isSavedToContacts,
            createdAt = createdAt
        )
    }

    private fun ScannedContact.toEntity(): ScannedContactEntity {
        return ScannedContactEntity(
            id = id,
            name = name,
            phone = phone,
            email = email,
            company = company,
            designation = designation,
            address = address,
            website = website,
            scanId = scanId,
            imagePath = imagePath,
            isSavedToContacts = isSavedToContacts,
            createdAt = createdAt
        )
    }
}

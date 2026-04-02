package com.voiceai.app.di

import com.voiceai.app.data.repository.AISummaryRepositoryImpl
import com.voiceai.app.data.repository.AudioRepositoryImpl
import com.voiceai.app.data.repository.OcrRepositoryImpl
import com.voiceai.app.data.repository.PdfRepositoryImpl
import com.voiceai.app.data.repository.ScannedDocumentRepositoryImpl
import com.voiceai.app.data.repository.SpeechToTextRepositoryImpl
import com.voiceai.app.data.repository.TtsRepositoryImpl
import com.voiceai.app.data.repository.VoiceNoteRepositoryImpl
import com.voiceai.app.domain.repository.AISummaryRepository
import com.voiceai.app.domain.repository.AudioRepository
import com.voiceai.app.domain.repository.OcrRepository
import com.voiceai.app.domain.repository.PdfRepository
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.domain.repository.SpeechToTextRepository
import com.voiceai.app.domain.repository.TtsRepository
import com.voiceai.app.domain.repository.VoiceNoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVoiceNoteRepository(
        impl: VoiceNoteRepositoryImpl
    ): VoiceNoteRepository

    @Binds
    @Singleton
    abstract fun bindScannedDocumentRepository(
        impl: ScannedDocumentRepositoryImpl
    ): ScannedDocumentRepository

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        impl: AudioRepositoryImpl
    ): AudioRepository

    @Binds
    @Singleton
    abstract fun bindAISummaryRepository(
        impl: AISummaryRepositoryImpl
    ): AISummaryRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        impl: OcrRepositoryImpl
    ): OcrRepository

    @Binds
    @Singleton
    abstract fun bindTtsRepository(
        impl: TtsRepositoryImpl
    ): TtsRepository

    @Binds
    @Singleton
    abstract fun bindSpeechToTextRepository(
        impl: SpeechToTextRepositoryImpl
    ): SpeechToTextRepository

    @Binds
    @Singleton
    abstract fun bindPdfRepository(
        impl: PdfRepositoryImpl
    ): PdfRepository
}

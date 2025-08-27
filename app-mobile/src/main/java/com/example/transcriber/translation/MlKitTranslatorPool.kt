package com.example.transcriber.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * Helper that downloads ML Kit translation models on demand and performs
 * translations entirely on-device.
 */
object MlKitTranslatorPool {
    private suspend fun getTranslator(src: String, dst: String): Translator? {
        val srcLang = TranslateLanguage.fromLanguageTag(src)
        val dstLang = TranslateLanguage.fromLanguageTag(dst)

        if (srcLang == null || dstLang == null) {
            return null
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(srcLang)
            .setTargetLanguage(dstLang)
            .build()
        val translator = Translation.getClient(options)
        val cond = DownloadConditions.Builder().requireWifi().build()
        translator.downloadModelIfNeeded(cond).await()
        return translator
    }

    suspend fun translate(src: String, dst: String, text: String): String {
        val tr = getTranslator(src, dst)
            ?: return "Unsupported language tag: $src or $dst"
        return tr.translate(text).await()
    }
}

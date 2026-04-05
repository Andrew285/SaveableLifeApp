package org.simpleapps.saveable.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.simpleapps.saveable.domain.translation.Definition
import org.simpleapps.saveable.domain.translation.TranslationResult
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Calls https://ftapi.pythonanywhere.com/translate and maps the JSON response
 * to [TranslationResult].
 *
 * Uses only java.net + org.json (already on the JVM classpath via Android/desktop).
 * No Retrofit needed — keeps this use-case self-contained.
 */
class TranslateUseCase {

    suspend operator fun invoke(text: String, sourceLang: String = "en", destLang: String = "uk"): TranslationResult =
        withContext(Dispatchers.IO) {
            val encodedText = URLEncoder.encode(text.trim(), "UTF-8")
            val url = "https://ftapi.pythonanywhere.com/translate?sl=$sourceLang&dl=$destLang&text=$encodedText"

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout    = 15_000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                throw Exception("Translation API error: HTTP $responseCode")
            }

            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
            connection.disconnect()

            parseResponse(body)
        }

    private fun parseResponse(json: String): TranslationResult {
        val root = JSONObject(json)

        // ── Pronunciation ────────────────────────────────────────────────
        val pronunciation = root.optJSONObject("pronunciation")
        val phonetic      = pronunciation?.optString("source-text-phonetic")?.takeIf { it.isNotBlank() }
        val srcAudio      = pronunciation?.optString("source-text-audio")?.takeIf { it.isNotBlank() }
        val dstAudio      = pronunciation?.optString("destination-text-audio")?.takeIf { it.isNotBlank() }

        // ── Possible translations ────────────────────────────────────────
        val translationsObj     = root.optJSONObject("translations")
        val possibleArr         = translationsObj?.optJSONArray("possible-translations")
        val possibleTranslations = buildList {
            if (possibleArr != null) {
                for (i in 0 until possibleArr.length()) add(possibleArr.getString(i))
            }
        }

        // ── Definitions ──────────────────────────────────────────────────
        val defsArr = root.optJSONArray("definitions")
        val definitions = buildList {
            if (defsArr != null) {
                for (i in 0 until defsArr.length()) {
                    val defObj = defsArr.getJSONObject(i)
                    val pos        = defObj.optString("part-of-speech", "")
                    val definition = defObj.optString("definition", "")
                    val example    = defObj.optString("example").takeIf { it.isNotBlank() }

                    // Synonyms can be a JSONObject with multiple groups or null
                    val synonymsList = mutableListOf<String>()
                    val synRaw = defObj.opt("synonyms")
                    if (synRaw is JSONObject) {
                        val keys = synRaw.keys()
                        while (keys.hasNext()) {
                            val key     = keys.next()
                            val synArr  = synRaw.optJSONArray(key)
                            if (synArr != null) {
                                for (j in 0 until synArr.length()) synonymsList.add(synArr.getString(j))
                            }
                        }
                    }

                    if (definition.isNotBlank()) {
                        add(Definition(pos, definition, example, synonymsList))
                    }
                }
            }
        }

        return TranslationResult(
            sourceText           = root.optString("source-text"),
            destinationText      = root.optString("destination-text"),
            sourceLanguage       = root.optString("source-language"),
            destinationLanguage  = root.optString("destination-language"),
            phonetic             = phonetic,
            sourceAudioUrl       = srcAudio,
            destinationAudioUrl  = dstAudio,
            possibleTranslations = possibleTranslations,
            definitions          = definitions
        )
    }
}
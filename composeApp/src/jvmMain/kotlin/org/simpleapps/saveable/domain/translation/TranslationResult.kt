package org.simpleapps.saveable.domain.translation

data class TranslationResult(
    val sourceText          : String,
    val destinationText     : String,
    val sourceLanguage      : String,
    val destinationLanguage : String,
    val phonetic            : String?,
    val sourceAudioUrl      : String?,
    val destinationAudioUrl : String?,
    val possibleTranslations: List<String>,
    val definitions         : List<Definition>
)

data class Definition(
    val partOfSpeech: String,
    val definition  : String,
    val example     : String?,
    val synonyms    : List<String>   // flattened from all synonym groups
)
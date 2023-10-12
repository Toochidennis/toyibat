package com.digitaldream.toyibatskool.models


data class SectionModel(
    var sectionId: String? = null,
    var sectionTitle: String?,
    var questionItem: QuestionItem?,
    val viewType: String,
)

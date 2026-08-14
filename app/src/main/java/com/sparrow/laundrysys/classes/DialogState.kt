package com.sparrow.laundrysys.classes

// State class for confirmation dialogs
data class DialogState(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit
)
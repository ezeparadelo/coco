package com.coco.app.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de ajustes (interfaz para poder usar fakes en tests del ViewModel sin Android).
 */
interface SettingsRepository {
    /** Si está activo, al abrir la app se muestra primero el historial. */
    val startInHistory: StateFlow<Boolean>
    fun setStartInHistory(value: Boolean)

    /** Si está activo, se desactivan las animaciones de apertura y UI para máxima velocidad. */
    val fastMode: StateFlow<Boolean>
    fun setFastMode(value: Boolean)

    /** Si el usuario ya vio la bienvenida de onboarding. */
    val hasSeenOnboarding: StateFlow<Boolean>
    fun completeOnboarding()

    /** Si está activo, la tecla Enter del teclado guarda la nota. Si es false, añade un salto de línea. */
    val enterToSubmit: StateFlow<Boolean>
    fun setEnterToSubmit(value: Boolean)
}

package com.fincaria.farms.domain

/**
 * Whether a farm's cadastral reference was verified against the external
 * cadastral registry. Modelled as a sealed type so the enriched details can
 * only exist on the verified branch: a "verified without details" (or the
 * reverse) state is unrepresentable rather than guarded at runtime.
 */
sealed interface CadastralVerification {
    data class Verified(val details: CadastralDetails) : CadastralVerification

    data object Unverified : CadastralVerification
}

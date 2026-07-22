package com.parodison.sgp4.init

/**
 * Régimen orbital según el período. Deep-space (SDP4, período >= 225 min, con
 * perturbaciones lunisolares y resonancias) no está soportado por este motor.
 */
internal enum class OrbitRegime {
    NEAR_EARTH,
    DEEP_SPACE,
}

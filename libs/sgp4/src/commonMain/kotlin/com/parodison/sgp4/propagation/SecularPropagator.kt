package com.parodison.sgp4.propagation

import com.parodison.sgp4.constants.WGS72Constants.TWO_PI
import com.parodison.sgp4.constants.WGS72Constants.X2O3
import com.parodison.sgp4.constants.WGS72Constants.XKE
import com.parodison.sgp4.init.RecoveredElements
import com.parodison.sgp4.init.SecularDragCoefficients
import com.parodison.sgp4.init.SecularGravityCoefficients
import com.parodison.sgp4.model.MeanElements
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** Aplica arrastre atmosférico + achatamiento terrestre (J2/J4) a los elementos medios en el instante t. */
internal object SecularPropagator {

    fun computeSecularEffects(
        minutesSinceEpoch: Double,
        elements: MeanElements,
        recovered: RecoveredElements,
        gravity: SecularGravityCoefficients,
        drag: SecularDragCoefficients,
    ): SecularEffects {
        val t = minutesSinceEpoch
        val t2 = t * t

        val xmdf = elements.meanAnomalyRad + gravity.meanAnomalyDot * t
        val argpdf = elements.argPerigeeRad + gravity.argPerigeeDot * t
        val nodedf = elements.raanRad + gravity.nodeDot * t

        var argpm = argpdf
        var mm = xmdf
        val nodem = nodedf + drag.nodecf * t2
        var tempa = 1.0 - drag.cc1 * t
        var tempe = elements.bstar * drag.cc4 * t
        var templ = drag.t2cof * t2

        if (!drag.isSimplifiedDrag) {
            val delomg = drag.omgcof * t
            val delmtemp = 1.0 + drag.eta * cos(xmdf)
            val delm = drag.xmcof * (delmtemp.pow(3) - drag.delmo)
            val temp = delomg + delm
            mm = xmdf + temp
            argpm = argpdf - temp
            val t3 = t2 * t
            val t4 = t3 * t
            tempa -= drag.d2 * t2 + drag.d3 * t3 + drag.d4 * t4
            tempe += elements.bstar * drag.cc5 * (sin(mm) - drag.sinmao)
            templ += drag.t3cof * t3 + t4 * (drag.t4cof + t * drag.t5cof)
        }

        val originalMeanMotion = recovered.meanMotionRadPerMin
        require(originalMeanMotion > 0.0) { "Movimiento medio inválido durante la propagación" }

        val am = (XKE / originalMeanMotion).pow(X2O3) * tempa * tempa
        val nm = XKE / am.pow(1.5)
        var em = elements.eccentricity - tempe

        check(em < 1.0 && em >= -0.001) { "Excentricidad fuera de rango durante la propagación: $em" }
        if (em < 1.0e-6) em = 1.0e-6

        mm += originalMeanMotion * templ
        val xlm = mm + argpm + nodem

        val nodemMod = nodem.mod(TWO_PI)
        val argpmMod = argpm.mod(TWO_PI)
        val xlmMod = xlm.mod(TWO_PI)
        val mmMod = (xlmMod - argpmMod - nodemMod).mod(TWO_PI)

        return SecularEffects(
            semiMajorAxis = am,
            meanMotion = nm,
            eccentricity = em,
            meanAnomaly = mmMod,
            argPerigee = argpmMod,
            node = nodemMod,
        )
    }
}

package com.parodison.orbital.system.models

import com.parodison.sgp4.SGP4Engine
import com.parodison.sgp4.Satellite
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.PI
import kotlin.time.Instant


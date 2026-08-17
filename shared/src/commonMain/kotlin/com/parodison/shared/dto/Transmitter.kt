package com.parodison.shared.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/** SatNOGS DB's `updated` timestamp always comes with a "Z" suffix, so it parses as-is. */
object SatnogsInstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("SatnogsInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}

/**
 * The modulation/waveform SatNOGS DB reports for a transmitter's `mode` (and `uplink_mode`)
 * field — what a demodulator needs to know how to decode the signal. [OTHER] is the fallback for
 * any raw value not in this list (e.g. a mode added to SatNOGS DB after this was written), so
 * deserialization never fails on an unrecognized mode.
 */
@Serializable(with = TransmitterModeSerializer::class)
enum class TransmitterMode(val rawValue: String) {
    FSK_4("4FSK"),
    QAM_64("64-QAM"),
    AFSK("AFSK"),
    AFSK_TUBIX10("AFSK TUBiX10"),
    AHRPT("AHRPT"),
    AM("AM"),
    APT("APT"),
    ASK("ASK"),
    BPSK("BPSK"),
    BPSK_PMT_A3("BPSK PMT-A3"),
    CERTO("CERTO"),
    CW("CW"),
    DATV("DATV"),
    DBPSK("DBPSK"),
    DOKA("DOKA"),
    DPSK("DPSK"),
    DQPSK("DQPSK"),
    DSB("DSB"),
    DSTAR("DSTAR"),
    DUV("DUV"),
    DVB_S2("DVB-S2"),
    FFSK("FFSK"),
    FM("FM"),
    FMN("FMN"),
    FSK("FSK"),
    FSK_AX100_MODE5("FSK AX.100 Mode 5"),
    FSK_AX100_MODE6("FSK AX.100 Mode 6"),
    FSK_AX25_G3RUH("FSK AX.25 G3RUH"),
    FT8("FT8"),
    GENESIS_FSK("GENESIS FSK"),
    GFSK("GFSK"),
    GFSK_PKST("GFSK Pkst"),
    GFSK_RKTR("GFSK Rktr"),
    GFSK_BPSK("GFSK/BPSK"),
    GMSK("GMSK"),
    GMSK_USP("GMSK USP"),
    HRPT("HRPT"),
    LORA("LoRa"),
    LRPT("LRPT"),
    LSB("LSB"),
    MFSK("MFSK"),
    MSK("MSK"),
    MSK_AX100_MODE5("MSK AX.100 Mode 5"),
    MSK_AX100_MODE6("MSK AX.100 Mode 6"),
    OFDM("OFDM"),
    OQPSK("OQPSK"),
    PPM("PPM"),
    PSK("PSK"),
    PSK31("PSK31"),
    PSK63("PSK63"),
    QPSK("QPSK"),
    QPSK31("QPSK31"),
    QPSK63("QPSK63"),
    SIDLOC("SIDLOC"),
    SQPSK("SQPSK"),
    SSDV("SSDV"),
    SSTV("SSTV"),
    UNKNOWN("UNKNOWN"),
    USB("USB"),
    WSJT("WSJT"),
    OTHER(""),
}

object TransmitterModeSerializer : KSerializer<TransmitterMode> {
    override val descriptor = PrimitiveSerialDescriptor("TransmitterMode", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TransmitterMode {
        val raw = decoder.decodeString()
        return TransmitterMode.entries.firstOrNull { it.rawValue == raw } ?: TransmitterMode.OTHER
    }

    override fun serialize(encoder: Encoder, value: TransmitterMode) = encoder.encodeString(value.rawValue)
}

@Serializable
data class ItuNotification(
    val urls: List<String> = emptyList(),
)

@Serializable
data class Transmitter(
    val uuid: String,
    val description: String,
    val alive: Boolean,
    val type: String,
    @SerialName("uplink_low")
    val uplinkLow: Long?,
    @SerialName("uplink_high")
    val uplinkHigh: Long?,
    @SerialName("uplink_drift")
    val uplinkDrift: Long?,
    @SerialName("downlink_low")
    val downlinkLow: Long?,
    @SerialName("downlink_high")
    val downlinkHigh: Long?,
    @SerialName("downlink_drift")
    val downlinkDrift: Long?,
    val mode: TransmitterMode?,
    @SerialName("mode_id")
    val modeId: Int?,
    @SerialName("uplink_mode")
    val uplinkMode: TransmitterMode?,
    val invert: Boolean,
    val baud: Double?,
    @SerialName("sat_id")
    val satId: String,
    @SerialName("norad_cat_id")
    val noradCatId: Long,
    @SerialName("norad_follow_id")
    val noradFollowId: Long?,
    val status: String,
    @Serializable(with = SatnogsInstantSerializer::class)
    val updated: Instant,
    val citation: String,
    val service: String,
    @SerialName("iaru_coordination")
    val iaruCoordination: String,
    @SerialName("iaru_coordination_url")
    val iaruCoordinationUrl: String,
    @SerialName("itu_notification")
    val ituNotification: ItuNotification,
    @SerialName("frequency_violation")
    val frequencyViolation: Boolean,
    val unconfirmed: Boolean,
)
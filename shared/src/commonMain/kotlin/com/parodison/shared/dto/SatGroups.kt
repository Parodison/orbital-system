package com.parodison.shared.dto

/**
 * The "Communications Satellites" groups Celestrak exposes at
 * https://celestrak.org/NORAD/elements/index.php — [groupValue] is what goes in the
 * `GROUP` query parameter of `gp.php` (e.g. `gp.php?GROUP=starlink&FORMAT=json`).
 */
enum class SatGroup(val displayName: String, val groupValue: String) {
    ACTIVE_GEOSYNCHRONOUS("Active Geosynchronous", "geo"),
    INTELSAT("Intelsat", "intelsat"),
    SES("SES", "ses"),
    EUTELSAT("Eutelsat", "eutelsat"),
    TELESAT("Telesat", "telesat"),
    STARLINK("Starlink", "starlink"),
    ONEWEB("OneWeb", "oneweb"),
    QIANFAN("Qianfan", "qianfan"),
    HULIANWANG("Hulianwang Digui", "hulianwang"),
    KUIPER("Kuiper", "kuiper"),
    IRIDIUM_NEXT("Iridium NEXT", "iridium-NEXT"),
    ORBCOMM("Orbcomm", "orbcomm"),
    GLOBALSTAR("Globalstar", "globalstar"),
    AMATEUR_RADIO("Amateur Radio", "amateur"),
    SATNOGS("SatNOGS", "satnogs"),
    EXPERIMENTAL_COMM("Experimental Comm", "x-comm"),
    OTHER_COMM("Other Comm", "other-comm"),
}

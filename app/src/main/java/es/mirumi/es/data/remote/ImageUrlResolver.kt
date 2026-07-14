package es.mirumi.es.data.remote

import es.mirumi.es.BuildConfig

/**
 * Resolves a photo/multimedia URL received from the backend into a fully absolute URL that Coil can fetch.
 *
 * Handles three shapes robustly:
 *   1. Already absolute (`http://…` or `https://…`) — returned as-is.
 *   2. Root-relative path (`/multimedia/foo.jpg`) — prepends the server origin from `BASE_URL`.
 *   3. Bare filename (`foo.jpg`) — prepends `BASE_URL` + `multimedia/`.
 *
 * Returns null for null / blank input.
 */
fun resolveImageUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null

    if (raw.startsWith("http://") || raw.startsWith("https://")) {
        return raw
    }

    val base = BuildConfig.BASE_URL.trimEnd('/')
    // BASE_URL typically ends in "/api/"; the multimedia handler is served from the server root, so drop trailing `/api`.
    val serverOrigin =
        if (base.endsWith("/api")) {
            base.removeSuffix("/api")
        } else {
            base
        }

    return if (raw.startsWith("/")) {
        "$serverOrigin$raw"
    } else {
        "$serverOrigin/multimedia/$raw"
    }
}

package com.insane.eyewalk.config

object AppConfig {

    /**
     * API base - host and end-points
     */
    object API {

        private const val hostBase: String = "https://api.eyewalk.42g.com.br"
        private const val api: String = "/api/v1"
        private const val pictureResource: String = "/api/v1/contact"

        fun getHostBase(): String {
            return hostBase;
        }

        fun getHost(): String {
            return "${hostBase}${api}";
        }

        fun getPictureUrl(id: Long, filename: String): String {
            return "${hostBase}${pictureResource}/${id}/${filename}"
        }

    }

}
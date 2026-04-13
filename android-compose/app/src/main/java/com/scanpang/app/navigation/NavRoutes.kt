package com.scanpang.app.navigation

/** Bottom-level tab graphs (matches RN MainTabParamList). */
object TabRoutes {
    const val HOME_TAB = "home_tab"
    const val SEARCH_TAB = "search_tab"
    const val AR_TAB = "ar_tab"
    const val SAVED_TAB = "saved_tab"
    const val PROFILE_TAB = "profile_tab"
}

/** Home stack */
object HomeRoutes {
    const val MAIN = "home_main"
    const val QIBLA = "home_qibla"
    const val NEARBY_HALAL = "home_nearby_halal"
    const val NEARBY_PRAYER = "home_nearby_prayer"
    const val RESTAURANT_DETAIL = "home_restaurant_detail"
    const val PRAYER_ROOM_DETAIL = "home_prayer_room_detail"
}

/** Search stack */
object SearchRoutes {
    const val MAIN = "search_main"
    const val RESULTS = "search_results"
    const val RESTAURANT_DETAIL = "search_restaurant_detail"
    const val PRAYER_ROOM_DETAIL = "search_prayer_room_detail"
}

/** AR stack */
object ArRoutes {
    const val EXPLORE = "ar_explore"
    const val CHAT = "ar_explore_chat"
    const val RECOMMENDED = "ar_recommended"
    const val NAV_MAP = "ar_nav_map"
    const val NAV_AGENT = "ar_nav_agent"
    const val ALMOST_ARRIVAL = "ar_almost_arrival"
    const val ARRIVED = "ar_arrived"
}

/** Saved stack */
object SavedRoutes {
    const val MAIN = "saved_main"
    const val RESTAURANT_DETAIL = "saved_restaurant_detail"
    const val PRAYER_ROOM_DETAIL = "saved_prayer_room_detail"
}

object ProfileRoutes {
    const val MAIN = "profile_main"
}

package com.example.androidpractices.domain.model

/**
 * Тип пивоварни. [apiValue] совпадает со значением поля `brewery_type` в Open Brewery DB
 * и с параметром фильтра `by_type`.
 */
enum class BreweryType(
    val apiValue: String,
    val title: String,
    val description: String,
) {
    MICRO(
        "micro",
        "Микропивоварня",
        "Большинство крафтовых пивоварен. Небольшие объёмы, свой taproom и эксперименты с сортами.",
    ),
    NANO(
        "nano",
        "Нанопивоварня",
        "Крошечная пивоварня, которая обычно продаёт пиво только в своём регионе.",
    ),
    REGIONAL(
        "regional",
        "Региональная",
        "Крупная крафтовая пивоварня с дистрибуцией в нескольких штатах.",
    ),
    BREWPUB(
        "brewpub",
        "Бар-пивоварня",
        "Бар или ресторан со своей пивоварней: пиво варят и наливают в одном месте.",
    ),
    LARGE(
        "large",
        "Крупная",
        "Очень большая пивоварня, чаще всего не относящаяся к крафту.",
    ),
    PLANNING(
        "planning",
        "В планах",
        "Пивоварня ещё готовится к открытию.",
    ),
    BAR(
        "bar",
        "Бар",
        "Бар без собственного производства, но с упором на крафтовое пиво.",
    ),
    CONTRACT(
        "contract",
        "Контрактная",
        "Варит пиво на мощностях другой пивоварни.",
    ),
    PROPRIETOR(
        "proprietor",
        "Арендная",
        "Арендует оборудование у другой пивоварни, но варит по своим рецептам.",
    ),
    CLOSED(
        "closed",
        "Закрыта",
        "Пивоварня закрылась и больше не работает.",
    ),
    UNKNOWN(
        "unknown",
        "Другое",
        "Тип пивоварни не указан.",
    );

    companion object {
        /** Типы, по которым можно фильтровать список. */
        val filterable: List<BreweryType> = entries.filter { it != UNKNOWN }

        fun fromApi(value: String?): BreweryType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

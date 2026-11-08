package com.example.androidpractices.domain.model

/**
 * Настройки фильтрации списка пивоварен (не больше трёх, как требует задание):
 * - [query] — поиск по названию (`by_name`);
 * - [state] — штат США (`by_state`), полное название, например «New York»;
 * - [type] — тип пивоварни (`by_type`).
 *
 * Пустые значения означают «без фильтра».
 */
data class BreweryFilters(
    val query: String = "",
    val state: String? = null,
    val type: BreweryType? = null,
) {
    /** Страница в состоянии по умолчанию — бейдж не нужен. */
    val isDefault: Boolean get() = query.isBlank() && state == null && type == null

    /** Сколько фильтров включено — для подписи «Фильтры · 2». */
    val activeCount: Int get() = listOf(query.isNotBlank(), state != null, type != null).count { it }

    companion object {
        val Default = BreweryFilters()
    }
}

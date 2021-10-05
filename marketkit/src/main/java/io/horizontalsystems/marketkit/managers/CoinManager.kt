package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class CoinManager(
    private val storage: CoinStorage,
    private val hsProvider: HsProvider,
    private val categoryManager: CoinCategoryManager
) {
    val fullCoinsUpdatedObservable = PublishSubject.create<Unit>()

    fun fullCoins(filter: String, limit: Int): List<FullCoin> {
        return storage.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return storage.fullCoins(coinUids)
    }

    fun fullCoinsByCoinTypes(coinTypes: List<CoinType>): List<FullCoin> {
        val platformCoins = storage.platformCoins(coinTypes)

        return storage.fullCoins(platformCoins.map { it.coin.uid })
    }

    fun marketInfosSingle(top: Int, limit: Int?, order: MarketInfo.Order?): Single<List<MarketInfo>> {
        return hsProvider.getMarketInfosSingle(top, limit, order)
    }

    fun marketInfosSingle(coinUids: List<String>, order: MarketInfo.Order?): Single<List<MarketInfo>> {
        return hsProvider.getMarketInfosSingle(coinUids, order)
    }

    fun marketInfoOverviewSingle(coinUid: String, currencyCode: String, language: String): Single<MarketInfoOverview> {
        return hsProvider.getMarketInfoOverview(coinUid, currencyCode, language)
            .map { overviewRaw ->
                val categoriesMap = categoryManager.coinCategories(overviewRaw.categoryIds)
                    .map { it.uid to it }
                    .toMap()

                val performance = overviewRaw.performance
                    .map { (vsCurrency, v) ->
                        vsCurrency to v.mapNotNull { (timePeriodRaw, performance) ->
                            if (performance == null) return@mapNotNull null
                            val timePeriod = TimePeriod.fromString(timePeriodRaw) ?: return@mapNotNull null

                            timePeriod to performance
                        }.toMap()
                    }.toMap()

                val links = overviewRaw.links
                    .mapNotNull { (linkTypeRaw, link) ->
                        LinkType.fromString(linkTypeRaw)?.let {
                            it to link
                        }
                    }.toMap()

                MarketInfoOverview(
                    overviewRaw.marketData.marketCap,
                    overviewRaw.marketData.marketCapRank,
                    overviewRaw.marketData.totalSupply,
                    overviewRaw.marketData.circulatingSupply,
                    overviewRaw.marketData.volume24h,
                    overviewRaw.marketData.dilutedMarketCap,
                    overviewRaw.marketData.tvl,
                    performance,
                    overviewRaw.genesisDate,
                    overviewRaw.categoryIds.mapNotNull { categoriesMap[it] },
                    overviewRaw.description,
                    links,
                )
            }
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return storage.platformCoin(coinType)
    }

    fun platformCoins(): List<PlatformCoin> {
        return storage.platformCoins()
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return storage.platformCoins(coinTypes)
    }

    fun platformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin> {
        return storage.platformCoinsByCoinTypeIds(coinTypeIds)
    }

    fun coins(filter: String, limit: Int): List<Coin> {
        return storage.coins(filter, limit)
    }

    fun handleFetched(fullCoins: List<FullCoin>) {
        storage.save(fullCoins)
        fullCoinsUpdatedObservable.onNext(Unit)
    }

}

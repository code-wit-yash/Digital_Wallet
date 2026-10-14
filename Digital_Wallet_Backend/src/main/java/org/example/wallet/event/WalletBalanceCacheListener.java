//package org.example.wallet.event;
//
//import org.springframework.cache.Cache;
//import org.springframework.cache.CacheManager;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Component
//public class WalletBalanceCacheListener {
//
//    private final CacheManager cacheManager;
//
//    public WalletBalanceCacheListener(CacheManager cacheManager) {
//        this.cacheManager = cacheManager;
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleWalletBalanceChanged(
//            WalletBalanceChangedEvent event) {
//
//        Cache cache = cacheManager.getCache("walletBalance");
//
//        if (cache == null) {
//            return;
//        }
//
//        for (Long userId : event.userIds()) {
//            if (userId != null) {
//                cache.evict(userId);
//            }
//        }
//    }
//}
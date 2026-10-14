//package org.example.wallet.service;
//
//import org.example.wallet.dto.WalletBalanceResponse;
//import org.example.wallet.repository.WalletRepository;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.NoSuchElementException;
//
//@Service
//public class WalletBalanceCacheService {
//
//    private final WalletRepository walletRepository;
//
//    public WalletBalanceCacheService(WalletRepository walletRepository) {
//        this.walletRepository = walletRepository;
//    }
//
//    @Cacheable(value = "walletBalance", key = "#userId")
//    @Transactional(readOnly = true)
//    public WalletBalanceResponse getWalletBalance(Long userId) {
//        return walletRepository.findBalanceByUserId(userId)
//                .orElseThrow(() ->
//                        new NoSuchElementException("Wallet not found"));
//    }
//}

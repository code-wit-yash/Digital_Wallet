package org.example.wallet.service;

import org.example.transaction.entity.IdempotencyRecord;
import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.example.wallet.entity.Wallet;
import org.example.wallet.repository.WalletRepository;
import org.example.transaction.entity.WalletTransaction;
import org.example.transaction.repository.WalletTransactionRepository;
import org.example.transaction.repository.IdempotencyRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRepository;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        wallet = mock(Wallet.class);
    }

    @Test
    void deposit_shouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.deposit(
                        "user@example.com", BigDecimal.ZERO)
        );

        verifyNoInteractions(
                userRepository,
                walletRepository,
                transactionRepository
        );
    }

    @Test
    void deposit_shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.deposit(
                        "user@example.com",
                        new BigDecimal("-10.00"))
        );

        verifyNoInteractions(
                userRepository,
                walletRepository,
                transactionRepository
        );
    }

    @Test
    void deposit_shouldRejectAmountWithMoreThanTwoDecimalPlaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.deposit(
                        "user@example.com",
                        new BigDecimal("10.123"))
        );

        verifyNoInteractions(
                userRepository,
                walletRepository,
                transactionRepository
        );
    }

    @Test
    void deposit_shouldCreditWalletAndSaveTransaction() {
        BigDecimal amount = new BigDecimal("100.00");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletTransaction result =
                walletService.deposit("user@example.com", amount);

        assertNotNull(result);

        verify(wallet).credit(amount);
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void withdraw_shouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.withdraw(
                        "user@example.com", BigDecimal.ZERO)
        );

        verifyNoInteractions(
                userRepository,
                walletRepository,
                transactionRepository
        );
    }

    @Test
    void withdraw_shouldDebitWalletAndSaveTransaction() {
        BigDecimal amount = new BigDecimal("25.00");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletTransaction result =
                walletService.withdraw("user@example.com", amount);

        assertNotNull(result);

        verify(wallet).debit(amount);
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void withdraw_shouldRejectInsufficientBalance() {
        BigDecimal amount = new BigDecimal("700.00");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(wallet).debit(amount);

        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.withdraw("user@example.com", amount)
        );

        verify(wallet).debit(amount);
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void debit_shouldRejectInsufficientBalance() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        testWallet.credit(new BigDecimal("100.00"));

        BigDecimal originalBalance = testWallet.getBalance();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.debit(new BigDecimal("150.00"))
        );

        assertEquals("Insufficient wallet balance", exception.getMessage());
        assertEquals(0, originalBalance.compareTo(testWallet.getBalance()));
    }

    @Test
    void debit_shouldRejectZeroAmount() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.debit(BigDecimal.ZERO)
        );

        assertEquals(
                "Withdrawal amount must be greater than zero",
                exception.getMessage()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(testWallet.getBalance())
        );
    }


    @Test
    void debit_shouldRejectNegativeAmount() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.debit(new BigDecimal("-10.00"))
        );

        assertEquals(
                "Withdrawal amount must be greater than zero",
                exception.getMessage()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(testWallet.getBalance())
        );
    }

    @Test
    void credit_shouldRejectZeroAmount() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.credit(BigDecimal.ZERO)
        );

        assertEquals(
                "Deposit amount must be greater than zero",
                exception.getMessage()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(testWallet.getBalance())
        );
    }

    @Test
    void credit_shouldRejectNegativeAmount() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.credit(new BigDecimal("-10.00"))
        );

        assertEquals(
                "Deposit amount must be greater than zero",
                exception.getMessage()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(testWallet.getBalance())
        );
    }

    @Test
    void credit_shouldIncreaseBalance() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        testWallet.credit(new BigDecimal("100.00"));
        testWallet.credit(new BigDecimal("50.00"));

        assertEquals(
                0,
                new BigDecimal("150.00")
                        .compareTo(testWallet.getBalance())
        );
    }

    @Test
    void debit_shouldDecreaseBalance() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        testWallet.credit(new BigDecimal("200.00"));

        System.out.println("Balance before withdrawal: "
                + testWallet.getBalance());

        testWallet.debit(new BigDecimal("75.00"));

        System.out.println("Balance after withdrawal: "
                + testWallet.getBalance());

        assertEquals(
                0,
                new BigDecimal("125.00")
                        .compareTo(testWallet.getBalance())
        );
    }

    @Test
    void credit_shouldRejectNullAmount() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.credit(null)
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(testWallet.getBalance())
        );
    }

    @Test
    void debit_shouldRejectNullAmount() {
        User testUser = mock(User.class);
        Wallet testWallet = new Wallet(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testWallet.debit(null)
        );

        System.out.println("Exception: " + exception.getMessage());
        System.out.println("Balance after rejected withdrawal: "
                + testWallet.getBalance());

        assertEquals(
                "Withdrawal amount must be greater than zero",
                exception.getMessage()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(testWallet.getBalance())
        );
    }

    @Test
    void transfer_shouldDebitSenderCreditRecipientAndSaveBothTransactions() {
        BigDecimal amount = new BigDecimal("50.00");
        String senderEmail = "sender@example.com";
        String recipientEmail = "recipient@example.com";
        String key = "transfer-test-001";

        User sender = mock(User.class);
        User recipient = mock(User.class);
        Wallet senderWallet = mock(Wallet.class);
        Wallet recipientWallet = mock(Wallet.class);
        IdempotencyRecord record = mock(IdempotencyRecord.class);

        when(userRepository.findByEmail(senderEmail))
                .thenReturn(Optional.of(sender));
        when(userRepository.findByEmail(recipientEmail))
                .thenReturn(Optional.of(recipient));

        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L))
                .thenReturn(Optional.of(recipientWallet));

        when(senderWallet.getId()).thenReturn(10L);
        when(recipientWallet.getId()).thenReturn(20L);

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(recipientWallet));

        when(record.getRequestHash()).thenReturn(
                requestHashForTest(recipientEmail, amount)
        );
        when(record.getResponseReferenceId()).thenReturn(null);

        when(idempotencyRepository.findForUpdate(senderEmail, key))
                .thenReturn(Optional.of(record));

        when(transactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletTransaction result = walletService.transfer(
                senderEmail, recipientEmail, amount, key
        );

        assertNotNull(result);

        verify(senderWallet).debit(amount);
        verify(recipientWallet).credit(amount);

        verify(transactionRepository, times(2))
                .save(any(WalletTransaction.class));

        verify(idempotencyRepository)
                .insertIfAbsent(eq(senderEmail), eq(key), anyString());

        verify(idempotencyRepository).save(record);
    }


    private String requestHashForTest(String recipientEmail, BigDecimal amount) {
        try {
            String canonicalRequest =
                    recipientEmail.trim().toLowerCase(java.util.Locale.ROOT)
                            + "|"
                            + amount.stripTrailingZeros().toPlainString();

            byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }


    @Test
    void transfer_shouldNotCreditRecipientWhenSenderHasInsufficientBalance() {
        BigDecimal amount = new BigDecimal("50.00");

        User sender = mock(User.class);
        User recipient = mock(User.class);
        Wallet senderWallet = mock(Wallet.class);
        Wallet recipientWallet = mock(Wallet.class);
        IdempotencyRecord record = mock(IdempotencyRecord.class);

        when(userRepository.findByEmail("sender@example.com"))
                .thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("recipient@example.com"))
                .thenReturn(Optional.of(recipient));

        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L))
                .thenReturn(Optional.of(recipientWallet));

        when(senderWallet.getId()).thenReturn(10L);
        when(recipientWallet.getId()).thenReturn(20L);

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(recipientWallet));

        when(record.getRequestHash())
                .thenReturn(requestHashForTest(
                        "recipient@example.com", amount));
        when(record.getResponseReferenceId()).thenReturn(null);

        when(idempotencyRepository.findForUpdate(
                "sender@example.com", "transfer-fail-001"))
                .thenReturn(Optional.of(record));

        doThrow(new IllegalArgumentException("Insufficient wallet balance"))
                .when(senderWallet).debit(amount);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transfer(
                        "sender@example.com",
                        "recipient@example.com",
                        amount,
                        "transfer-fail-001"
                ));

        verify(senderWallet).debit(amount);
        verify(recipientWallet, never()).credit(any());
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void transfer_shouldReturnOriginalTransactionOnRetry() {
        BigDecimal amount = new BigDecimal("50.00");
        String senderEmail = "sender@example.com";
        String recipientEmail = "recipient@example.com";
        String key = "transfer-retry-001";
        String originalReferenceId = "original-reference-123";

        User sender = mock(User.class);
        User recipient = mock(User.class);
        IdempotencyRecord record = mock(IdempotencyRecord.class);
        WalletTransaction originalTransaction = mock(WalletTransaction.class);

        when(userRepository.findByEmail(senderEmail))
                .thenReturn(Optional.of(sender));
        when(userRepository.findByEmail(recipientEmail))
                .thenReturn(Optional.of(recipient));

        when(record.getRequestHash())
                .thenReturn(requestHashForTest(recipientEmail, amount));
        when(record.getResponseReferenceId())
                .thenReturn(originalReferenceId);

        when(idempotencyRepository.findForUpdate(senderEmail, key))
                .thenReturn(Optional.of(record));

        when(transactionRepository.findByReferenceId(originalReferenceId))
                .thenReturn(Optional.of(originalTransaction));

        WalletTransaction result = walletService.transfer(
                senderEmail, recipientEmail, amount, key
        );

        assertSame(originalTransaction, result);

        verify(transactionRepository).findByReferenceId(originalReferenceId);
        verify(transactionRepository, never()).save(any(WalletTransaction.class));
        verify(idempotencyRepository, never()).save(any(IdempotencyRecord.class));
        verify(walletRepository, never()).findByUserIdForUpdate(anyLong());
    }

}

// class SlowTransactionRepositoryMock implements TransactionRepository {
//   @Override
//   public CompletableFuture<MonetaryAmount> sumBalance(Account account) {
//     try {
//       Thread.sleep(1000L);
//     } catch (InterruptedException e) {
//       e.printStackTrace();
//     }
    
//     return null;
//   }

//   @Override
//   public CompletableFuture<Transaction> create(Transaction transaction) {
//     return null;
//   }
// }

// private TransactionRepository slowTransactionRepositoryMock;

// this.slowTransactionRepositoryMock = new SlowTransactionRepositoryMock();

// @Test
// public void itShouldHandleConcurrentTest() {
//   Account accountMock1 = createUsdAccountMock("abc123", BigDecimal.TEN);
//   Account accountMock2 = createUsdAccountMock("abc321", BigDecimal.ZERO);
//   stubCreateTransaction();

//   accountService.transfer(
//     accountMock1.getId(), 
//     accountMock2.getId(), 
//     new MonetaryAmount(Currency.getInstance("USD"), BigDecimal.TEN)
//   );

//   accountService.transfer(
//     accountMock1.getId(), 
//     accountMock2.getId(), 
//     new MonetaryAmount(Currency.getInstance("USD"), BigDecimal.TEN)
//   ).whenComplete(
//     (result, exception) -> {
//       assertNotNull(exception);
//       assertEquals(exception.getClass(), InsufficientFundsException.class);
//     }
//   );
// }

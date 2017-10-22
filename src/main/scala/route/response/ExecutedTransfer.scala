package route.response

private[response] case class ExecutedTransfer(sourceAccount: String,
                                    destinationAccount: String,
                                    amount: BigDecimal)

package route.response

case class ExecutedTransfers(transfers: Seq[ExecutedTransfer])

object ExecutedTransfers {
  def fromDomain(records: domain.TransferRecords): ExecutedTransfers =
    ExecutedTransfers(
      records.records.map(r =>
        ExecutedTransfer(
          sourceAccount = r.from.number,
          destinationAccount = r.to.number,
          amount = r.amount.underlying
        ))
    )
}

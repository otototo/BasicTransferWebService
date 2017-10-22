package object domain {
  case class Account(number : String)
  case class Amount(underlying: BigDecimal)
  case class TransferRecord(to: Account,
                            from: Account,
                            amount: Amount)

  case class TransferRecords(records: Seq[TransferRecord])
}
package route.request

import domain._

case class TransferRequest(sourceAccount: String,
                           destinationAccount: String,
                           amount: BigDecimal) {
  def toDomain: domain.TransferRecord =
    TransferRecord(
      to = Account(destinationAccount),
      from = Account(sourceAccount),
      Amount(amount)
    )

}

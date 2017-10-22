package service.storage

import akka.Done
import akka.actor.Actor
import domain.{Account, TransferRecord, TransferRecords}

import scala.collection.mutable

class InMemoryStorage extends Actor {

  import InMemoryStorage._

  private val transfersTo = mutable.HashMap.empty[Account, Seq[TransferRecord]]
  private val transfersFrom = mutable.HashMap.empty[Account, Seq[TransferRecord]]

  private def saveTransfer(transferRecord: TransferRecord) = {
    val currentTransfersTo: Seq[TransferRecord] = transfersTo.getOrElse(transferRecord.to, Nil)
    transfersTo += ((transferRecord.to, currentTransfersTo :+ transferRecord))
    val currentTransfersFrom: Seq[TransferRecord] = transfersFrom.getOrElse(transferRecord.to, Nil)
    transfersFrom += ((transferRecord.from, currentTransfersFrom :+ transferRecord))
  }

  private def loadTransferTo(account: Account): Seq[TransferRecord] =
    transfersTo.getOrElse(account, Nil)

  private def loadTransferFrom(account: Account): Seq[TransferRecord] =
    transfersFrom.getOrElse(account, Nil)

  override def receive: PartialFunction[Any, Unit] = {
    case Save(record) =>
      saveTransfer(record)
      sender() ! Done
    case LoadTransfersTo(account) =>
      val transfers = loadTransferTo(account)
      sender() ! TransferRecords(transfers)
    case LoadTransfersFrom(account) =>
      val transfers = loadTransferFrom(account)
      sender() ! TransferRecords(transfers)
  }

}

object InMemoryStorage {

  sealed trait StorageCommand

  case class Save(transferRecord: TransferRecord) extends StorageCommand

  sealed trait StorageQuery

  case class LoadTransfersTo(to: Account) extends StorageQuery

  case class LoadTransfersFrom(from: Account) extends StorageQuery
}

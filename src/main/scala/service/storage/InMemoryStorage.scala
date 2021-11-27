package service.storage

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import domain.{Account, TransferRecord, TransferRecords}

import scala.collection.immutable.HashMap

object InMemoryStorage {
  sealed trait Command
  final case class Save(transferRecord: TransferRecord, replyTo: ActorRef[Done]) extends Command
  final case class LoadTransfersTo(to: Account, replyTo: ActorRef[TransferRecords]) extends Command
  final case class LoadTransfersFrom(from: Account, replyTo: ActorRef[TransferRecords]) extends Command

  def apply(): Behavior[Command] = registry(
    HashMap.empty[Account, Seq[TransferRecord]],
    HashMap.empty[Account, Seq[TransferRecord]])

  private def registry(transfersTo: HashMap[Account, Seq[TransferRecord]],
                       transfersFrom: HashMap[Account, Seq[TransferRecord]]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Save(transferRecord, replyTo) =>
        val currentTransfersTo: Seq[TransferRecord] = transfersTo.getOrElse(transferRecord.to, Nil)
        val updatedTo = transfersTo + ((transferRecord.to, currentTransfersTo :+ transferRecord))

        val currentTransfersFrom: Seq[TransferRecord] = transfersFrom.getOrElse(transferRecord.to, Nil)
        val updatedFrom = transfersFrom + ((transferRecord.from, currentTransfersFrom :+ transferRecord))

        replyTo ! Done
        registry(updatedTo, updatedFrom);
      case LoadTransfersTo(account, replyTo) =>
        val transfers = transfersTo.getOrElse(account, Nil)
        replyTo ! TransferRecords(transfers)
        Behaviors.same
      case LoadTransfersFrom(account, replyTo) =>
        val transfers = transfersFrom.getOrElse(account, Nil)
        replyTo ! TransferRecords(transfers)
        Behaviors.same
    }
  }
}

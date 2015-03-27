package vfd.uav

import org.mavlink.Assembler
import org.mavlink.Packet
import org.mavlink.Parser
import org.mavlink.messages.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.util.ByteString

/** Provides utilities for actors representing a mavlink connection. */
trait MavlinkUtil { myself: Actor with ActorLogging =>
  
  /** Mavlink system ID of this connection. */
  val systemId: Byte

  /** Mavlink component ID of this connection. */
  val componentId: Byte
  
  /** Assembler for creating packets originating from this connection. */
  private lazy val assembler = new Assembler(systemId, componentId)
  
  /** Assembles a message into a bytestring representing a packet sent from this connection. */
  protected def assemble(message: Message): ByteString = {
    val (messageId, payload) = Message.pack(message)
    val packet: Packet = assembler.assemble(messageId, payload)
    ByteString(packet.toArray)
  }

  /** Parser for messages being sent to the uav. */
  protected val outgoing: Parser = new Parser(packet => Message.unpack(packet.messageId, packet.payload) match {
    //TODO handle ping
    /*
    case Ping(`systemId`, `componentId`) =>
      val message = Ack(packet.systemId, packet.componentId)
      val data = assemble(message)
      self ! Connection.Received(data)*/
    case _ => ()
  })

  /** Parser for messages coming from the uav. */
  protected val incoming: Parser = new Parser(pckt =>
    log.debug("incoming message: " + Message.unpack(pckt.messageId, pckt.payload)))

}
package Impl

import akka.util.ByteString
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import java.net.InetSocketAddress
import java.io.FileOutputStream
import java.io.FileInputStream

/**
 * singleton object contains useful functions to support the functionalities of paxos_Actor
 * 
 * @author Zepeng Zhao
 */
object Util {
  
  /**
   * passing in an object, convert it to ByteString and then return it
   * this is useful when the paxos actor needs to send an object via Udp protocol
   * 
   */
  def toByteString(obj:Object):ByteString = {
    if(obj == null)
      return null
    var bout = new ByteArrayOutputStream()
    var out =  new ObjectOutputStream(bout)
    out.writeObject(obj)
    ByteString(bout.toByteArray())    
  }
  
  /**
   * passing in a byteString, convert it back to object and return it
   * this is useful when the paxos actor receives data via Udp protocol.
   * 
   */
  def toObject(bstr:ByteString):Object = {
    if(bstr == null) return null
    var in = new ByteArrayInputStream(bstr.toByteBuffer.array())
    var is = new ObjectInputStream(in)
    is.readObject()    
  }
  
  /*
   * load paxos properties(id, ip addr and port number) from file 'paxos.config' 
   * and store it to a map
   * the config file must store all paxoses properties in a format like below;
   * id1 host1 port_number1   eg.   1 127.0.0.1 2015
   * id2 host2 port_number2         2 127.0.0.1 2016
   *    ... ...
   *  Note that id must be a unique number
   */
  def loadPaxos():Map[Int,InetSocketAddress] ={
    var m:Map[Int,InetSocketAddress] = Map()
    val filename = "paxos.config"
    for (line <- Source.fromFile(filename).getLines()) {
      var arr = line.split(" ")
      m+=(arr(0).toInt->new InetSocketAddress(arr(1),arr(2).toInt))
    }   
    m   
  }
  
  def writeToDisk(filename:String, m:Map[BigInt,(java.io.Serializable,ProposalID)]){
    val fos = new FileOutputStream(filename)
    val oos = new ObjectOutputStream(fos)
    oos.writeObject(m)
    oos.close()
  }
  
  def read_from_disk(filename:String):Map[BigInt,(java.io.Serializable,ProposalID)]={
    val fis = new FileInputStream(filename)
    val ois = new ObjectInputStream(fis)
    val obj = ois.readObject()
    ois.close()
    obj.asInstanceOf[Map[BigInt,(java.io.Serializable,ProposalID)]]    
  }
   
  
}
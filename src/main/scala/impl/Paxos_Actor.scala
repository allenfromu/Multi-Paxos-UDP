package Impl

import akka.actor.Actor
import akka.io.Udp
import java.net.InetSocketAddress
import akka.io.IO
import akka.actor.ActorRef
import java.util.logging.Logger
import java.io.Serializable
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.io.Source
import java.io.FileWriter

case class receive_prepare(ins:BigInt, pid:ProposalID)
case class receive_promise(ins:BigInt,nid:Int,v_id:(Serializable,ProposalID), promised_pid:ProposalID)
case class receive_nack(ins:BigInt, highest_pid:ProposalID)
case class propose(value:Serializable,nid:Int)
case class accept(ins:BigInt,acc_value:Serializable, acc_pid:ProposalID )
case class accepted(ins:BigInt, uid:Int, value:Serializable, pid:ProposalID)
case class propose_cmd(v:Serializable)
case class receive_heartbeat(ins:BigInt, nid:Int)
case class leader_liveness()
case class instance_request(ins:BigInt)
case class instance_response(ins:BigInt, acc_v:Serializable, acc_pid:ProposalID)
case class Print_logs()


/**
 * A paxos actor can act as three types of agents: proposer, acceptor, learner.
 * As proposer, users can prompt the paxos to propose a value from the console
 * As acceptor, the paxos can also receive prepare message, and send out promise or nack message as response
 * As learner, the paxos is always listening from other paxos accepted value.
 * 
 * @author Zepeng Zhao
 * 
 */
class Paxos_Actor(val pm:Map[Int,InetSocketAddress], val id:Int) extends Actor{   
   private val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
   import context.system
   private var proposal_values:List[Serializable] = List()   
   private var proposing_value:Serializable = null      
   private var proposing_id:ProposalID = null      
   private var promises:Map[String,List[(Serializable,ProposalID)]]=Map()           //((instance_number,proposal_id)=>List[(value,proposal_id)])
   private var learned_proposals:Map[String,(Serializable,ProposalID, Int)] = Map()
   private val quorum_size = pm.size/2 + 1
   private var promise_ids:Map[BigInt,ProposalID] = Map()
   private var next_instance = BigInt(0)
   private var logs:Map[BigInt,(Serializable,ProposalID)] = Map()                    //(instance_number => (value, pid))
   private var leader:Int = this.id
   private var leading_instance:BigInt = BigInt(0)
   private var leader_live = true
   private var socket:ActorRef = null
   private var lock1 = new Object()
   private val filename = "/tmp/Node_"+this.id+".txt"
   try{
     logger.info("Trying to read logs from disk.")
     this.logs = Util.read_from_disk(filename)
     //obtain the most recent instance number
     this.logs.keys.foreach { x => if(this.next_instance <= x) this.next_instance = x+1 }
   }
   catch{
     case e:Exception=>{println("\nRead file failed:"+e.getMessage+"\n->")}
   }
   
   this.leading_instance = this.next_instance
   
   logger.info("Next instance:"+this.next_instance)
   implicit val executor = context.system.dispatcher
   context.system.scheduler.schedule(Duration(1000,TimeUnit.MILLISECONDS),
       Duration(500, TimeUnit.MILLISECONDS),
       new Runnable{def run(){ send_heartbeat() }})
       
   context.system.scheduler.schedule(Duration(1, TimeUnit.SECONDS),
       Duration(new scala.util.Random(System.currentTimeMillis).nextInt(1500)+1500, TimeUnit.MILLISECONDS),
       new Runnable{def run(){ check_and_update_proposal_array() }})
       
   context.system.scheduler.schedule(Duration(4000,TimeUnit.MILLISECONDS),
       Duration(1500, TimeUnit.MILLISECONDS),
       new Runnable{def run(){ send_leader_liveness()}})
       
   context.system.scheduler.schedule(Duration(5000,TimeUnit.MILLISECONDS),
       Duration(3000, TimeUnit.MILLISECONDS),
       new Runnable{def run(){ check_leader_liveness }})
       
   context.system.scheduler.schedule(Duration(3000,TimeUnit.MILLISECONDS),
       Duration(500, TimeUnit.MILLISECONDS),
       new Runnable{def run(){ check_and_update_instance() }})
   
   IO(Udp) ! Udp.Bind(self, pm(this.id))
 
  def receive = {
    case Udp.Bound(local) =>  {
      socket = sender
      context.become(ready())
    }
  }
  
  def ready(): Receive = {
    case Udp.Received(data, remote) =>{
      
      Util.toObject(data) match{     
        
        case receive_prepare(ins, promised_pid) =>{
          logger.info("receive prepare{instance_number:"+ins.toString()+
              ", proposal_id:"+promised_pid.toString()+"} from remote:["+remote.toString()+"]")          
            logger.info("Receive prepare from:"+remote.toString())              
            var pid:ProposalID = if(this.promise_ids.contains(ins)) this.promise_ids(ins) else null
            if(pid == null || !pid.isGreater(promised_pid)){
              this.promise_ids +=(ins->promised_pid)
              logger.info("Send back promise to remote:["+remote.toString()+"]")
              var acc_v_id:(Serializable,ProposalID) = if(this.logs.contains(ins)) this.logs(ins) else null
              socket ! Udp.Send(Util.toByteString(receive_promise(ins,this.id,acc_v_id,promised_pid)),remote)
            }else{
              logger.info("Send back promise to remote:["+remote.toString()+"]")
              socket ! Udp.Send(Util.toByteString(receive_nack(ins,pid)),remote)              
            }     
        }
        
        case receive_promise(ins,nid, acc_v_id, promised_pid) => {
          if(this.next_instance == ins){
             var key = promised_pid.toString()
             var l = List(acc_v_id)
             if(this.promises.contains(key))
               l = l++this.promises(key)
             this.promises+=(key->l)
             if(l.size >= this.quorum_size){
               var mid:ProposalID = null
               l.foreach(f =>{
                 if(f !=null && (mid == null || f._2.isGreater(mid))){
                    if(mid == null)
                      this.proposal_values=this.proposing_value::this.proposal_values
                    mid = f._2
                    this.proposing_value = f._1
                 }
                }
                )           
                this.promises = Map()
                for((k,v)<-pm){
                  socket ! Udp.Send(Util.toByteString(accept(this.next_instance,this.proposing_value,this.proposing_id)),v)
                }            
             }            
          }
        }
        
        case receive_nack(ins,higher_pid) => {
          
          logger.info("Receive nack{instance:"+ins+",higher_pid:"+higher_pid.toString()+"} from:"+remote.toString())     
          if(this.leader == this.id){        
            if(ins == this.next_instance && this.proposing_id != null && higher_pid.isGreater(this.proposing_id)){
              this.proposing_id = new ProposalID(higher_pid.getNumber+1,this.id)
              for((k,v)<-pm){
                socket ! Udp.Send(Util.toByteString(receive_prepare(this.next_instance,this.proposing_id)), v)
                logger.info("Send prepare{instance:["+this.next_instance+"], proposal_id:"+this.proposing_id.toString()+ "} to node:"+k)
              }
            } 
          }else{
           self ! propose(this.proposing_value, this.leader)
          }
                    
        }
        
        case accept(ins, acc_v, acc_pid) =>{

          var pid:ProposalID = if(this.promise_ids.contains(ins)) this.promise_ids(ins) else null
          logger.info("Receive accept from:"+remote.toString()+" value:["+acc_v+"],proposal id:["+acc_pid.toString()+"]")
          if(pid == null || !pid.isGreater(acc_pid)){ 
              this.promise_ids+=(ins->acc_pid)
              this.logs+=(ins->(acc_v,acc_pid))
              socket ! Udp.Send(Util.toByteString(accepted(ins,this.id,acc_v,acc_pid)),remote)
              Util.writeToDisk(filename, logs)
           }else{
              logger.info("send back nack to remote:["+remote.toString()+"]")
              socket ! Udp.Send(Util.toByteString(receive_nack(ins, pid)),remote)
           }

        }
        
        case accepted(ins, nid, acc_v, acc_pid) =>{

          logger.info("learning a value:["+acc_v+"] from node "+nid)
          var key = acc_pid.toString()+"_"+ins           
          //println("accept key:"+key)
          if(ins >= this.next_instance){
            if(!this.learned_proposals.contains(key)){
               this.learned_proposals+=(key->(acc_v,acc_pid,1))
            }else{
              var temp1 = this.learned_proposals(key)
              var temp2 = temp1._3+1
              this.learned_proposals+=(key->(acc_v,acc_pid,temp2))
              if(temp2 >= this.quorum_size){
                this.proposing_value = null
                this.proposing_id = null
                this.next_instance+=1
                this.learned_proposals = Map()
                print("\nLearned value:"+acc_v+",instance:"+this.next_instance+"\n->")
             }
            }
          }   
          
        }      
       
       
       case propose_cmd(va) =>{
        logger.info("receive propose command:propose{value:"+va+"}")
        if(this.proposing_value == null){
          this.proposing_value = va
          this.proposing_id =  new ProposalID(0, this.id)
          this.lock1.synchronized{
            for((k,v)<-pm){
              socket ! Udp.Send(Util.toByteString(receive_prepare(this.next_instance,this.proposing_id)), v)
              logger.info("Send prepare{instance:["+this.next_instance+"], proposal_id:"+this.proposing_id.toString()+ "} to node:"+k)
            }                  
          }
        }else{
          this.proposal_values=this.proposal_values:+va
        }
      }
       
       case receive_heartbeat(ins,nid)=>{
         if(ins > this.leading_instance || (ins == this.leading_instance && this.leader < nid)){
           this.leader = nid
           this.leading_instance = ins
           this.leader_live = true
           logger.info("Leader id:"+nid+", instance:"+ins)
         }
       }   
       
       case leader_liveness()=>{
          this.leader_live = true
       }
       
       case instance_request(ins)=>{
         var temp = if(this.logs.contains(ins)) this.logs(ins) else null
         if(temp != null)
           socket ! Udp.Send(Util.toByteString(instance_response(ins,temp._1,temp._2)),remote)
       }
       
       case instance_response(ins, acc_v, acc_pid)=>{
         if(this.proposing_value == null && ins == this.next_instance){
           this.logs+=(ins->(acc_v,acc_pid))
           this.next_instance+=1
         }
           
       }
       
      }
    }
    
    
    case propose(v:Serializable, nid:Int) => {
      if(nid <= pm.size){
        socket ! Udp.Send(Util.toByteString(propose_cmd(v)), pm(this.leader))
        logger.info("ask node:["+nid+"] to popose {value:"+v+"}")
      }
    }
    
    case Print_logs() =>{
      println()
      for(i <-1 until this.leading_instance.toInt){
        if(logs.contains(i))
          println("instance:"+i+", value:"+this.logs(i))
      }
      println("->")
    }
          
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)   
    
  }
  
  def send_heartbeat(){
    if(socket != null){      
      for((k,v)<-pm){
        socket ! Udp.Send(Util.toByteString(receive_heartbeat(this.next_instance,this.id)),v)        
      }
    }      
  }
  
  def send_leader_liveness(){
    if(this.leading_instance == this.next_instance && this.id == this.leader){
      for((k,v)<-pm){
        socket ! Udp.Send(Util.toByteString(leader_liveness()),v)
      }
    }     
  }
  
  def check_leader_liveness(){
    if(!this.leader_live){
      logger.info("leader:"+this.leader+" was not alive, updating leadership")
      this.leading_instance = this.next_instance
      this.leader = this.id
    }
    this.leader_live = false
  }
  
  def check_and_update_instance(){
    
    if(this.next_instance < this.leading_instance){
      socket ! Udp.Send(Util.toByteString(instance_request(this.next_instance)),pm(this.leader))
    }
      
  }
  
  def check_and_update_proposal_array(){
 
    if(this.proposing_value == null && this.proposal_values.size != 0){

      var v = this.proposal_values(0)
      println("sending "+v)
      //socket ! Udp.Send(Util.toByteString(propose_cmd(v)),pm(this.id))
      this.self ! propose(v, this.leader)
      this.proposal_values = this.proposal_values.drop(1)
    }   
    
  }
  
}
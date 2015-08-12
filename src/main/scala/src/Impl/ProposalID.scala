package Impl

/*
 * proposalID instance to represent a unique propose among all paxoses
 * 
 * @author Zepeng Zhao
 */
class ProposalID(val num:Int,val id:Int) extends Serializable{
  private var number = num
  private var pid = id
  
  def reset(num1:Int, id1:Int) {
    this.number = num1
    this.pid = id1
  }
  
  def getNumber:Int = number
  
  def getPid:Int = pid
  
  def isGreater(other:ProposalID):Boolean = this.number > other.number || (this.number == other.number && this.pid > other.pid)
  
  def isSmaller(other:ProposalID):Boolean = this.number < other.number || (this.number == other.number && this.pid < other.pid)

  def isEqual(other:ProposalID):Boolean = this.number == other.number && this.pid == other.pid
  
  override def toString:String = "[number:"+this.number.toString()+", pid:"+this.pid.toString()+"]"
  
}
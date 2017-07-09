package sri.tools

package object graphqltosjs {
  implicit class String_Ext_Method(val in: String) extends AnyVal {
    def removeComments =
      if (in != null)
        in.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "")
      else in
  }

  def getScalaName(in: String): String = in match {
    case "type" => "`type`"
    case "var" => "`var`"
    case "object" => "`object`"
    case "then" => "`then`"
    case _ => in
  }

  case class ScalaField(name: String,
                        tpe: String,
                        isRequired: Boolean = false,
                        customTypeDef: String = "") {
    override def hashCode(): Int = name.hashCode

    override def equals(obj: scala.Any): Boolean = obj match {
      case o: ScalaField => o.name == name
      case _ => false
    }
  }
}

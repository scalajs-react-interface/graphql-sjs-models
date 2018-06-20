package sri.tools.graphqltosjs

import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.global

import scala.scalajs.js


object Generator  {

  val gqlTypes = Map("String" -> "String",
                     "Float" -> "Double",
                     "Int" -> "Int",
                     "Boolean" -> "Boolean",
                     "DateTime" -> "String",
                     "ID" -> "String")

  def getScalaType(in: GraphQLDefinitionFieldType,
                   isRequired: Boolean = false): String = {

    if (in.kind == GraphQLDefinitionFieldTypeKind.LIST_TYPE) {
      if (isRequired) s"js.Array[${getScalaType(in.`type`, false)}]"
      else s"js.UndefOr[js.Array[${getScalaType(in.`type`, false)}]]"
    } else if (in.kind == GraphQLDefinitionFieldTypeKind.NON_NULL_TYPE) {
      getScalaType(in.`type`, true)
    } else if (in.kind == GraphQLDefinitionFieldTypeKind.NAMED_TYPE) {
      val t = in.name.value
      if (isRequired) gqlTypes.getOrElse(t, t)
      else s"js.UndefOr[${gqlTypes.getOrElse(t, t)}]"
    } else getScalaType(in.`type`, false)
  }

  def convertGraphFieldToScalaField(in: GraphQLDefinitionField) = {
    //    global.console.log("converting type", in.name.value)
    import io.scalajs.JSON
    val name = getScalaName(in.name.value)
//    println(s"type dude : ${JSON.stringify(in.`type`)}")
    val tpe = getScalaType(in.`type`)
    ScalaField(name, tpe)
  }

  def convertObjectDefinitionToScala(in: GraphQLDefinition) = {
//    global.console.log("converting type", in.name.value)
    val traitName = in.name.value
    val interfaces = in.interfaces.map(gi => gi.name.value).toList
    val ext =
      if (interfaces.length == 0) "js.Object"
      else if (interfaces.length == 1) interfaces.head
      else s"${interfaces.head} with ${interfaces.tail.mkString("with")}"
    val fields = in.fields.map(convertGraphFieldToScalaField)
    s"""
       |
       |@js.native
       |trait $traitName extends $ext  {
       |  ${fields
         .map(sf => {
           if (sf.name == "id" && traitName != "Viewer") ""
           else s"val ${sf.name} :${sf.tpe} = js.native"
         }) //Bloody  hack to get rid of id
         .mkString("\n")}
       |}
     """.stripMargin
  }

  def convertInterfaceDefinitionToScala(in: GraphQLDefinition) = {
//    global.console.log("converting type", in.name.value)
    val traitName = in.name.value
    val fields = in.fields.map(convertGraphFieldToScalaField)
    s"""
       |
       |@js.native
       |trait $traitName extends js.Object  {
       |  ${fields
         .map(sf => s"val ${sf.name} :${sf.tpe} = js.native")
         .mkString("\n")}
       |}
     """.stripMargin
  }

  def convertInputObjectTypeDefinitionToScala(in: GraphQLDefinition) = {
//    global.console.log("converting type", in.name.value)
    val traitName = in.name.value
    val fields = in.fields.map(convertGraphFieldToScalaField)
    s"""
       |
       |
       |trait $traitName extends js.Object  {
       |  ${fields
         .map(sf =>
           s"   val ${sf.name} :${sf.tpe} ${if (sf.tpe.contains("UndefOr[")) s" = js.undefined"
           else ""}")
         .mkString("\n")}
       |}
       |
       |object $traitName {
       |
       | def apply(${fields.map(sf => s"${sf.name} :${sf.tpe.replace("js.UndefOr[","OptionalParam[")} ${if (sf.tpe.contains("UndefOr[")) s" = OptDefault" else ""}").mkString(",\n")}):$traitName = {
       |
       |   val p = FunctionObjectMacro()
       |   p.asInstanceOf[$traitName]
       | }
       |
       |}
       |
     """.stripMargin
  }

  def convertEnumTypeDefinitionToScala(in: GraphQLDefinition) = {
//    global.console.log("converting type", in.name.value)
    val traitName = in.name.value
    val values = in.values.map(_.name.value)
    s"""
       |
       |@js.native
       |trait $traitName extends js.Object
       |
       |object $traitName {
       |  ${values
         .map(v => s"""@inline def $v = "$v".asInstanceOf[$traitName] """)
         .mkString("\n")}
       |}
       |
     """.stripMargin
  }

   def getArg(args:Seq[String], name:String, shortName:String=""):js.UndefOr[String] = {
    val index = args.zipWithIndex.find{ case (f,i) => if(shortName.nonEmpty) (f == name || f == shortName) else (f == name) }.map(_._2).getOrElse(-1)
     if(index == -1 || args.length == index + 1) js.undefined else args(index+1)
   }

   def main(args:Array[String]): Unit = {
    val commandArgs = global.process.argv.toList

    val SCHEMA_PATH = getArg(commandArgs,"--schema","--s").getOrElse("./data/schema.graphql")

    val OUTPUT_PATH = getArg(commandArgs,"--output","--o").getOrElse("")

    val RELAY_URL = getArg(commandArgs,"--relayUrl","--u").getOrElse("")

    val RELAY_WS_URL = getArg(commandArgs,"--relayWsUrl","--w").getOrElse("")

    val in = Fs.readFileSync(SCHEMA_PATH, "utf8").toString()
    val ast = GraphQL.parse(in)
    val tpes = ast.definitions
      .map(d => {
        d.kind match {
          case GraphQLDefinitionKind.ObjectTypeDefinition =>
            convertObjectDefinitionToScala(d)
          case GraphQLDefinitionKind.EnumTypeDefinition =>
            convertEnumTypeDefinitionToScala(d)
          case GraphQLDefinitionKind.InputObjectTypeDefinition =>
            convertInputObjectTypeDefinitionToScala(d)
          case GraphQLDefinitionKind.InterfaceTypeDefinition =>
            convertInterfaceDefinitionToScala(d)
          case _ => ""
        }
      })
      .mkString("\n")

    val out =
      s"""
         |package ${OUTPUT_PATH.replace(".","").replace("src/main/scala/","").split("/").mkString(".")}
         |
         |import scala.scalajs.js
         |import scala.scalajs.js.annotation.{JSImport}
         |
         |import scalajsplus.{
         |  OptDefault,
         |  OptionalParam
         |}
         |import scalajsplus.macros.FunctionObjectMacro
         |
         |
         | /** this file is automatically generated on ${new js.Date()
           .toISOString()}
         |
         |  don't modify this file directly */
         |object Source {
         |  val RELAY_URL: String = "$RELAY_URL"
         |  val RELAY_WS_URL: String = "$RELAY_WS_URL"
         |}
         |
         |object Scalars {
         |  type Json = js.Array[js.Object]
         |}
         |import Scalars._
         |
         |trait SubscriptionFilterNode extends js.Object {
         |}
         |
         | $tpes
         |
       """.stripMargin

    Fs.writeFileSync(s"$OUTPUT_PATH/GraphQLModels.scala", out)

    global.console.log(s"Successfully generated models!.")

  }
}

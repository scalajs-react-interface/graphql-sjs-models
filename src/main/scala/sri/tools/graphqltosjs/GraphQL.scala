package sri.tools.graphqltosjs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
trait GraphQlParsed extends js.Object {
  val kind: String = js.native
  val definitions: js.Array[GraphQLDefinition] = js.native
}

@js.native
trait GraphQLDefinition extends js.Object {
  val kind: GraphQLDefinitionKind = js.native
  val name: GraphQLDefinitionName = js.native
  val fields: js.Array[GraphQLDefinitionField] = js.native
  val interfaces: js.Array[GraphQLDefinitionInterface] = js.native
  val values: js.Array[GraphQLDefinitionField] = js.native

}

@js.native
trait GraphQLDefinitionInterface extends js.Object {
  val kind: String = js.native
  val name: GraphQLDefinitionName = js.native
}

@js.native
trait GraphQLDefinitionName extends js.Object {
  val kind: String = js.native
  val value: String = js.native
}

@js.native
trait GraphQLDefinitionField extends js.Object {
  val kind: String = js.native
  val name: GraphQLDefinitionName = js.native
  val `type`: GraphQLDefinitionFieldType = js.native
}

@js.native
trait GraphQLDefinitionFieldType extends js.Object {
  val kind: GraphQLDefinitionFieldTypeKind = js.native
  val `type`: GraphQLDefinitionFieldType = js.native
  val name: GraphQLDefinitionName = js.native
}

@js.native
trait GraphQLDefinitionFieldTypeKind extends js.Object

object GraphQLDefinitionFieldTypeKind {
  val NAMED_TYPE = "NamedType".asInstanceOf[GraphQLDefinitionFieldTypeKind]
  val LIST_TYPE = "ListType".asInstanceOf[GraphQLDefinitionFieldTypeKind]
  val NON_NULL_TYPE =
    "NonNullType".asInstanceOf[GraphQLDefinitionFieldTypeKind]
}

@js.native
@JSImport("graphql", JSImport.Namespace)
object GraphQL extends js.Object {
  def parse(input: String): GraphQlParsed = js.native
}

@js.native
trait GraphQLDefinitionKind extends js.Object

object GraphQLDefinitionKind {
  val InputObjectTypeDefinition =
    "InputObjectTypeDefinition".asInstanceOf[GraphQLDefinitionKind]
  val ObjectTypeDefinition =
    "ObjectTypeDefinition".asInstanceOf[GraphQLDefinitionKind]
  val EnumTypeDefinition =
    "EnumTypeDefinition".asInstanceOf[GraphQLDefinitionKind]
  val InterfaceTypeDefinition =
    "InterfaceTypeDefinition".asInstanceOf[GraphQLDefinitionKind]
  val ScalarTypeDefinition =
    "ScalarTypeDefinition".asInstanceOf[GraphQLDefinitionKind]
  val TypeExtensionDefinition =
    "TypeExtensionDefinition".asInstanceOf[GraphQLDefinitionKind]
}

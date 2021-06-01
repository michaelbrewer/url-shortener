import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awscdk.core.ConstructNode
import software.amazon.awscdk.core.Stack


/**
 * Get the stack template as a JsonNode
 *
 * @param stack The stack convert to json
 */
fun getStackTemplateJson(stack: Stack): JsonNode {
    val root = stack.node.root
    val stackArtifact = ConstructNode.synth(root.node).getStackByName(stack.stackName)
    return ObjectMapper().valueToTree(stackArtifact.template)
}

/**
 * Count resources that matches a type
 *
 * @param jsonNode The root jsonNode of a stack
 * @param type The type of resource we want to count
 */
fun countResources(jsonNode: JsonNode, type: String) = jsonNode["Resources"].count { node -> isType(node, type) }

/**
 * Count resources that matches a type and contains all of the map properties
 *
 * @param jsonNode The root jsonNode of a stack
 * @param type The type of resource we want to count
 * @param map A map of strings used to match against a Resource Properties
 */
fun countResourcesLike(jsonNode: JsonNode, type: String, map: Map<String, String>): Int {
    return jsonNode["Resources"].count { node -> isType(node, type) && propertiesMatchAll(node, map) }
}

/**
 * First resource that matches a type and contains all of the map properties
 *
 * @param jsonNode The root jsonNode of a stack
 * @param type The type of resource
 * @param map A map of strings used to match against a Resource Properties
 */
fun firstResourcesLike(jsonNode: JsonNode, type: String, map: Map<String, String>): JsonNode {
    return jsonNode["Resources"].first { node -> isType(node, type) && propertiesMatchAll(node, map) }
}

/**
 * Count outputs that contains all of the map properties
 *
 * @param jsonNode The root jsonNode of a stack
 * @param map A map of strings used to match against
 */
fun countOutputsLike(jsonNode: JsonNode, map: Map<String, Any>): Int {
    return jsonNode["Outputs"].count { node -> matchAll(node, map) }
}

private fun isType(node: JsonNode, type: String) = node["Type"] != null && node["Type"].asText() == type

private fun propertiesMatchAll(node: JsonNode, map: Map<String, String>) =
    node["Properties"] != null &&
            map.all { item ->
                node["Properties"][item.key] != null && node["Properties"][item.key].asText() == item.value
            }

private fun matchAll(node: JsonNode, map: Map<String, Any>) = map.all { item -> matchAllItems(node, item) }

private fun matchAllItems(node: JsonNode, item: Map.Entry<String, Any>): Boolean {
    return when {
        node[item.key] == null -> false
        item.value is String -> {
            node[item.key].asText() == item.value
        }
        item.value is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            matchAll(node[item.key], item.value as Map<String, Any>)
        }
        else -> false
    }
}

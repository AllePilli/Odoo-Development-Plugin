package com.github.allepilli.odoodevelopmentplugin.core

import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.openapi.project.Project
import java.util.LinkedList
import java.util.Queue

class ModuleGraph {
    companion object {
        fun createGraph(project: Project, root: String): ModuleGraph = ModuleGraph().also {
            addToGraph(project, root, it)
        }

        private fun addToGraph(project: Project, node: String, graph: ModuleGraph) {
            val dependencies = ModuleDependencyIndexUtil.findDirectDependencies(project, node)

            graph.addEdges(node, dependencies)
            dependencies.forEach {
                addToGraph(project, it, graph)
            }
        }
    }

    private val adjacencyList: MutableMap<String, MutableSet<String>> = mutableMapOf()
    lateinit var root: String
        private set

    val nodes: Set<String>
        get() = adjacencyList.keys

    fun getDependencies(node: String): Set<String> = adjacencyList.getOrDefault(node, mutableSetOf())

    fun addEdges(from: String, to: Collection<String>) {
        if (!this::root.isInitialized) root = from

        adjacencyList.getOrPut(from) { mutableSetOf() }.addAll(to)
    }

    fun addEdge(edge: Pair<String, String>) = addEdge(edge.first, edge.second)
    fun addEdge(from: String, to: String) {
        if (!this::root.isInitialized) root = from

        adjacencyList.getOrPut(from) { mutableSetOf() }.add(to)
    }

    fun bfs(start: String = root): Sequence<String> = sequence {
        val visited = mutableSetOf<String>()
        val queue: Queue<String> = LinkedList()

        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            yield(node)

            adjacencyList[node]?.forEach { neighbor ->
                if (neighbor !in visited) {
                    queue.add(neighbor)
                    visited.add(neighbor)
                }
            }
        }
    }
}
package com.github.allepilli.odoodevelopmentplugin.search_everywhere.models

import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter


class ModelChooseByNameContributor : ChooseByNameContributorEx {
    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        OdooModelNameIndexUtil.processAllNames(processor, scope, filter)
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        OdooModelNameIndexUtil.processItems(
                name,
                { file, items ->
                    !items.all { item ->
                        processor.process(ModelNavigationItem(name, item.modelNameOffset, file, parameters))
                    }
                },
                parameters.searchScope,
                filter = parameters.idFilter,
        )
    }
}
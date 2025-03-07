// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.stubindex.resolve

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor
import com.intellij.util.Processors
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.kotlin.idea.base.indices.KotlinPackageIndexUtils
import org.jetbrains.kotlin.idea.stubindex.*
import org.jetbrains.kotlin.idea.vfilefinder.KotlinPackageSourcesMemberNamesIndex
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.safeNameForLazyResolve
import org.jetbrains.kotlin.resolve.lazy.data.KtClassInfoUtil
import org.jetbrains.kotlin.resolve.lazy.data.KtClassOrObjectInfo
import org.jetbrains.kotlin.resolve.lazy.data.KtScriptInfo
import org.jetbrains.kotlin.resolve.lazy.declarations.PackageMemberDeclarationProvider
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter

class StubBasedPackageMemberDeclarationProvider(
    private val fqName: FqName,
    private val project: Project,
    private val searchScope: GlobalSearchScope
) : PackageMemberDeclarationProvider {

    override fun getDeclarations(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean): List<KtDeclaration> {
        val fqNameAsString = fqName.asString()
        val result = ArrayList<KtDeclaration>()

        fun addFromIndex(index: KotlinStringStubIndexExtension<out KtNamedDeclaration>) {
            index.processElements(fqNameAsString, project, searchScope) {
                if (nameFilter(it.nameAsSafeName)) {
                    result.add(it)
                }
                true
            }
        }

        if (kindFilter.acceptsKinds(DescriptorKindFilter.CLASSIFIERS_MASK)) {
            addFromIndex(KotlinTopLevelClassByPackageIndex)
            addFromIndex(KotlinTopLevelTypeAliasByPackageIndex)
        }

        if (kindFilter.acceptsKinds(DescriptorKindFilter.FUNCTIONS_MASK)) {
            addFromIndex(KotlinTopLevelFunctionByPackageIndex)
        }

        if (kindFilter.acceptsKinds(DescriptorKindFilter.VARIABLES_MASK)) {
            addFromIndex(KotlinTopLevelPropertyByPackageIndex)
        }

        return result
    }

    private val declarationNames_: Set<Name> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FileBasedIndex.getInstance()
            .getValues(KotlinPackageSourcesMemberNamesIndex.KEY, fqName.asString(), searchScope)
            .flatMapTo(hashSetOf()) {
                it.map { stringName -> Name.identifier(stringName).safeNameForLazyResolve() }
            }
    }

    override fun getDeclarationNames() = declarationNames_

    override fun getClassOrObjectDeclarations(name: Name): Collection<KtClassOrObjectInfo<*>> = runReadAction {
        KotlinFullClassNameIndex.get(childName(name), project, searchScope)
            .map { KtClassInfoUtil.createClassOrObjectInfo(it) }
    }

    @ApiStatus.Internal
    fun checkClassOrObjectDeclarations(name: Name) {
        val childName = childName(name)
        if (KotlinFullClassNameIndex.get(childName, project, searchScope).isEmpty()) {
            val processor = object : CommonProcessors.FindFirstProcessor<String>() {
                override fun accept(t: String?): Boolean = childName == t
            }
            KotlinFullClassNameIndex.processAllKeys(searchScope, null, processor)
            if (processor.isFound) {
                throw IllegalStateException("KotlinFullClassNameIndex has '$childName' but has no value for it in $searchScope")
            }
        }
    }

    override fun getScriptDeclarations(name: Name): Collection<KtScriptInfo> = runReadAction {
        KotlinScriptFqnIndex.get(childName(name), project, searchScope)
            .map(::KtScriptInfo)
    }


    override fun getFunctionDeclarations(name: Name): Collection<KtNamedFunction> {
        return runReadAction {
            KotlinTopLevelFunctionFqnNameIndex.get(childName(name), project, searchScope)
        }
    }

    override fun getPropertyDeclarations(name: Name): Collection<KtProperty> {
        return runReadAction {
            KotlinTopLevelPropertyFqnNameIndex.get(childName(name), project, searchScope)
        }
    }

    override fun getDestructuringDeclarationsEntries(name: Name): Collection<KtDestructuringDeclarationEntry> {
        return emptyList()
    }

    override fun getAllDeclaredSubPackages(nameFilter: (Name) -> Boolean): Collection<FqName> {
        return KotlinPackageIndexUtils.getSubPackageFqNames(fqName, searchScope, nameFilter)
    }

    override fun getPackageFiles(): Collection<KtFile> {
        return KotlinPackageIndexUtils.findFilesWithExactPackage(fqName, searchScope, project)
    }

    override fun containsFile(file: KtFile): Boolean {
        return searchScope.contains(file.virtualFile ?: return false)
    }

    override fun getTypeAliasDeclarations(name: Name): Collection<KtTypeAlias> {
        return KotlinTopLevelTypeAliasFqNameIndex.get(childName(name), project, searchScope)
    }

    private fun childName(name: Name): String {
        return fqName.child(name.safeNameForLazyResolve()).asString()
    }
}

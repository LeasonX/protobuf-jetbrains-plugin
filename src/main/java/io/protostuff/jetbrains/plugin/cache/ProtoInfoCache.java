package io.protostuff.jetbrains.plugin.cache;

import com.intellij.openapi.project.Project;
import io.protostuff.jetbrains.plugin.bean.ImportableNode;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoInfoCache {

    private final static Map<Project, Set<String>> CACHE_FILE_ABSTRACT_PATHS_MAP = new ConcurrentHashMap<>();

    //use memory to reduce time
    private final static Map<Project, Set<String>> CACHE_FILE_RELATIVE_PATHS_MAP = new ConcurrentHashMap<>();

    //key -> project; value -> {key -> file path; value -> importable node}
    private final static Map<Project, Map<String, Set<ImportableNode>>> CACHE_IMPORTABLE_NODE_MAP = new ConcurrentHashMap<>();

    public static Map<String, Set<ImportableNode>> getImportableNodeMap(Project project) {
        return CACHE_IMPORTABLE_NODE_MAP.get(project);
    }

    public static void putProjectImportableNodeMap(Project project,Map<String, Set<ImportableNode>> importableNodeMap){
        CACHE_IMPORTABLE_NODE_MAP.put(project, importableNodeMap);
    }

    public static void putProjectCacheFileAbstractPaths(Project project, Set<String> projectCacheFileAbstractPaths) {
        CACHE_FILE_ABSTRACT_PATHS_MAP.put(project, projectCacheFileAbstractPaths);
    }

    public static void putProjectCacheFileRelativePaths(Project project, Set<String> projectCacheFileRelativePaths) {
        CACHE_FILE_RELATIVE_PATHS_MAP.put(project, projectCacheFileRelativePaths);
    }

    public static Set<String> getProjectCacheFileAbstractPaths(Project project) {
        return CACHE_FILE_ABSTRACT_PATHS_MAP.get(project);
    }

    public static Set<String> getProjectCacheFileRelativePaths(Project project) {
        return CACHE_FILE_RELATIVE_PATHS_MAP.get(project);
    }
}

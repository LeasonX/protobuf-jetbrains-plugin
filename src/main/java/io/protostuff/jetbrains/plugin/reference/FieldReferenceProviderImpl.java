package io.protostuff.jetbrains.plugin.reference;

import static io.protostuff.compiler.model.ProtobufConstants.MSG_ENUM_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_ENUM_VALUE_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_FIELD_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_FILE_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_MESSAGE_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_METHOD_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_ONEOF_OPTIONS;
import static io.protostuff.compiler.model.ProtobufConstants.MSG_SERVICE_OPTIONS;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import io.protostuff.jetbrains.plugin.psi.AbstractFieldReferenceNode;
import io.protostuff.jetbrains.plugin.psi.DataTypeContainer;
import io.protostuff.jetbrains.plugin.psi.EnumConstantNode;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.ExtendNode;
import io.protostuff.jetbrains.plugin.psi.FieldNode;
import io.protostuff.jetbrains.plugin.psi.FieldReferenceNode;
import io.protostuff.jetbrains.plugin.psi.GroupNode;
import io.protostuff.jetbrains.plugin.psi.MapNode;
import io.protostuff.jetbrains.plugin.psi.MessageField;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.psi.OneOfNode;
import io.protostuff.jetbrains.plugin.psi.ProtoPsiFileRoot;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.psi.RpcMethodNode;
import io.protostuff.jetbrains.plugin.psi.ServiceNode;
import io.protostuff.jetbrains.plugin.psi.TypeReferenceNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Field reference provider.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class FieldReferenceProviderImpl implements FieldReferenceProvider {

    // "default" field option (a special case)
    public static final String DEFAULT = "default";
    // // Like default value, this "json_name" is not an actual option.
    // https://github.com/protocolbuffers/protobuf/blob/ba8692fbade4ba329cc4531e286ab5a8e0821d97/src/google/protobuf/compiler/parser.cc#L1087
    public static final String JSON_NAME = "json_name";
    private static final Logger LOGGER = Logger.getInstance(FieldReferenceProviderImpl.class);
    private static final Map<Class<? extends PsiElement>, String> TARGET_MAPPING
            = ImmutableMap.<Class<? extends PsiElement>, String>builder()
            .put(FieldNode.class, MSG_FIELD_OPTIONS)
            .put(MapNode.class, MSG_FIELD_OPTIONS)
            .put(MessageNode.class, MSG_MESSAGE_OPTIONS)
            .put(EnumConstantNode.class, MSG_ENUM_VALUE_OPTIONS)
            .put(EnumNode.class, MSG_ENUM_OPTIONS)
            .put(RpcMethodNode.class, MSG_METHOD_OPTIONS)
            .put(ServiceNode.class, MSG_SERVICE_OPTIONS)
            .put(ProtoRootNode.class, MSG_FILE_OPTIONS)
            .put(OneOfNode.class, MSG_ONEOF_OPTIONS)
            .build();
    private final Project project;
    private PsiFile inm = null;

    public FieldReferenceProviderImpl(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(FieldReferenceNode fieldReference) {
        String text = fieldReference.getText();
        if (Strings.isNullOrEmpty(text)) {
            return new PsiReference[0];
        }
        String targetType = getTarget(fieldReference);
        MessageNode message = resolveType(fieldReference, targetType);
        if (message == null) {
            LOGGER.warn("Could not resolve " + targetType);
            return new PsiReference[0];
        }
        List<AbstractFieldReferenceNode> components = new ArrayList<>();
        for (PsiElement element : fieldReference.getChildren()) {
            if (element instanceof AbstractFieldReferenceNode) {
                components.add((AbstractFieldReferenceNode) element);
            }
        }
        List<PsiReference> result = new ArrayList<>();
        for (AbstractFieldReferenceNode fieldRef : components) {
            String key = fieldRef.getText();
            MessageField target = null;
            if (message != null) {
                if (fieldRef.isExtension()) {
                    target = resolveCustomOptionReference(fieldReference, message, key);
                } else {
                    target = resolveStandardOptionReference(fieldReference, message, key);
                }
                message = null;
                if (target instanceof FieldNode) {
                    FieldNode targetField = (FieldNode) target;
                    TypeReferenceNode fieldTypeRef = targetField.getFieldType();
                    if (fieldTypeRef != null) {
                        PsiReference reference = fieldTypeRef.getReference();
                        if (reference != null) {
                            PsiElement fieldType = reference.resolve();
                            if (fieldType instanceof MessageNode) {
                                message = (MessageNode) fieldType;
                            }
                        }
                    }
                }
                if (target instanceof GroupNode) {
                    message = (MessageNode) target;
                }
            }
            TextRange textRange = getTextRange(fieldReference, fieldRef);
            result.add(new OptionReference(fieldReference, textRange, target));
        }
        Collections.reverse(result);
        return result.toArray(new PsiReference[0]);
    }

    @NotNull
    private TextRange getTextRange(FieldReferenceNode sourceReference, AbstractFieldReferenceNode subReference) {
        int baseOffset = sourceReference.getTextOffset();
        int startOffset = subReference.getTextOffset();
        int length = subReference.getTextLength();
        return new TextRange(startOffset - baseOffset, startOffset - baseOffset + length);
    }

    @Nullable
    private String getTarget(PsiElement element) {
        while (element != null) {
            String result = TARGET_MAPPING.get(element.getClass());
            if (result != null) {
                return result;
            }
            element = element.getParent();
        }
        return null;
    }

    private ProtoRootNode getProtoRoot(PsiElement element) {
        PsiElement parent = element.getParent();
        while (!(parent instanceof ProtoRootNode)) {
            parent = parent.getParent();
        }
        return (ProtoRootNode) parent;
    }

    private MessageField resolveStandardOptionReference(PsiElement sourceElement, MessageNode target, String key) {
        if (MSG_FIELD_OPTIONS.equals(target.getQualifiedName())) {
            if (DEFAULT.equals(key)) {
                return resolveDefaultOptionReference(sourceElement);
            }
            if (JSON_NAME.equals(key)) {
                MessageField result = resolveJsonNameOptionReference(sourceElement, key);
                return result;
            }
        }
        for (MessageField field : target.getFields()) {
            if (Objects.equals(key, field.getFieldName())) {
                return field;
            }
        }
        return null;
    }

    /**
     * "default" field option is a special case: it is not defined
     * in the {@code google/protobuf/descriptor.proto} and it cannot
     * be treated like other options, as its type depends on a field's
     * type.
     * <p>
     * In order to implement value validation, we have to return the
     * field where this option was applied.
     */
    private MessageField resolveDefaultOptionReference(PsiElement element) {
        while (element != null) {
            if (element instanceof FieldNode) {
                return (MessageField) element;
            }
            element = element.getParent();
        }
        return null;
    }

    /**
     * "json_name" field option is a special case: it is not defined in google.protobuf.FieldOptions,
     * so it is not an actual option.
     * It's definition is inside of google.protobuf.FieldDescriptorProto.
     */
    private MessageField resolveJsonNameOptionReference(PsiElement sourceElement, String key) {
        MessageNode message = resolveType(sourceElement, ".google.protobuf.FieldDescriptorProto");
        MessageField result = null;
        if (message != null) {
            result = resolveStandardOptionReference(sourceElement, message, key);
        }
        return result;
    }

    @Nullable
    private MessageField resolveCustomOptionReference(PsiElement element, MessageNode target, String key) {
        ProtoRootNode protoRoot = getProtoRoot(element);
        DataTypeContainer container = getContainer(element);
        Deque<String> scopeLookupList = TypeReferenceProviderImpl.createScopeLookupList(container);
        // case 1: (.package.field)
        // case 2: (.package.field).field
        // case 3: (.package.field).(.package.field)
        Collection<ExtendNode> extensions = protoRoot.getExtenstions(target);
        Map<String, MessageField> extensionFields = new HashMap<>();
        for (ExtendNode extension : extensions) {
            for (MessageField field : extension.getExtensionFields().values()) {
                extensionFields.put(extension.getNamespace() + field.getFieldName(), field);
            }
        }
        if (key.startsWith(".")) {
            return extensionFields.get(key);
        } else {
            for (String scope : scopeLookupList) {
                MessageField field = extensionFields.get(scope + key);
                if (field != null) {
                    return field;
                }
            }
        }
        return null;
    }

    @NotNull
    private DataTypeContainer getContainer(PsiElement element) {
        PsiElement parent = element.getParent();
        while (!(parent instanceof DataTypeContainer)) {
            parent = parent.getParent();
        }
        return (DataTypeContainer) parent;
    }

    private MessageNode resolveType(PsiElement element, String qualifiedName) {
        MessageNode message = resolveTypeFromCurrentFile(element, qualifiedName);
        // For standard options import is not required.
        // This way they cannot be resolved in standard way, we have to check them
        // separately using bundled descriptor.proto
        // TODO: what if there is non-bundled descriptor.proto available in other location?
        if (message == null) {
            ProtoPsiFileRoot descriptorProto = (ProtoPsiFileRoot) loadInMemoryDescriptorProto();
            if (descriptorProto == null) {
                // by some reason protobuf library is not yet loaded or not attached
                return null;
            }
            return (MessageNode) descriptorProto.findType(qualifiedName.substring(1));
        }
        return message;
    }

    @Nullable
    private PsiFile loadInMemoryDescriptorProto() {
        if (inm == null) {
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
                VirtualFile[] allSourceRoots = moduleRootManager.orderEntries().getAllSourceRoots();
                for (VirtualFile allSourceRoot : allSourceRoots) {
                    PsiDirectory directory = PsiManager.getInstance(project).findDirectory(allSourceRoot);
                    if (directory != null && directory.isValid()) {
                        String relPath = "google/protobuf/descriptor.proto";
                        VirtualFile file = directory.getVirtualFile().findFileByRelativePath(relPath);
                        if (file != null) {
                            PsiManager psiManager = PsiManager.getInstance(project);
                            PsiFile psiFile = psiManager.findFile(file);
                            if (psiFile instanceof ProtoPsiFileRoot) {
                                inm = psiFile;
                                return (ProtoPsiFileRoot) psiFile;
                            }
                        }
                    }
                }
            }
        }
        return inm;
    }

    @Nullable
    private MessageNode resolveTypeFromCurrentFile(PsiElement element, String qualifiedName) {
        PsiElement protoElement = element;
        while (protoElement != null && !(protoElement instanceof ProtoRootNode)) {
            protoElement = protoElement.getParent();
        }
        if (protoElement == null) {
            return null;
        }
        ProtoRootNode proto = (ProtoRootNode) protoElement;
        return (MessageNode) proto.resolve(qualifiedName, new ArrayDeque<>());
    }

}

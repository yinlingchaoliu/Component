package com.xiaojinzi.component;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component.bean.RouterAnnoBean;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedOptions("HOST")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({com.xiaojinzi.component.ComponentUtil.ROUTER_ANNO_CLASS_NAME})
public class RouterProcessor extends BaseHostProcessor {

    private static final String NAME_OF_REQUEST = "request";

    private ClassName customerIntentCallClassName;

    private TypeElement routerBeanTypeElement;
    private ClassName exceptionClassName;
    private TypeElement intentTypeElement;
    private TypeMirror intentTypeMirror;
    private TypeMirror routerRequestTypeMirror;
    private TypeElement interceptorTypeElement;

    final AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        interceptorTypeElement = mElements.getTypeElement(ComponentConstants.INTERCEPTOR_INTERFACE_CLASS_NAME);
        routerBeanTypeElement = mElements.getTypeElement(ComponentConstants.ROUTER_BEAN_CLASS_NAME);
        final TypeElement exceptionTypeElement = mElements.getTypeElement(ComponentConstants.JAVA_EXCEPTION);
        exceptionClassName = ClassName.get(exceptionTypeElement);
        final TypeElement customerIntentCallTypeElement = mElements.getTypeElement(ComponentConstants.CUSTOMER_INTENT_CALL_CLASS_NAME);
        intentTypeElement = mElements.getTypeElement(ComponentConstants.ANDROID_INTENT);
        intentTypeMirror = intentTypeElement.asType();
        final TypeElement routerRequestTypeElement = mElements.getTypeElement(ComponentConstants.ROUTER_REQUEST_CLASS_NAME);
        routerRequestTypeMirror = routerRequestTypeElement.asType();
        customerIntentCallClassName = ClassName.get(customerIntentCallTypeElement);

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isNotEmpty(set)) {
            final Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(RouterAnno.class);
            parseAnno(routeElements);
            createRouterImpl();
            return true;
        }
        return false;
    }

    private final Map<String, RouterAnnoBean> routerMap = new HashMap<>();

    /**
     * 解析注解
     *
     * @param routeElements
     */
    private void parseAnno(Set<? extends Element> routeElements) {
        for (Element element : routeElements) {
            // 如果是一个Activity 才会走到这里
            final RouterAnno router = element.getAnnotation(RouterAnno.class);
            if (router == null) {
                // 理论上不是不可能成立的
                continue;
            }
            final RouterAnnoBean routerBean = toRouterAnnoBean(element, router);
            // 如果重复就抛出异常
            if (routerMap.containsKey(routerBean.hostAndPath())) {
                throw new ProcessException("the url value '" + routerBean.hostAndPath() + "' of " + element + " is alreay exist");
            }
            routerMap.put(routerBean.hostAndPath(), routerBean);
        }
    }

    private RouterAnnoBean toRouterAnnoBean(Element element, RouterAnno routerAnno) {

        // 如果有host那就必须满足规范
        if (routerAnno.host() != null && !routerAnno.host().isEmpty() && routerAnno.host().contains("/")) {
            throw new ProcessException(element + "the host path '" + routerAnno.host() + "' can't contains '/'");
        }

        final RouterAnnoBean routerBean = new RouterAnnoBean();
        String host = routerAnno.host();
        String path = routerAnno.path();

        String hostAndPath = routerAnno.hostAndPath();
        if (!"".equals(hostAndPath)) { // 如果用户填写了 hostAndPath 就拆分出 host 和 path 覆盖之前的
            int index = hostAndPath.indexOf('/');
            if (index < 0) {
                throw new ProcessException("the hostAndPath(" + hostAndPath + ") must have '/',such as \"app/test\"");
            }
            if (index == 0 || index == hostAndPath.length() - 1) {
                throw new ProcessException("the hostAndPath(" + hostAndPath + ") can't start with '/' and end with '/'");
            }
            host = hostAndPath.substring(0, index);
            path = hostAndPath.substring(index + 1);
        }
        // 如果用户 host 没填
        if (host == null || host.isEmpty()) {
            host = componentHost;
        }
        // 如果 path 没有 / 开头,会自动加一个
        if (path != null && path.length() > 0 && path.charAt(0) != '/') {
            path = ComponentConstants.SEPARATOR + path;
        }

        routerBean.setHost(host);
        // 一定 '/' 开头的
        routerBean.setPath(path);

        routerBean.setDesc(routerAnno.desc());
        routerBean.setRawType(element);
        routerBean.getInterceptors().clear();
        routerBean.getInterceptors().addAll(getImplClassName(routerAnno));
        if (routerAnno.interceptorNames() != null) {
            routerBean.getInterceptorNames().clear();
            for (String interceptorName : routerAnno.interceptorNames()) {
                routerBean.getInterceptorNames().add(interceptorName);
            }
        }

        return routerBean;

    }

    /**
     * 生成路由
     */
    private void createRouterImpl() {
        final String claName = com.xiaojinzi.component.ComponentUtil.genHostRouterClassName(componentHost);
        //pkg
        final String pkg = claName.substring(0, claName.lastIndexOf('.'));
        //simpleName
        final String cn = claName.substring(claName.lastIndexOf('.') + 1);
        // superClassName
        final ClassName superClass = ClassName.get(mElements.getTypeElement(ComponentUtil.UIROUTER_IMPL_CLASS_NAME));
        MethodSpec initHostMethod = generateInitHostMethod();
        MethodSpec initMapMethod = generateInitMapMethod();
        TypeSpec typeSpec = TypeSpec.classBuilder(cn)
                .addAnnotation(mClassNameKeep)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .superclass(superClass)
                .addMethod(initHostMethod)
                .addMethod(initMapMethod)
                .addJavadoc(componentHost + "业务模块的路由表\n")
                .build();
        try {
            JavaFile.builder(pkg, typeSpec)
                    .indent("    ")
                    .build().writeTo(mFiler);
        } catch (IOException e) {
            throw new ProcessException(e);
        }
    }

    private MethodSpec generateInitMapMethod() {
        TypeName returnType = TypeName.VOID;
        final MethodSpec.Builder initMapMethodSpecBuilder = MethodSpec.methodBuilder("initMap")
                .returns(returnType)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);
        initMapMethodSpecBuilder.addStatement("super.initMap()");
        routerMap.forEach(new BiConsumer<String, RouterAnnoBean>() {
            @Override
            public void accept(String key, RouterAnnoBean routerBean) {
                // 生成变量的名字,每一个变量代表每一个目标界面的配置对象
                String routerBeanName = "routerBean" + atomicInteger.incrementAndGet();
                // 生成 Activity 的调用代码
                generateActivityCall(routerBean, routerBeanName, initMapMethodSpecBuilder);
                // 生成静态方法的代码的调用
                generateStaticMethodCall(routerBean, routerBeanName, initMapMethodSpecBuilder);
                // 拦截器的代码的生成
                if (routerBean.getInterceptors() != null && !routerBean.getInterceptors().isEmpty()) {
                    String interceptorListName = "interceptorList" + atomicInteger.incrementAndGet();
                    initMapMethodSpecBuilder.addStatement("java.util.List<Class<? extends $T>> " + interceptorListName + " = new $T($L)",
                            interceptorTypeElement, ArrayList.class, routerBean.getInterceptors().size());
                    for (String interceptorClassName : routerBean.getInterceptors()) {
                        initMapMethodSpecBuilder.addStatement("$N.add($T.class)", interceptorListName, ClassName.get(mElements.getTypeElement(interceptorClassName)));
                    }
                    initMapMethodSpecBuilder.addStatement("$N.setInterceptors($N)", routerBeanName, interceptorListName);
                }
                if (routerBean.getInterceptorNames() != null && !routerBean.getInterceptorNames().isEmpty()) {
                    String interceptorNameListName = "interceptorNameList" + atomicInteger.incrementAndGet();
                    initMapMethodSpecBuilder.addStatement("java.util.List<String> " + interceptorNameListName + " = new $T($L)", ArrayList.class, routerBean.getInterceptorNames().size());
                    for (String interceptorName : routerBean.getInterceptorNames()) {
                        initMapMethodSpecBuilder.addStatement("$N.add($S)", interceptorNameListName, interceptorName);
                    }
                    initMapMethodSpecBuilder.addStatement("$N.setInterceptorNames($N)", routerBeanName, interceptorNameListName);
                }
                initMapMethodSpecBuilder.addStatement("routerBeanMap.put($S,$N)", key, routerBeanName);
                initMapMethodSpecBuilder.addCode("\n");
            }
        });
        initMapMethodSpecBuilder.addJavadoc("初始化路由表的数据\n");
        return initMapMethodSpecBuilder.build();
    }

    private MethodSpec generateInitHostMethod() {

        TypeName returnType = mClassNameString;
        MethodSpec.Builder openUriMethodSpecBuilder = MethodSpec.methodBuilder("getHost")
                .returns(returnType)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        openUriMethodSpecBuilder.addStatement("return $S", componentHost);
        return openUriMethodSpecBuilder.build();

    }

    /**
     * 如果 host 为空会使用默认值
     *
     * @param anno 为空的话默认使用默认的host
     * @return
     */
    private String getHostAndPathFromAnno(RouterAnno anno) {
        String host = anno.host();
        String path = anno.path();
        String hostAndPath = anno.hostAndPath();
        if (!"".equals(hostAndPath)) { // 如果用户填写了 hostAndPath 就拆分出 host 和 path 覆盖之前的
            int index = hostAndPath.indexOf('/');
            if (index < 0) {
                throw new ProcessException("the hostAndPath(" + hostAndPath + ") must have '/',such as \"app/test\"");
            }
            if (index == 0 || index == hostAndPath.length() - 1) {
                throw new ProcessException("the hostAndPath(" + hostAndPath + ") can't start with '/' and end with '/'");
            }
            host = hostAndPath.substring(0, index);
            path = hostAndPath.substring(index + 1);
        }
        // 如果用户 host 没填
        if (host == null || host.isEmpty()) {
            host = componentHost;
        }
        // 如果 path 没有 / 开头,会自动加一个
        if (path != null && path.length() > 0 && path.charAt(0) != '/') {
            path = ComponentConstants.SEPARATOR + path;
        }
        return host + path;
    }

    private List<String> getImplClassName(RouterAnno anno) {
        List<String> implClassNames = new ArrayList<>();
        try {
            implClassNames.clear();
            //这里会报错，此时在catch中获取到拦截器的全类名
            final Class[] interceptors = anno.interceptors();
            // 这个循环其实不会走,我就随便写的,不过最好也不要删除
            for (Class interceptor : interceptors) {
                implClassNames.add(interceptor.getName());
            }
        } catch (MirroredTypesException e) {
            implClassNames.clear();
            final List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
            for (TypeMirror typeMirror : typeMirrors) {
                implClassNames.add(typeMirror.toString());
            }
        }
        return implClassNames;
    }

    /**
     * 生成目标是 Activity 的调用
     *
     * @param routerBean
     * @param methodSpecBuilder
     */
    private void generateActivityCall(RouterAnnoBean routerBean, String routerBeanName, MethodSpec.Builder methodSpecBuilder) {
        if (!(routerBean.getRawType() instanceof TypeElement)) {
            return;
        }
        ClassName targetActivityClassName = ClassName.get((TypeElement) routerBean.getRawType());
        String commentStr = targetActivityClassName.toString();
        methodSpecBuilder.addCode("\n");
        methodSpecBuilder.addComment(NORMALLINE + commentStr + NORMALLINE);
        methodSpecBuilder.addCode("\n");
        methodSpecBuilder.addStatement("$T $N = new $T()", routerBeanTypeElement, routerBeanName, routerBeanTypeElement);
        //methodSpecBuilder.addStatement("$N.setHost($S)", routerBeanName, routerBean.getHost());
        //methodSpecBuilder.addStatement("$N.setPath($S)", routerBeanName, routerBean.getPath());
        methodSpecBuilder.addStatement("$N.setDesc($S)", routerBeanName, routerBean.getDesc());
        methodSpecBuilder.addStatement("$N.setTargetClass($T.class)", routerBeanName, targetActivityClassName);
    }

    /**
     * 目的是生成调用静态方法的实现
     *
     * @param routerBean        注解的实体对象
     * @param methodSpecBuilder 生成方法代码的 Builder
     * @return
     */
    private void generateStaticMethodCall(RouterAnnoBean routerBean, String routerBeanName, MethodSpec.Builder methodSpecBuilder) {
        if (!(routerBean.getRawType() instanceof ExecutableElement)) {
            return;
        }
        // 静态方法
        ExecutableElement executableElement = (ExecutableElement) routerBean.getRawType();
        // 获取声明这个方法的类的 TypeElement
        TypeElement declareClassType = (TypeElement) executableElement.getEnclosingElement();
        // 调用这个静态方法的全路径
        String customerIntentOrJumpPath = declareClassType.toString() + "." + executableElement.getSimpleName();
        // 注释的信息
        String commentStr = customerIntentOrJumpPath;
        // 获取自定义的静态方法的返回类型
        TypeMirror customerReturnType = executableElement.getReturnType();

        methodSpecBuilder.addCode("\n");
        methodSpecBuilder.addComment("---------------------------" + commentStr + "---------------------------");
        methodSpecBuilder.addCode("\n");
        methodSpecBuilder.addStatement("$T $N = new $T()", routerBeanTypeElement, routerBeanName, routerBeanTypeElement);
        //methodSpecBuilder.addStatement("$N.setHost($S)", routerBeanName, routerBean.getHost());
        //methodSpecBuilder.addStatement("$N.setPath($S)", routerBeanName, routerBean.getPath());
        methodSpecBuilder.addStatement("$N.setDesc($S)", routerBeanName, routerBean.getDesc());
        // 如果是自定义 Intent
        if (intentTypeMirror.equals(customerReturnType)) {
            MethodSpec.Builder jumpMethodBuilder = MethodSpec.methodBuilder("get")
                    .addParameter(TypeName.get(routerRequestTypeMirror), NAME_OF_REQUEST, Modifier.FINAL)
                    .addAnnotation(Override.class)
                    .addException(exceptionClassName)
                    .addModifiers(Modifier.PUBLIC);
            generateActualMethodCall(jumpMethodBuilder, executableElement, customerIntentOrJumpPath, true);
            jumpMethodBuilder.returns(TypeName.get(intentTypeElement.asType()));

            TypeSpec intentCallTypeSpec = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(customerIntentCallClassName)
                    .addMethod(
                            jumpMethodBuilder.build()
                    )
                    .build();
            // 添加一个匿名内部类
            methodSpecBuilder.addStatement("$N.setCustomerIntentCall($L)", routerBeanName, intentCallTypeSpec);
        } else { // 自定义跳转的
            throw new ProcessException("the return type of method(" + customerIntentOrJumpPath + ") must be 'Intent' ");
        }
    }

    private void generateActualMethodCall(MethodSpec.Builder jumpMethodBuilder, ExecutableElement executableElement,
                                          String customerIntentOrJumpPath, boolean isReturnIntent) {
        // 获取这个方法的参数
        List<? extends VariableElement> parameters = executableElement.getParameters();
        // 参数调用的 sb
        StringBuilder parameterSB = new StringBuilder();
        if (parameters != null && !parameters.isEmpty()) {
            for (int i = 0, size = parameters.size(); i < size; i++) {
                if (i > 0) {
                    parameterSB.append(",");
                }
                // 拿到每一个参数
                VariableElement variableElement = parameters.get(i);
                TypeName parameterTypeName = ClassName.get(variableElement.asType());
                // 生成一个不重复的参数名字
                String parameterName = "paramater" + atomicInteger.incrementAndGet();
                // 如果要的是 request 对象
                if (variableElement.asType().equals(routerRequestTypeMirror)) {
                    parameterSB.append(NAME_OF_REQUEST);
                } else { // 其他类型的情况,是实现序列化的对象,这种时候我们直接要从 bundle 中获取
                    /*Utils.generateParameterCodeForRouter(
                            mTypes, variableElement, jumpMethodBuilder,
                            parameterSupportTypeMirror, parameterName,"request.bundle",
                            mClassNameString, serializableTypeMirror, parcelableTypeMirror
                    );
                    parameterSB.append(parameterName);*/
                    throw new ProcessException("can relsove parameter " + variableElement.getSimpleName() + " in " + variableElement.getEnclosingElement().getEnclosingElement().getSimpleName() + "." + variableElement.getEnclosingElement().getSimpleName() + " method");
                }
            }
        }
        if (isReturnIntent) {
            jumpMethodBuilder.addStatement("return $N($N)", customerIntentOrJumpPath, parameterSB.toString());
        } else {
            jumpMethodBuilder.addStatement("$N($N)", customerIntentOrJumpPath, parameterSB.toString());
        }
    }

}

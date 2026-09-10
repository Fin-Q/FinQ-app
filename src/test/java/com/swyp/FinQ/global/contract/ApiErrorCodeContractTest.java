package com.swyp.FinQ.global.contract;

import com.swyp.FinQ.global.config.ApiDocumentation;
import com.swyp.FinQ.global.exception.ErrorCodeCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatCode;

class ApiErrorCodeContractTest {

    @Test
    void ApiDocumentation은_카탈로그에_존재하는_에러_코드만_참조한다() throws Exception {
        for (Class<?> controller : controllerClasses()) {
            for (Method method : controller.getDeclaredMethods()) {
                ApiDocumentation documentation = method.getAnnotation(ApiDocumentation.class);
                if (documentation == null) {
                    continue;
                }

                for (String errorCode : documentation.errors()) {
                    assertThatCode(() -> ErrorCodeCatalog.require(errorCode))
                            .as("%s#%s가 참조한 에러 코드: %s",
                                    controller.getSimpleName(), method.getName(), errorCode)
                            .doesNotThrowAnyException();
                }
            }
        }
    }

    private Class<?>[] controllerClasses() throws Exception {
        String packagePath = "com/swyp/FinQ";
        URL resource = Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResource(packagePath)
        );
        File root = new File(resource.toURI());

        return Arrays.stream(Objects.requireNonNull(root.listFiles()))
                .filter(File::isDirectory)
                .flatMap(domain -> findControllerClasses(domain, "com.swyp.FinQ." + domain.getName()))
                .filter(type -> type.isAnnotationPresent(RestController.class))
                .toArray(Class<?>[]::new);
    }

    private java.util.stream.Stream<Class<?>> findControllerClasses(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) {
            return java.util.stream.Stream.empty();
        }

        return Arrays.stream(files).flatMap(file -> {
            if (file.isDirectory()) {
                return findControllerClasses(file, packageName + "." + file.getName());
            }
            if (!file.getName().endsWith("Controller.class") || file.getName().contains("$")) {
                return java.util.stream.Stream.empty();
            }
            String className = packageName + "." + file.getName().replace(".class", "");
            try {
                return java.util.stream.Stream.of(Class.forName(className));
            } catch (ClassNotFoundException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }
}
